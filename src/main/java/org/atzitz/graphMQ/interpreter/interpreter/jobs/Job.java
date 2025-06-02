package org.atzitz.graphMQ.interpreter.interpreter.jobs;

import org.atzitz.graphMQ.interpreter.interpreter.jobs.communication.JobCallingContext;

import java.util.Stack;

public class Job {

    static Stack<Job> unusedJobs;

    public byte name;
    public MemoryHandler memoryHandlerRef;

    public int stackSize;
    public int locSize;

    public int $locPointer;
    public int $stackPointer;
    public int $argPointer;
    public int $framePointer;


    public Job(byte name, MemoryHandler memoryHandlerRef, int $locPointer, int $stackPointer, int $argPointer, int $framePointer) {
        this.name = name;
        this.memoryHandlerRef = memoryHandlerRef;
        this.$locPointer = $locPointer;
        this.$stackPointer = $stackPointer;
        this.$argPointer = $argPointer;
        this.$framePointer = $framePointer;
    }

    public void markAsUnused() {
        unusedJobs.push(this);
    }


    public static Job newIfNeeded(JobCallingContext ctx) {
        return null;
    }
}
