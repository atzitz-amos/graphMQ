package org.atzitz.graphMQ.compiler.parser.nodes;

import org.atzitz.graphMQ.compiler.utils.Location;

public class ASTFloat extends ASTNode {
    private final int base;
    private final int floating;

    public ASTFloat(int base, int floating, Location loc) {
        super(Type.Float, loc);
        this.base = base;
        this.floating = floating;
    }

    @Override
    public String toString() {
        return STR."\{base}.\{floating}";
    }
}
