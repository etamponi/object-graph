package com.objectgraph.jobsystem;

import com.objectgraph.jobsystem.exceptions.JobNotExistsException;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class PausableAsyncService<V> extends Service<V> implements Pausable {

    private final JobNode node;
    private final String jobName;
    private final Object[] params;

    private PausableAsyncTask<V> currentTask;

    public PausableAsyncService(JobNode node, String jobName, Object... params) {
        if (!node.getJobs().contains(jobName))
            throw new JobNotExistsException(jobName + ": no such job for class " + node.getClass());
        this.node = node;
        this.jobName = jobName;
        this.params = params;
    }

    @Override
    public void pause() {
        if (currentTask == null)
            throw new IllegalStateException("Cannot pause a service if it is not in RUNNING or SCHEDULED state");
        currentTask.pause();
    }

    @Override
    public boolean isPaused() {
        return currentTask == null ? false : currentTask.isPaused();
    }

    @Override
    public void waitWhilePaused() throws InterruptedException {
        if (currentTask != null)
            currentTask.waitWhilePaused();
    }

    @Override
    public void resume() {
        if (currentTask != null)
            currentTask.resume();
        else
            throw new IllegalStateException("Cannot resume a service if it is not paused");
    }

    @Override
    protected Task<V> createTask() {
        currentTask = new PausableAsyncTask<V>(node, jobName, params);
        return currentTask;
    }

    @Override
    protected void cancelled() {
        super.cancelled();
        currentTask = null;
    }

    @Override
    protected void failed() {
        super.failed();
        currentTask = null;
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        currentTask = null;
    }

}
