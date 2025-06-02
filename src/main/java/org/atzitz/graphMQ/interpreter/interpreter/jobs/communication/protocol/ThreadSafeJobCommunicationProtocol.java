package org.atzitz.graphMQ.interpreter.interpreter.jobs.communication.protocol;

import org.atzitz.graphMQ.interpreter.interpreter.jobs.JobManager;
import org.atzitz.graphMQ.interpreter.interpreter.jobs.communication.JobCallingContext;
import org.atzitz.graphMQ.interpreter.interpreter.jobs.communication.JobCompletionWatcher;

import java.util.concurrent.atomic.AtomicInteger;

public class ThreadSafeJobCommunicationProtocol implements JobCommunicationProtocol {

    private final AtomicInteger runningJobsCount = new AtomicInteger(0);

    private final JobManager jobManager;

    public ThreadSafeJobCommunicationProtocol(JobManager jobManager) {
        this.jobManager = jobManager;
    }


    @Override
    public int getRunningJobsCount() {
        return runningJobsCount.get();
    }

    @Override
    public void beginJob() {
        runningJobsCount.incrementAndGet();
    }

    @Override
    public void endJob() {
        runningJobsCount.decrementAndGet();
    }

    @Override
    public synchronized JobCompletionWatcher scheduleJob(JobCallingContext ctx) {
        return jobManager.scheduleJob(ctx);
    }
}
