package org.atzitz.graphMQ.compiler.parser.nodes;

import lombok.Getter;
import org.atzitz.graphMQ.compiler.utils.Location;

import java.util.Collection;
import java.util.stream.Collectors;

@Getter
public class ASTProgram extends ASTNode {

    private final ASTScope body;

    public ASTProgram(ASTScope body, Location loc) {
        super(Type.Program, loc);

        this.body = body;
    }

}
