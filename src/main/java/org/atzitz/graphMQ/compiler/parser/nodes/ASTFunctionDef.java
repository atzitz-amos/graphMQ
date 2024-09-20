package org.atzitz.graphMQ.compiler.parser.nodes;

import lombok.Getter;
import org.atzitz.graphMQ.compiler.utils.Location;

import java.util.Collection;

@Getter
public class ASTFunctionDef extends ASTNode {

    public final String returnType;
    public final String name;
    public final ASTScope body;
    private final Collection<ASTParameter> params;

    public ASTFunctionDef(String returnType, String name, Collection<ASTParameter> params, ASTScope body, Location loc) {
        super(Type.FuncDef, loc);
        this.returnType = returnType;
        this.name = name;
        this.params = params;

        this.body = body;
    }

}
