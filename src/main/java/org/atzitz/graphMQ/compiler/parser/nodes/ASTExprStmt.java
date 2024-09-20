package org.atzitz.graphMQ.compiler.parser.nodes;

import lombok.Getter;

@Getter
public class ASTExprStmt extends ASTNode {
     final ASTNode content;

    public ASTExprStmt(ASTNode content) {
        super(Type.ExprStmt, content.loc);
        this.content = content;
    }

    @Override
    public String toString() {
        return content.toString();
    }
}
