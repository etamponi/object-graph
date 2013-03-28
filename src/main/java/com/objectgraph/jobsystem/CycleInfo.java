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