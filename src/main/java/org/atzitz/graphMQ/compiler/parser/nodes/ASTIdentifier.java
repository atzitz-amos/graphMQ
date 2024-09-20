package org.atzitz.graphMQ.compiler.parser.nodes;

import lombok.Getter;
import lombok.Setter;
import org.atzitz.graphMQ.compiler.utils.Location;

@Getter
public class ASTIdentifier extends ASTNode {
    public final String name;
    public @Setter ASTVar var;

    public ASTIdentifier(String name, Location loc) {
        super(Type.Identifier, loc);
        this.name = name;
    }

    protected ASTIdentifier(Type type, String name, Location loc) {
        super(type, loc);
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
