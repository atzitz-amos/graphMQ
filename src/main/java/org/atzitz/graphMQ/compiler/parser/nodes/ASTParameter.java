package org.atzitz.graphMQ.compiler.parser.nodes;

import org.atzitz.graphMQ.compiler.utils.Location;

public class ASTParameter extends ASTNode {
    public final ASTVarType type;
    public final ASTIdentifier id;

    public ASTParameter(ASTVarType type, ASTIdentifier id) {
        super(Type.Parameter, Location.of(type.loc, id.loc));
        this.type = type;
        this.id = id;
    }

    @Override
    public String toString() {
        return STR."\{type} \{id}";
    }
}
