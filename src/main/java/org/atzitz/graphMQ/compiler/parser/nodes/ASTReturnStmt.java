package org.atzitz.graphMQ.compiler.parser.nodes;

import org.atzitz.graphMQ.compiler.utils.Location;

public class ASTReturnStmt extends ASTNode {
    public final ASTNode expr;

    public ASTReturnStmt(ASTNode expr, Location loc) {
        super(Type.ReturnStmt, loc);
        this.expr = expr;
    }

    @Override
    public String toString() {
        return STR."ASTReturnStmt{\{expr}}";
    }
}
