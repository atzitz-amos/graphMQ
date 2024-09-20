package org.atzitz.graphMQ.compiler.parser.nodes;

import java.util.Collection;
import org.atzitz.graphMQ.compiler.utils.Location;

public class ASTArray extends ASTNode {
    public final Collection<ASTNode> content;

    public ASTArray(Collection<ASTNode> content, Location loc) {
        super(Type.Array, loc);
        this.content = content;
    }

    @Override
    public String toString() {
        return STR."\{content}";
    }
}
