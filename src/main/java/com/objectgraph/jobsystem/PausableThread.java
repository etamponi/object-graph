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
        while (paused) {
            synchronized (lock) {
                lock.wait();
            }
        }
    }

    public void go() {
        if (paused) {
            paused = false;
            synchronized (lock) {
                lock.notifyAll();
            }
        }
    }

}