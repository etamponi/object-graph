package com.objectgraph.jobsystem;

public interface Pausable {
	
	public void pause();
	
	public boolean isPaused();
	
	public void waitWhilePaused() throws InterruptedException;
	
	public void resume();

}
