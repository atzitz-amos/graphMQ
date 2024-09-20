package org.atzitz.graphMQ.compiler.parser.nodes;

import org.atzitz.graphMQ.compiler.utils.Location;

public class ASTForEachStmt extends ASTNode {
    public final ASTVarType varType;
    public final ASTIdentifier id;
    public final ASTNode expr;
    public final ASTScope body;

    public ASTForEachStmt(ASTVarType type, ASTIdentifier id, ASTNode expr, ASTScope body, Location loc) {
        super(Type.ForLoop, loc);
        varType = type;
        this.id = id;
        this.expr = expr;
        this.body = body;
    }
}
