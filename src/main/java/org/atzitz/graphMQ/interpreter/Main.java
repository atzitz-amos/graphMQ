package org.atzitz.graphMQ.interpreter;

import org.atzitz.graphMQ.interpreter.interpreter.bytecode.BytecodeLoader;
import org.atzitz.graphMQ.interpreter.interpreter.jobs.JobManager;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: java -jar graphMQ.jar <path to bytecode.byc>");
            System.exit(1);
        }
        String bytecodePath = args[0];

        try {
            new JobManager(BytecodeLoader.load(bytecodePath)).run();
        } catch (IOException e) {
            System.err.println(STR."Error reading bytecode file: \{e.getMessage()}");
            System.exit(1);
        }
    }
}
