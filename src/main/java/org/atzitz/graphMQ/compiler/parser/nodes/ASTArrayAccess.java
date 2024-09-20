package org.atzitz.graphMQ.compiler.parser.nodes;


import org.atzitz.graphMQ.compiler.utils.Location;

public class ASTArrayAccess extends ASTNode {
    public final ASTNode base;
    public final ASTNode expr;

    public ASTArrayAccess(ASTNode base, ASTNode expr, Location location) {
        super(Type.ArrayAccess, location);
        this.base = base;
        this.expr = expr;
    }

    @Override
    public String toString() {
        return STR."\{base}[\{expr}]";
    }
}
