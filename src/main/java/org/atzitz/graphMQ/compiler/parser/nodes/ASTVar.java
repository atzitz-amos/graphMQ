package org.atzitz.graphMQ.compiler.parser.nodes;

import lombok.Getter;

@Getter
public class ASTVar {
    public final String name;

    public ASTVarType type;
    public ASTVarDeclarationType declarationType;
    public ASTScope declaringScope;
    public int id;

    public ASTVar(String name, ASTVarType type, ASTVarDeclarationType declarationType, ASTScope declaringScope) {
        this.name = name;
        this.type = type;
        this.declarationType = declarationType;
        this.declaringScope = declaringScope;

        if (declarationType == ASTVarDeclarationType.LOCAL || declarationType == ASTVarDeclarationType.LOOPVAR)
            declaringScope.addDeclaration(this);
    }

    @Override
    public String toString() {
        return name;
    }


    public enum ASTVarDeclarationType {
        GLOBAL,
        SUPER,
        PARAM,
        LOCAL,
        LOOPVAR
    }
}
