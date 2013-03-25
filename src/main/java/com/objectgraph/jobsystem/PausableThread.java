package com.objectgraph.jobsystem;

public class PausableThread extends Thread {
	private boolean paused = false;
	private final Object lock = new Object();
	
	public PausableThread(Runnable r) {
		super(r);
	}
	
	public void pause() {
		if (!paused) {
			paused = true;
			interrupt();
		}
	}
	
	public boolean isPaused() {
		return paused;
	}
	
	public void waitWhilePaused() throws InterruptedException {
		while(paused) {
			synchronized (lock) {
				lock.wait();
			}
		}
	}
	
	public void go() {
		if (paused) {
			paused = false;
			synchronized(lock) {
				lock.notifyAll();
			}
		}
	}
	
}