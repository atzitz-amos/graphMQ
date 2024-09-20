package org.atzitz.graphMQ.compiler.parser.nodes;

import org.atzitz.graphMQ.compiler.utils.Location;

public class ASTAssignStmt extends ASTBinaryOp {
    public ASTAssignStmt(ASTNode left, String ops, ASTNode right, Location loc) {
        super(left, ops, right, Type.AssignStmt, loc);
    }

    protected ASTAssignStmt(ASTNode left, String ops, ASTNode right, Type type, Location loc) {
        super(left, ops, right, type, loc);
    }
}
