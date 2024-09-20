package org.atzitz.graphMQ.compiler.parser.nodes;

import org.atzitz.graphMQ.compiler.utils.Location;

import java.util.List;

public class ASTFunctionCall extends ASTNode {
    public final ASTNode id;
    public final List<ASTNode> params;

    public ASTFunctionCall(ASTNode id, List<ASTNode> params, Location location) {
        super(Type.FunctionCallStmt, location);
        this.id = id;
        this.params = params;
    }

    @Override
    public String toString() {
        return STR."\{id}(\{params})";
    }
}
