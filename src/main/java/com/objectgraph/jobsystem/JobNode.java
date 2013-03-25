package com.objectgraph.jobsystem;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import com.objectgraph.core.ObjectNode;
import com.objectgraph.jobsystem.exceptions.JobInterruptedException;
import com.objectgraph.jobsystem.exceptions.TryLaterException;
import com.objectgraph.pluginsystem.PluginManager;

public abstract class JobNode extends ObjectNode {
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	protected @interface Job {}
	
	private static final Map<Class<?>, Map<String, JobInfo>> classJobInfos = new HashMap<>();
	private static final Map<Class<?>, List<String>> classJobs = new HashMap<>();
	
	private static final Map<Thread, LinkedList<JobInfo>> threadJobs = new HashMap<>();
	
	private static final Object lock = new Object();
	
	public JobNode() {
		prepareJobs(getClass());
	}
	
	protected static void checkForPauseOrInterruption() {
		if (Thread.interrupted()) {
			onInterrupted();
		}
	}
	
	private static void onInterrupted() {
		if (Thread.currentThread() instanceof PausableThread) {
			try {
				PausableThread thread = (PausableThread)Thread.currentThread();
				if (!thread.isPaused())
					cleanUp();
				thread.waitWhilePaused();
			} catch (InterruptedException ex) {
				cleanUp();
			}
			return; // No interruption, just pause
		}
		
		PausableAsyncTask<?> task = PausableAsyncTask.getTaskFromThread();
		if (task != null) {
			try {
				if (!task.isPaused())
					cleanUp();
				task.waitWhilePaused();
			} catch (InterruptedException ex) {
				cleanUp();
			}
			return; // No interruption, just pause
		}
		
		cleanUp();
	}
	
	private static void cleanUp() {
		synchronized (lock) {
			threadJobs.get(Thread.currentThread()).clear();
		}
		throw new JobInterruptedException();
	}

