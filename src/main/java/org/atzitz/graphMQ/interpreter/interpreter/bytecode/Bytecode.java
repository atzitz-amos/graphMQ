package org.atzitz.graphMQ.interpreter.interpreter.bytecode;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;

@Getter
public class Bytecode {
    public record Node(byte name, boolean isHeavy, int stackSize, byte[] data) {
    }

    private final Collection<Node> nodes = new ArrayList<>();

    public void addNode(byte name, boolean isHeavy, int stackSize, byte[] data) {
        nodes.add(new Node(name, isHeavy, stackSize, data));
    }

}
