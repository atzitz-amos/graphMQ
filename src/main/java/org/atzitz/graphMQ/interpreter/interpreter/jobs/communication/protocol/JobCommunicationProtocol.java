package org.atzitz.graphMQ.interpreter.interpreter.jobs.communication.protocol;


import org.atzitz.graphMQ.interpreter.interpreter.jobs.communication.JobCallingContext;
import org.atzitz.graphMQ.interpreter.interpreter.jobs.communication.JobCompletionWatcher;

/**
 * Thread safe communication protocol meant to be transmitted to all jobs
 */
public interface JobCommunicationProtocol {
    int getRunningJobsCount();

    void beginJob();

    void endJob();

    JobCompletionWatcher scheduleJob(JobCallingContext ctx);

}