	protected static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			onInterrupted();
		}
	}
	
	protected void startJob() {
		checkForPauseOrInterruption();
		synchronized (lock) {
			Thread current = Thread.currentThread();
			if (!threadJobs.containsKey(current))
				threadJobs.put(current, new LinkedList<JobInfo>());
			String jobName = current.getStackTrace()[2].getMethodName();
			int callPosition = current.getStackTrace()[3].getLineNumber();
			threadJobs.get(current).addLast(getJobInfos().get(jobName).instantiate(callPosition));
		}
	}
	
	protected void endJob() {
		checkForPauseOrInterruption();
		synchronized (lock) {
			Thread current = Thread.currentThread();
			threadJobs.get(current).removeLast();
			if (threadJobs.get(current).isEmpty())
				threadJobs.remove(current);
		}
	}
	
	protected void startCycle(int currentRun, int totalRuns) {
		if (totalRuns <= 0)
			totalRuns = 1;
		if (currentRun >= totalRuns)
			currentRun = totalRuns - 1;
		checkForPauseOrInterruption();
		synchronized (lock) {
			Thread current = Thread.currentThread();
			String jobName = current.getStackTrace()[2].getMethodName();
			int currentLine = current.getStackTrace()[2].getLineNumber();
			CycleInfo info = getJobInfos().get(jobName).getCycles().get(currentLine).instantiate(currentRun, totalRuns, "");
			threadJobs.get(current).getLast().getCurrentCycles().addLast(info);
		}
	}
	
	protected void startCycle(String description, int currentRun, int totalRuns) {
		if (totalRuns <= 0)
			totalRuns = 1;
		if (currentRun < 0)
			currentRun = 0;
		if (currentRun >= totalRuns)
			currentRun = totalRuns - 1;
		checkForPauseOrInterruption();
		synchronized (lock) {
			Thread current = Thread.currentThread();
			String jobName = current.getStackTrace()[2].getMethodName();
			int currentLine = current.getStackTrace()[2].getLineNumber();
			CycleInfo info = getJobInfos().get(jobName).getCycles().get(currentLine).instantiate(currentRun, totalRuns, description);
			threadJobs.get(current).getLast().getCurrentCycles().addLast(info);
		}
	}
	
	protected void endCycle() {
		checkForPauseOrInterruption();
		synchronized (lock) {
			threadJobs.get(Thread.currentThread()).getLast().getCurrentCycles().removeLast();
		}
	}
	
	protected int getProgress() {
		try {
			return getProgress(Thread.currentThread());
		} catch (TryLaterException e) { /* Can never happen */ return -1; }
	}
	
	public static int getProgress(Thread worker) throws TryLaterException {
		synchronized (lock) {
			StackTraceElement[] stackTrace = worker.getStackTrace();
			String innermostType = null;
			String innermostJob = null;
			int currentLine = -1;
			for(StackTraceElement e: stackTrace) {
				if (getJobInfos(e.getClassName()).containsKey(e.getMethodName())) {
					currentLine = e.getLineNumber();
					innermostType = e.getClassName();
					innermostJob = e.getMethodName();
					break;
				}
			}
			if (currentLine < 0 || !threadJobs.containsKey(worker)) 
				throw new TryLaterException();
			
			LinkedList<JobInfo> jobs = new LinkedList<>(threadJobs.get(worker));
			if (jobs.isEmpty() || 
					!jobs.getLast().getJobName().equals(innermostJob) || 
					!jobs.getLast().getTypeName().equals(innermostType))
				throw new TryLaterException();
			
			JobInfo outerjob = jobs.pollFirst();
			double progress = 0;
			double outerstart = outerjob.getPosition();
			double outerLength = outerjob.getRunningLength();
			double ratio = 1;
			double currentRun = 0;
			double totalRuns = 1;
			while(true) {
				if (outerjob.getCycles().containsKey(currentLine))
					throw new TryLaterException();
				
				for(CycleInfo cycleInfo: outerjob.getCurrentCycles()) {
					progress +=  ratio * ((cycleInfo.getRunningPosition() - outerstart) / outerLength + currentRun) / totalRuns;
					ratio = ratio * cycleInfo.getRunningLength() / (outerLength * totalRuns);
					outerstart = cycleInfo.getRunningPosition();
					outerLength = cycleInfo.getRunningLength();
					currentRun = cycleInfo.getCurrentRun();
					totalRuns = cycleInfo.getTotalRuns();
				}
				
				if (jobs.isEmpty())
					break;
				
				JobInfo innerjob = jobs.pollFirst();
				progress += ratio * (((outerjob.getRunningLine(innerjob.getCallPosition()) - outerstart) / outerLength) + currentRun) / totalRuns;
				ratio = ratio * innerjob.getCodeLength() / (outerLength * totalRuns);
				outerstart = innerjob.getPosition();
				outerLength = innerjob.getRunningLength();
				currentRun = 0;
				totalRuns = 1;
				outerjob = innerjob;
			}
			
			progress += ratio * ((outerjob.getRunningLine(currentLine) - outerstart) / outerLength + currentRun) / totalRuns;
			
			return (int)(100*progress);
		}
		
	}

	public List<String> getJobs() {
		return classJobs.get(getClass());
	}
	
	private static void prepareJobs(Class<? extends JobNode> type) {
		if (!classJobInfos.containsKey(type)) {
			classJobInfos.put(type, new HashMap<String, JobInfo>());
			Map<String, JobInfo> jobs = classJobInfos.get(type);
			Method[] methods = type.getMethods();
			ClassPool pool = ClassPool.getDefault();
			pool.appendClassPath(new ClassClassPath(type));
			try {
				CtClass cc = pool.get(type.getName());
				for(Method m: methods) {
					if (m.isAnnotationPresent(Job.class)) {
						jobs.put(m.getName(), new JobInfo(cc.getDeclaredMethod(m.getName()), type.getName(), m.getName()));
					}
				}
			} catch (NotFoundException ex) { /* Cannot happen */ }
			for(JobInfo info: jobs.values()) {
				info.computeRunningLength(jobs);
			}
			List<String> list = new ArrayList<>(jobs.keySet());
			Collections.sort(list);
			classJobs.put(type, Collections.unmodifiableList(list));
		}
	}

	private Map<String, JobInfo> getJobInfos() {
		return getJobInfos(getClass().getName());
	}
	
	private static Map<String, JobInfo> getJobInfos(String typeName) {
		try {
			Class<?> type = PluginManager.getClassLoader().loadClass(typeName);
			if (!classJobInfos.containsKey(type)) {
				return Collections.emptyMap();
			}
			return classJobInfos.get(type);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
			return null;
		}
	}

}
