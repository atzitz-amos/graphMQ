package org.atzitz.graphMQ.compiler.parser.nodes;

import lombok.Getter;
import org.atzitz.graphMQ.compiler.utils.Location;

@Getter
public class ASTIfStmt extends ASTNode {
    private final ASTExprStmt expr;
    private final ASTScope body;
    private ASTIfStmt alternative;

    public ASTIfStmt(ASTExprStmt expr, ASTScope body, ASTIfStmt alternative, Location loc) {
        super(Type.IfStmt, loc);

        this.expr = expr;
        this.body = body;
        this.alternative = alternative;
    }

    public ASTIfStmt(ASTExprStmt expr, ASTScope body, Location loc) {
        this(expr, body, null, loc);
    }

    public ASTIfStmt alternative(ASTIfStmt stmt) {
        alternative = stmt;
        return stmt;
    }
}
