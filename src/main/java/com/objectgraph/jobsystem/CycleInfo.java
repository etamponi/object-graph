package com.objectgraph.jobsystem;

class CycleInfo {
	private final int position;
	private int length;
	private int currentRun, totalRuns;
	private int runningPosition, runningLength;
	private String description;
	
	public CycleInfo(int position) {
		this.position = position;
	}

	public int getCodeLength() {
		return length;
	}

	public void setCodeLength(int length) {
		this.length = length;
	}

	public int getCurrentRun() {
		return currentRun;
	}

	private void setCurrentRun(int currentRun) {
		this.currentRun = currentRun;
	}

	public int getTotalRuns() {
		return totalRuns;
	}

	private void setTotalRuns(int totalRuns) {
		this.totalRuns = totalRuns;
	}
	
	public CycleInfo instantiate(int currentRun, int totalRuns, String description) {
		CycleInfo ret = this.copy();
		ret.setCurrentRun(currentRun);
		ret.setTotalRuns(totalRuns);
		ret.setDescription(description);
		return ret;
	}

	private void setDescription(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}

	public int getCodePosition() {
		return position;
	}
	
	public int getRunningPosition() {
		return runningPosition;
	}
	
	public void setRunningPosition(int runningPosition) {
		this.runningPosition = runningPosition;
	}
	
	public int getRunningLength() {
		return runningLength;
	}

	public void setRunningLength(int runningLength) {
		this.runningLength = runningLength;
	}

	private CycleInfo copy() {
		CycleInfo ret = new CycleInfo(position);
		ret.setCodeLength(length);
		ret.setRunningLength(runningLength);
		ret.setRunningPosition(runningPosition);
		return ret;
	}
}