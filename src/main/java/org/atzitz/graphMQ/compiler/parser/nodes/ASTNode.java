package org.atzitz.graphMQ.compiler.parser.nodes;

import org.atzitz.graphMQ.compiler.utils.Location;
import org.jetbrains.annotations.NotNull;

public abstract class ASTNode {

    public final @NotNull Type type;
    public final Location loc;

    protected ASTNode(@NotNull Type type, Location loc) {
        this.type = type;
        this.loc = loc;
    }

    public enum Type {
        Program,

        ExprStmt, DeclareStmt, IfStmt,

        Number, Identifier, Float,

        BinOp, AssignStmt, FuncDef, Parameter,
        FunctionCallStmt,
        ReturnStmt, Literal, UnOp, Array, Boolean, ArrayAccess, ForLoop, VarType, Alloc, Comparison
    }

}
