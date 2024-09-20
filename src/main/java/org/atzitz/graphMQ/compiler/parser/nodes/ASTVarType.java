package org.atzitz.graphMQ.compiler.parser.nodes;

import org.atzitz.graphMQ.compiler.utils.Location;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ASTVarType extends ASTIdentifier {
    public final List<Integer> values;
    public final boolean isFinal;

    public ASTVarType(String value, List<Integer> values, boolean isFinal, Location loc) {
        super(Type.VarType, value, loc);

        this.values = values;
        this.isFinal = isFinal;
    }

    @Override
    public String toString() {
        return (isFinal ? "const " : "") + super.toString() + values.stream()
                .map(x -> Objects.isNull(x) ? "[]" : STR."[\{x}]")
                .collect(Collectors.joining());
    }
}
