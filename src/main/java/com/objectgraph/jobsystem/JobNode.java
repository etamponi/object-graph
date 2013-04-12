/*
 * Copyright 2013 Emanuele Tamponi
 *
 * This file is part of object-graph.
 *
 * object-graph is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * object-graph is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with object-graph.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.objectgraph.jobsystem;

import com.objectgraph.core.ObjectNode;
import com.objectgraph.pluginsystem.PluginManager;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.*;

public abstract class JobNode extends ObjectNode {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    protected @interface Job { }

    private static final Map<Class<?>, Map<String, JobInfo>> CLASS_JOB_INFOS = new HashMap<>();
    private static final Map<Class<?>, List<String>> CLASS_JOBS = new HashMap<>();

    private static final Map<Thread, LinkedList<JobInfo>> THREAD_JOBS = new HashMap<>();
    private static final Object THREAD_JOBS_LOCK = new Object();

    private static final int CALLER_METHOD = 2;

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
                PausableThread thread = (PausableThread) Thread.currentThread();
                if (!thread.isPaused()) {
                    cleanUp();
                } else {
                    thread.waitWhilePaused();
                }
            } catch (InterruptedException ex) {
                cleanUp();
            }
            // No interruption, just pause
            return;
        }

        PausableAsyncTask<?> task = PausableAsyncTask.getTaskFromThread();
        if (task != null) {
            try {
                if (!task.isPaused()) {
                    cleanUp();
                } else {
                    task.waitWhilePaused();
                }
            } catch (InterruptedException ex) {
                cleanUp();
            }
            // No interruption, just pause
            return;
        }

        cleanUp();
    }

    private static void cleanUp() {
        synchronized (THREAD_JOBS_LOCK) {
            THREAD_JOBS.get(Thread.currentThread()).clear();
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
        synchronized (THREAD_JOBS_LOCK) {
            Thread current = Thread.currentThread();
            if (!THREAD_JOBS.containsKey(current)) {
                THREAD_JOBS.put(current, new LinkedList<JobInfo>());
            }
            String jobName = current.getStackTrace()[CALLER_METHOD].getMethodName();
            int callPosition = current.getStackTrace()[CALLER_METHOD+1].getLineNumber();
            THREAD_JOBS.get(current).addLast(getJobInfos().get(jobName).instantiate(callPosition));
        }
    }

    protected void endJob() {
        checkForPauseOrInterruption();
        synchronized (THREAD_JOBS_LOCK) {
            Thread current = Thread.currentThread();
            THREAD_JOBS.get(current).removeLast();
            if (THREAD_JOBS.get(current).isEmpty()) {
                THREAD_JOBS.remove(current);
            }
        }
    }

    protected void startCycle(int currentRun, int totalRuns, String description) {
        if (totalRuns <= 0) {
            totalRuns = 1;
        }
        if (currentRun < 0) {
            currentRun = 0;
        }
        if (currentRun >= totalRuns) {
            currentRun = totalRuns - 1;
        }
        checkForPauseOrInterruption();
        synchronized (THREAD_JOBS_LOCK) {
            Thread current = Thread.currentThread();
            String jobName = current.getStackTrace()[CALLER_METHOD].getMethodName();
            int currentLine = current.getStackTrace()[CALLER_METHOD].getLineNumber();
            CycleInfo info = getJobInfos().get(jobName).getCycles().get(currentLine).instantiate(currentRun, totalRuns, description);
            THREAD_JOBS.get(current).getLast().getCurrentCycles().addLast(info);
        }
    }

    protected void endCycle() {
        checkForPauseOrInterruption();
        synchronized (THREAD_JOBS_LOCK) {
            THREAD_JOBS.get(Thread.currentThread()).getLast().getCurrentCycles().removeLast();
        }
    }

    protected int getProgress() {
        try {
            return getProgress(Thread.currentThread());
        } catch (TryLaterException e) {
            // Can never happen
            return -1;
        }
    }

    public static int getProgress(Thread worker) throws TryLaterException {
        synchronized (THREAD_JOBS_LOCK) {
            StackTraceElement[] stackTrace = worker.getStackTrace();
            String innermostType = null;
            String innermostJob = null;
            int currentLine = -1;
            for (StackTraceElement e : stackTrace) {
                if (getJobInfos(e.getClassName()).containsKey(e.getMethodName())) {
                    currentLine = e.getLineNumber();
                    innermostType = e.getClassName();
                    innermostJob = e.getMethodName();
                    break;
                }
            }
            if (currentLine < 0 || !THREAD_JOBS.containsKey(worker)) {
                throw new TryLaterException();
            }

            LinkedList<JobInfo> jobs = new LinkedList<>(THREAD_JOBS.get(worker));
            if (jobs.isEmpty() ||
                    !jobs.getLast().getJobName().equals(innermostJob) ||
                    !jobs.getLast().getTypeName().equals(innermostType)) {
                throw new TryLaterException();
            }

            JobInfo outerJob = jobs.pollFirst();
            double progress = 0;
            double outerStart = outerJob.getPosition();
            double outerLength = outerJob.getRunningLength();
            double ratio = 1;
            double currentRun = 0;
            double totalRuns = 1;
            while (true) {
                if (outerJob.getCycles().containsKey(currentLine)) {
                    throw new TryLaterException();
                }

                for (CycleInfo cycleInfo : outerJob.getCurrentCycles()) {
                    progress += ratio * ((cycleInfo.getRunningPosition() - outerStart) / outerLength + currentRun) / totalRuns;
                    ratio = ratio * cycleInfo.getRunningLength() / (outerLength * totalRuns);
                    outerStart = cycleInfo.getRunningPosition();
                    outerLength = cycleInfo.getRunningLength();
                    currentRun = cycleInfo.getCurrentRun();
                    totalRuns = cycleInfo.getTotalRuns();
                }

                if (jobs.isEmpty()) {
                    break;
                }

                JobInfo innerJob = jobs.pollFirst();
                progress += ratio * (((outerJob.getRunningLine(innerJob.getCallPosition()) - outerStart) / outerLength) + currentRun) / totalRuns;
                ratio = ratio * innerJob.getCodeLength() / (outerLength * totalRuns);
                outerStart = innerJob.getPosition();
                outerLength = innerJob.getRunningLength();
                currentRun = 0;
                totalRuns = 1;
                outerJob = innerJob;
            }

            progress += ratio * ((outerJob.getRunningLine(currentLine) - outerStart) / outerLength + currentRun) / totalRuns;

            return (int) (100 * progress);
        }

    }

    public List<String> getJobs() {
        return CLASS_JOBS.get(getClass());
    }

    private static void prepareJobs(Class<? extends JobNode> type) {
        if (!CLASS_JOB_INFOS.containsKey(type)) {
            CLASS_JOB_INFOS.put(type, new HashMap<String, JobInfo>());
            Map<String, JobInfo> jobs = CLASS_JOB_INFOS.get(type);
            Method[] methods = type.getMethods();
            ClassPool pool = ClassPool.getDefault();
            pool.appendClassPath(new ClassClassPath(type));
            try {
                CtClass cc = pool.get(type.getName());
                for (Method m : methods) {
                    if (m.isAnnotationPresent(Job.class)) {
                        jobs.put(m.getName(), new JobInfo(cc.getDeclaredMethod(m.getName()), type.getName(), m.getName()));
                    }
                }
            } catch (NotFoundException ex) { /* Cannot happen */ }
            for (JobInfo info : jobs.values()) {
                info.computeRunningLength(jobs);
            }
            List<String> list = new ArrayList<>(jobs.keySet());
            Collections.sort(list);
            CLASS_JOBS.put(type, Collections.unmodifiableList(list));
        }
    }

    private Map<String, JobInfo> getJobInfos() {
        return getJobInfos(getClass().getName());
    }

    private static Map<String, JobInfo> getJobInfos(String typeName) {
        try {
            Class<?> type = PluginManager.getClassLoader().loadClass(typeName);
            if (!CLASS_JOB_INFOS.containsKey(type)) {
                return Collections.emptyMap();
            }
            return CLASS_JOB_INFOS.get(type);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }

}
