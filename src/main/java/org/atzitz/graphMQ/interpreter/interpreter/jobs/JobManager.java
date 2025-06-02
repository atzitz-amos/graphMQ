package org.atzitz.graphMQ.interpreter.interpreter.jobs;

import org.atzitz.graphMQ.interpreter.interpreter.bytecode.Bytecode;
import org.atzitz.graphMQ.interpreter.interpreter.jobs.communication.JobCallingContext;
import org.atzitz.graphMQ.interpreter.interpreter.jobs.communication.JobCompletionWatcher;

import java.util.Stack;

public class JobManager {
    private final Stack<Job> jobQueue = new Stack<>();

    public JobManager(Bytecode bytecode) {
    }

    public void run() {

    }

    public JobCompletionWatcher scheduleJob(JobCallingContext ctx) {
        Job job = Job.newIfNeeded(ctx);
        jobQueue.push(job);
        // return job.watcher();
        return null;
    }

    public Job fetchNextJob() {
        return jobQueue.isEmpty() ? null : jobQueue.pop();
    }
}
