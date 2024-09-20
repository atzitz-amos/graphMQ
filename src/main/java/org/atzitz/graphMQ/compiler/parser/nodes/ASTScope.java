package org.atzitz.graphMQ.compiler.parser.nodes;

import lombok.Getter;
import lombok.Setter;
import org.atzitz.graphMQ.compiler.utils.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
public class ASTScope {
    private final List<ASTNode> body = new ArrayList<>();
    private final ASTScopeType type;

    private final List<ASTFunctionDef> functions = new ArrayList<>();
    private final List<ASTVar> declarations = new ArrayList<>();

    private final boolean isDominant;

    private final Collection<ASTVar> inputs = new ArrayList<>();
    private final Collection<ASTFunctionDef> usedFunctions = new ArrayList<>();
    private final Collection<ASTVar> outputs = new ArrayList<>();

    private final Collection<ASTScope> children = new ArrayList<>();
    private ASTScope parent = null;

    private ScopeHeaviness heaviness;

    public ASTScope(ASTScopeType type, boolean isDominant, ScopeHeaviness heaviness) {
        this.type = type;
        this.isDominant = isDominant;
        this.heaviness = heaviness;
    }

    public void registerInput(ASTVar var) {
        if (var.declaringScope == this) return;
        if (!inputs.contains(var)) inputs.add(var);
    }

    public void registerOutput(ASTVar var) {
        if (!outputs.contains(var)) outputs.add(var);
    }

    public void add(ASTNode node) {
        body.add(node);
    }

    public void addAll(Collection<ASTNode> nodes) {
        body.addAll(nodes);
    }

    public void addFunc(String name, ASTFunctionDef astFuncDef) {
        functions.add(astFuncDef);
    }

    public void addDeclaration(ASTVar var) {
        declarations.add(var);
    }

    public void registerInput(ASTVar.ASTVarDeclarationType declType, ASTScope declScope, String name, ASTVarType type) {
        registerInput(new ASTVar(name, type, declType, declScope));
    }

    public void registerOutput(ASTVar.ASTVarDeclarationType declType, ASTScope declScope, String name, ASTVarType type) {
        registerOutput(new ASTVar(name, type, declType, declScope));
    }

    public Location loc() {
        return Location.of(body.getFirst().loc, body.getLast().loc);
    }

    public void addChild(ASTScope child) {
        children.add(child);
        child.parent = this;
    }

    public ASTVar lookup(String name) {
        return declarations.stream().filter(var -> var.getName().equals(name)).findFirst().orElse(null);
    }

    public ASTFunctionDef lookupFunc(String name) {
        return functions.stream().filter(func -> func.getName().equals(name)).findFirst().orElse(null);
    }

    public void registerFuncInput(ASTFunctionDef astFuncDef) {
        usedFunctions.add(astFuncDef);
    }

    public enum ASTScopeType {
        GLOBAL, FUNC, FOR, IF;
    }

    public enum ScopeHeaviness {
        UNKNOWN, HEAVY, LIGHT;
    }
}
