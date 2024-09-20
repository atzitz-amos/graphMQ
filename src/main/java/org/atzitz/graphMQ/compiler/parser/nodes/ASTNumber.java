package org.atzitz.graphMQ.compiler.parser.nodes;

import lombok.Getter;
import org.atzitz.graphMQ.compiler.utils.Location;

@Getter
public class ASTNumber extends ASTNode {
    private final int value;
    private final String raw;

    public ASTNumber(int value, String raw, Location loc) {
        super(Type.Number, loc);
        this.value = value;
        this.raw = raw;
    }

    @Override
    public String toString() {
        return raw;
    }
}
