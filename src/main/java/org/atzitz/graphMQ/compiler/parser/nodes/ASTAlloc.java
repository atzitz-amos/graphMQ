package org.atzitz.graphMQ.compiler.parser.nodes;

import org.atzitz.graphMQ.compiler.utils.Location;

public class ASTAlloc extends ASTNode {
    public final ASTVarType varType;

    public ASTAlloc(ASTVarType varType, Location loc) {
        super(Type.Alloc, loc);
        this.varType = varType;
    }
}
