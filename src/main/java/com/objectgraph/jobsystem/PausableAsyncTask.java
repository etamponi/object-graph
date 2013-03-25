package com.objectgraph.jobsystem;

import javafx.concurrent.Task;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.objectgraph.jobsystem.exceptions.JobNotExistsException;
import com.objectgraph.jobsystem.exceptions.TryLaterException;

public class PausableAsyncTask<V> extends Task<V> implements Pausable {
	
	private static final BiMap<Thread, PausableAsyncTask<?>> threadTaskPairs = HashBiMap.create();
	
	static <V> PausableAsyncTask<V> getTaskFromThread() {
		return (PausableAsyncTask<V>) threadTaskPairs.get(Thread.currentThread());
	}
	
	static void putThreadTaskPair(Thread t, PausableAsyncTask<?> s) {
		threadTaskPairs.put(t, s);
	}
	
	static void removeThreadTaskPair(Thread t) {
		threadTaskPairs.remove(t);
	}
	
	private Thread getThreadFromTask() {
		return threadTaskPairs.inverse().get(this);
	}
	
	private final JobNode node;
	private final String jobName;
	private final Object[] params;
	
	private boolean paused = false;
	private final Object pauseLock = new Object();
	
	public PausableAsyncTask(JobNode node, String jobName, Object... params) {
		if (!node.getJobs().contains(jobName))
			throw new JobNotExistsException(jobName + ": no such job for class " + node.getClass());
		this.node = node;
		this.jobName = jobName;
		this.params = params;
	}
	
	@Override
	public void pause() {
		if (!isRunning())
			throw new IllegalStateException("Cannot pause a task if it is not in RUNNING or SCHEDULED state");
		if (!paused && isRunning()) {
			paused = true;
			getThreadFromTask().interrupt();
		}
	}
	
	@Override
	public boolean isPaused() {
		return paused;
	}
	
	@Override
	public void waitWhilePaused() throws InterruptedException {
		while(paused) {
			synchronized (pauseLock) {
				pauseLock.wait();
			}
		}
	}
	
	@Override
	public void resume() {
		if (paused) {
			paused = false;
			synchronized(pauseLock) {
				pauseLock.notifyAll();
			}
		} else
			throw new IllegalStateException("Cannot resume a task if it is not paused");
	}

	@Override
	protected V call() throws Exception {
		Thread t = new Thread(new Runnable() {
			final Thread main = Thread.currentThread();
			@Override
			public void run() {
				try {
					while(true) {
						try {
							int progress = JobNode.getProgress(main);
							PausableAsyncTask.this.updateProgress(progress, 100);
						} catch (TryLaterException ex) {}
						Thread.sleep(5);
					}
				} catch (InterruptedException ex) {}
			}
		});
		t.start();
		try {
			updateProgress(0, 100);
			PausableAsyncTask.putThreadTaskPair(Thread.currentThread(), PausableAsyncTask.this);
			MethodAccess access = MethodAccess.get(node.getClass());
			V ret = (V)access.invoke(node, jobName, params);
			updateProgress(100, 100);
			return ret;
		} catch (Exception ex) {
			throw ex;
		} finally {
			PausableAsyncTask.removeThreadTaskPair(Thread.currentThread());
			t.interrupt();
		}
	}

	@Override
	protected void cancelled() {
		super.cancelled();
		paused = false;
	}

	@Override
	protected void failed() {
		super.failed();
		if (paused) {
			paused = false;
			synchronized(pauseLock) {
				pauseLock.notifyAll();
			}
		}
	}

	@Override
	protected void succeeded() {
		super.succeeded();
		paused = false;
	}

}
