package org.atzitz.graphMQ.compiler.lexer;


import java.util.ArrayList;
import java.util.Collection;

public class Constants {
    public static final _Keywords KEYWORDS = new _Keywords();
    public static final _Operators OPERATORS = new _Operators();

    private static class ConstantsHolder extends ArrayList<String> {
        protected final static Collection<String> _keywords = new ArrayList<>();

        public ConstantsHolder() {
            super(_keywords);
        }

        protected static String _define(String s) {
            _keywords.add(s);
            return s;
        }
    }

    public static final class _Keywords extends ConstantsHolder {
        public static final String CONST = _define("const");
        public static final String NEW = _define("new");
        public static final String IF = _define("if");
        public static final String ELSE = _define("else");
        public static final String FOR = _define("for");
        public static final String EACH = _define("each");
        public static final String OF = _define("of");
        public static final String RETURN = _define("return");
        public static final String HEAVY = _define("heavy");
        public static final String LIGHT = _define("light");
    }

    public static final class _Operators extends ConstantsHolder {
        public static final String EQUALS = _define("=");
        public static final String DOUBLE_EQUALS = _define("==");

        public static final String PLUS = _define("+");
        public static final String MINUS = _define("-");
        public static final String MULTIPLY = _define("*");
        public static final String DIVIDE = _define("/");
        public static final String MODULO = _define("%");
    }
}
