package org.atzitz.graphMQ.compiler.parser.nodes;

import lombok.Getter;
import org.atzitz.graphMQ.compiler.utils.Location;

@Getter
public class ASTDeclareStmt extends ASTNode {
    private final ASTIdentifier vartype;
    private final ASTIdentifier name;
    private final ASTNode init;

    public ASTDeclareStmt(ASTIdentifier type, ASTIdentifier name, ASTNode init, Location loc) {
        super(Type.DeclareStmt, loc);
        this.vartype = type;
        this.name = name;
        this.init = init;
    }

    @Override
    public String toString() {
        return STR."[\{loc}] \{vartype} \{name} = \{init}";
    }
}
