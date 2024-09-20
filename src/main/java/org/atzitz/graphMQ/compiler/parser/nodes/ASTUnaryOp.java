package org.atzitz.graphMQ.compiler.parser.nodes;


import lombok.Getter;
import org.atzitz.graphMQ.compiler.utils.Location;

@Getter
public class ASTUnaryOp extends ASTNode {
    protected final String op;
    protected final ASTNode node;

    public ASTUnaryOp(String op, ASTNode node, Location loc) {
        super(Type.UnOp, loc);
        this.op = op;
        this.node = node;
    }

    public ASTUnaryOp(String op, ASTNode node, Type type, Location loc) {
        super(type, loc);
        this.op = op;
        this.node = node;
    }

    @Override
    public String toString() {
        return op + node.toString();
    }
}
