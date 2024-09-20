package org.atzitz.graphMQ.compiler.semantics;

import lombok.Getter;
import org.atzitz.graphMQ.compiler.parser.Parser;
import org.atzitz.graphMQ.compiler.parser.nodes.*;
import org.atzitz.graphMQ.exceptions.compile.LangCompileTimeException;

import java.util.Stack;
import java.util.function.BiConsumer;

@Getter
public class SemanticsAnalyser {

    private final ASTProgram AST;
    private final Stack<ASTScope> scopes = new Stack<>();

    public SemanticsAnalyser(ASTProgram ast) {
        AST = ast;
    }

    public static void main(String[] args) {
        Parser parser = new Parser("""
                int expensiveComputation(const int[] arr) heavy {
                    int sum = 0;
                    for each (int el of arr) {
                        sum += arr[i];
                    }
                    return sum;
                }
                                
                int getResult(const int[][] arr) heavy {
                    int sum = 0;
                    for each (int el of arr) {
                        sum += expensiveComputation(el);
                    }
                    return sum;
                }
                                
                int[10000][10000] arr = new int[10000][10000];
                for each (int[] el of arr) {
                    for each (int j of el) {
                        arr[i][j] = 1;
                    }
                }
                print(getResult(arr));""");
        parser.parse();
        SemanticsAnalyser analyser = new SemanticsAnalyser(parser.getProgram());
        analyser.analyse();
    }

    /**
     * Utils*/
    private ASTVar semanticLookup(String name) {
        for (ASTScope scope : scopes.reversed()) {
            ASTVar var = scope.lookup(name);
            if (var != null) return var;
        }
        throw new LangCompileTimeException(STR."Variable \{name} not found");
    }

    private ASTFunctionDef semanticFuncLookup(String name) {
        if (name.equals("print")) return null; // TODO
        for (ASTScope scope : scopes) {
            ASTFunctionDef func = scope.lookupFunc(name);
            if (func != null) return func;
        }
        throw new LangCompileTimeException(STR."Function \{name} not found");
    }

    private String resolveName(ASTNode id) {
        while (!(id instanceof ASTIdentifier)) {
            if (id instanceof ASTArrayAccess aa) id = aa.base;
            else if (id instanceof ASTFunctionCall fc) id = fc.id;
        }
        return ((ASTIdentifier) id).name;
    }


    private void bubbles(ASTVar var, BiConsumer<ASTScope, ASTVar> registrationFunc) {
        for (ASTScope sc : scopes.reversed()) {
            if (sc.getDeclarations().contains(var)) {
                return;
            } else {
                registrationFunc.accept(sc, var);
                if (sc.isDominant())
                    return;
            }
        }
    }

    private void closeScope() {
        ASTScope scope = scopes.pop();
        
        if (!scope.isDominant()) {
            scope.getInputs()
                    .stream()
                    .filter(x -> x.getDeclarationType() != ASTVar.ASTVarDeclarationType.LOCAL)
                    .forEach(input -> bubbles(input, ASTScope::registerInput));
            scope.getOutputs().forEach(output -> bubbles(output, ASTScope::registerOutput));
        }
    }

    /**
     * Analysis */
    public void analyse() {
        analyse(AST.getBody());
    }

    private void analyse(ASTScope scope) {
        if (!scopes.isEmpty()) scopes.peek().addChild(scope);

        scopes.push(scope);
        analyseScope(scope);
        closeScope();
    }

    private void analyseScope(ASTScope scope) {
        scope.getBody().forEach(this::analyseASTNode);
    }

    private void analyseASTNode(ASTNode node) {
        switch (node.type) {
            case Array -> {
                ((ASTArray) node).content.forEach(this::analyseASTNode);
            }
            case ArrayAccess -> {
                ASTNode base = ((ASTArrayAccess) node).base;
                while (!(base instanceof ASTIdentifier)) {
                    if (base instanceof ASTArrayAccess aa) base = aa.base;
                    else if (base instanceof ASTFunctionCall fc) base = fc.id;
                }
                analyseASTNode(base);
            }
            case AssignStmt -> {
                analyseASTNode(((ASTAssignStmt) node).getLeft());
                analyseASTNode(((ASTAssignStmt) node).getRight());
                scopes.peek().registerOutput(semanticLookup(resolveName(((ASTAssignStmt) node).getLeft())));
            }
            case BinOp -> {
                analyseASTNode(((ASTBinaryOp) node).getLeft());
                analyseASTNode(((ASTBinaryOp) node).getRight());
            }
            case Comparison -> {
                analyseASTNode(((ASTComparison) node).getLeft());
                analyseASTNode(((ASTComparison) node).getRight());
            }
            case ExprStmt -> {
                analyseASTNode(((ASTExprStmt) node).getContent());
            }
            case ForLoop -> {
                ASTScope scope = ((ASTForEachStmt) node).body;
                scope.setParent(scopes.peek());
                scopes.push(scope);
                analyseASTNode(((ASTForEachStmt) node).expr);

                ASTVar loopvar = new ASTVar(((ASTForEachStmt) node).id.name, ((ASTForEachStmt) node).varType, ASTVar.ASTVarDeclarationType.LOOPVAR, scope);
                scopes.peek().registerOutput(loopvar);

                analyseScope(scope);

                closeScope();
            }
            case FuncDef -> {
                ASTScope scope = ((ASTFunctionDef) node).getBody();
                scope.setParent(scopes.peek());
                scopes.push(scope);
                ((ASTFunctionDef) node).getParams().forEach(p -> {
                    ASTVar var = new ASTVar(p.id.name, p.type, ASTVar.ASTVarDeclarationType.PARAM, scope);
                    scopes.peek().registerInput(var);
                });
                analyseScope(scope);
                closeScope();
            }
            case FunctionCallStmt -> {
                scopes.peek().registerFuncInput(semanticFuncLookup(resolveName(((ASTFunctionCall) node).id)));
                ((ASTFunctionCall) node).params.forEach(this::analyseASTNode);
            }
            case Identifier -> {
                ASTVar var = semanticLookup(((ASTIdentifier) node).name);
                ((ASTIdentifier) node).setVar(var);
                if (var.declarationType != ASTVar.ASTVarDeclarationType.LOCAL && (var.declarationType != ASTVar.ASTVarDeclarationType.GLOBAL || scopes.peek()
                        .getType() != ASTScope.ASTScopeType.GLOBAL)) scopes.peek().registerInput(var);
            }
            case IfStmt -> {
                analyseASTNode(((ASTIfStmt) node).getExpr());
                ASTScope scope = ((ASTIfStmt) node).getBody();
                scope.setParent(scopes.peek());
                analyse(scope);
                if (((ASTIfStmt) node).getAlternative() != null) analyseASTNode(((ASTIfStmt) node).getAlternative());
            }
            case ReturnStmt -> analyseASTNode(((ASTReturnStmt) node).expr);
            case UnOp -> analyseASTNode(((ASTUnaryOp) node).getNode());
            default -> {
            }
        }
    }
}
