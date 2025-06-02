package org.atzitz.graphMQ.compiler.utils.translator;

import org.atzitz.graphMQ.compiler.utils.Source;
import org.atzitz.graphMQ.exceptions.compile.LangCompileTimeException;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

import static java.util.Map.entry;

/**
 * Convert human-readable bytecode to bytes and the other way around
 */
public class BytecodeTranslator {

    public static List<Byte> translate(String filename) {
        BytecodeLexer lexer = new BytecodeLexer(filename);
        BytecodeParser parser = new BytecodeParser(lexer);
        return parser.parse();
    }

    public static void main(String[] args) throws IOException {
        List<Byte> result = BytecodeTranslator.translate("C:\\Users\\amosa\\OneDrive\\IdeaProjects\\graphMQ\\src\\main\\java\\org\\atzitz\\graphMQ\\compiler\\utils\\translator\\bytecode.txt");
        try (FileOutputStream outputStream = new FileOutputStream("C:\\Users\\amosa\\OneDrive\\IdeaProjects\\graphMQ\\src\\main\\java\\org\\atzitz\\graphMQ\\interpreter\\bytecode_test2.byc")) {
            byte[] bytes = new byte[result.size()];
            for (int i = 0; i < result.size(); i++) {
                bytes[i] = result.get(i);
            }
            outputStream.write(bytes);
        }
    }

    private sealed interface BytecodeNode {
    }

    private record BytecodeInstruction(byte instr) implements BytecodeNode {
    }

    private record BytecodeNodeName(String name) implements BytecodeNode {
    }

    private static final class BytecodeLexer {
        private final Source src;

        private final ArrayList<Token> tokens = new ArrayList<>();
        private int index = 0;

        public BytecodeLexer(String filename) {
            src = new Source(readFile(filename));
        }

        public String readFile(String filename) {
            try {
                String line;
                StringBuilder sb = new StringBuilder();

                BufferedReader reader = new BufferedReader(new FileReader(filename));
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                reader.close();
                return sb.toString();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private Token resolveIdentifiers() {
            if (!src.cacheEmpty()) {
                String cache = src.clearCache();
                if (Objects.equals(cache, "node") || Objects.equals(cache, "heavy") || Objects.equals(cache, "light") || Objects.equals(cache, "entrypoint")) {
                    return new Token(cache, TokenType.KEYWORD);
                } else {
                    return new Token(cache, TokenType.IDENTIFIER);
                }
            }
            return null;
        }

        private String collect(Predicate<Character> predicate) {
            StringBuilder result = new StringBuilder();
            while (!src.empty() && predicate.test(src.seek())) {
                result.append(src.consume());
            }
            return result.toString();
        }

        private Token buildNext() {
            Token result = null;

            while (result == null) {
                if (src.empty()) return new Token("", TokenType.EOF);
                char c = src.seek();
                if (c == '\n' || c == ' ') {
                    var res = this.resolveIdentifiers();
                    if (res != null) return res;
                } else if (c == '<') {
                    var res = this.resolveIdentifiers();
                    if (res != null) return res;
                    result = new Token(String.valueOf(c), TokenType.OPEN_CHEVRON);
                } else if (c == '>') {
                    var res = this.resolveIdentifiers();
                    if (res != null) return res;
                    result = new Token(String.valueOf(c), TokenType.CLOSE_CHEVRON);
                } else if (c == '*') {
                    var res = this.resolveIdentifiers();
                    if (res != null) return res;
                    result = new Token(String.valueOf(c), TokenType.STAR);
                } else if (Character.isDigit(c) && src.cacheEmpty()) {
                    var res = this.resolveIdentifiers();
                    if (res != null) return res;
                    String num = collect(Character::isDigit);
                    return new Token(num, TokenType.NUMBER);
                } else if (c == '(' || c == ')' || c == '{' || c == '}' || c == '[' || c == ']') {
                    var res = this.resolveIdentifiers();
                    if (res != null) return res;
                    result = new Token(String.valueOf(c), switch (c) {
                        case '(' -> TokenType.OPEN_PAREN;
                        case ')' -> TokenType.CLOSE_PAREN;
                        case '{' -> TokenType.OPEN_BRACE;
                        case '}' -> TokenType.CLOSE_BRACE;
                        case '[' -> TokenType.OPEN_BRACKET;
                        case ']' -> TokenType.CLOSE_BRACKET;
                        default -> throw new LangCompileTimeException("Unexpected character");
                    });
                } else if (c == ';') {
                    var res = this.resolveIdentifiers();
                    if (res != null) return res;
                    result = new Token(String.valueOf(c), TokenType.SEMICOLON);
                } else if (c == '.') {
                    var res = this.resolveIdentifiers();
                    if (res != null) return res;
                    result = new Token(String.valueOf(c), TokenType.DOT);
                } else if (c == ',') {
                    var res = this.resolveIdentifiers();
                    if (res != null) return res;
                    result = new Token(String.valueOf(c), TokenType.COMMA);
                } else if (c == ':') {
                    var res = this.resolveIdentifiers();
                    if (res != null) return res;
                    result = new Token(String.valueOf(c), TokenType.COLON);
                } else if (c == '-') {
                    var res = this.resolveIdentifiers();
                    if (res != null) return res;
                    result = new Token(String.valueOf(c), TokenType.DASH);
                } else if (c == '$') {
                    var res = this.resolveIdentifiers();
                    if (res != null) return res;
                    result = new Token(String.valueOf(c), TokenType.DOLLAR);
                } else if (c == '#') {
                    var res = this.resolveIdentifiers();
                    if (res != null) return res;
                    result = new Token(String.valueOf(c), TokenType.HASHTAG);
                } else if (c == '\'') {
                    var res = this.resolveIdentifiers();
                    if (res != null) return res;
                    int n = src.find(c);
                    if (n == -1) throw new LangCompileTimeException("No closing string quote found");
                    result = new Token(src.slice(n), TokenType.STRING);
                    src.jump(n + 1);
                } else src.save();

                src.consume();
            }
            return result;
        }

        private void pushNext() {
            tokens.add(buildNext());
        }

        public Token consume() {
            if (index >= tokens.size()) {
                pushNext();
            }

            return tokens.get(index++);
        }

        public Token lookAhead(int d) {
            while (index + d > tokens.size()) {
                pushNext();
            }
            return tokens.get(index + d - 1);
        }

        public Token lookAhead() {
            return lookAhead(1);
        }

        public Token lookBehind() {
            return tokens.get(index - 1);
        }

        public Token getAt(int i) {
            while (i >= tokens.size()) {
                pushNext();
            }
            return tokens.get(i);
        }

        public Token getLast() {
            return tokens.getLast();
        }

        public boolean empty() {
            return tokens.isEmpty();
        }

    }

    private static final class BytecodeParser {
        private static final Map<String, String> LSb_BYTECODES = Map.ofEntries(
                entry("PUSH", "00000"),
                entry("STORE", "00001"),
                entry("INEG", "00010"),
                entry("BNEG", "00011"),
                entry("IADD", "00100"),
                entry("ISUB", "00101"),
                entry("IMUL", "00110"),
                entry("IDIV", "00111"),
                entry("IPADD", "01100"),
                entry("IPSUB", "01101"),
                entry("IPMUL", "01110"),
                entry("IPDIV", "01111"),
                entry("CMP_GT", "10001"),
                entry("CMP_EQ", "10010"),
                entry("CMP_GE", "10011"),
                entry("CMP_LT", "10100"),
                entry("CMP_NQ", "10101"),
                entry("CMP_LE", "10110"),
                entry("ARRACCESS", "11001"),
                entry("ARRSTORE", "11010"),
                entry("ARRLENGTH", "11011"));

        private final BytecodeLexer lexer;
        private final Map<String, Byte> nodeNames = new HashMap<>();
        private List<BytecodeNode> currentNode;

        public BytecodeParser(BytecodeLexer lexer) {
            this.lexer = lexer;
        }

        // -----------------------------------------------
        // |                  UTILITIES                  |
        // -----------------------------------------------
        private Token seek(int d) {
            return lexer.lookAhead(d);
        }

        private Token seek() {
            return seek(1);
        }

        private Token seekNext() {
            return seek(2);
        }

        private @NotNull Token eat(TokenType type) {
            Token token = lexer.consume();
            if (token.type() != type) {
                if (token.type() == TokenType.SEMICOLON) return eat(type);
                throw new LangCompileTimeException(STR."Expected '\{type}' but received instead '\{token.type()}'");
            }
            return token;
        }

        private boolean consumeIf(TokenType type) {
            if (seek().type() == type) {
                eat(type);
                return true;
            }
            return false;
        }

        private Byte toByte(String value) {
            return (byte) Integer.parseInt(value, 2);
        }

        private void addToCurrentNode(byte node) {
            currentNode.add(new BytecodeInstruction(node));
        }

        private void addToCurrentNode(int index, byte node) {
            currentNode.add(index, new BytecodeInstruction(node));
        }
        // -----------------------------------------------
        // |                   PARSING                   |
        // -----------------------------------------------

        public List<Byte> parse() {
            List<BytecodeNode> result = new ArrayList<>();

            int i = 0;
            while (seek().type() != TokenType.EOF) {
                currentNode = new ArrayList<>();

                String name = parseNode();
                int id = Objects.equals(name, "$entrypoint$") ? 0 : i++;
                nodeNames.put(name, (byte) id);

                result.add(new BytecodeInstruction((byte) id));
                result.add(new BytecodeInstruction((byte) currentNode.size()));
                result.addAll(currentNode);
            }

            return result.stream().map(node -> {
                if (node instanceof BytecodeInstruction instr) {
                    return instr.instr();
                }
                if (node instanceof BytecodeNodeName name) {
                    return nodeNames.get(name.name());
                }
                throw new RuntimeException("Should not happen");
            }).toList();
        }


        private String parseNode() {
            String name;

            Token keyword = eat(TokenType.KEYWORD);
            boolean isHeavy = false;
            if (seek().type() == TokenType.KEYWORD) {
                isHeavy = keyword.value().equals("heavy");
                keyword = eat(TokenType.KEYWORD);
            }
            addToCurrentNode(toByte(isHeavy ? "1" : "0"));
            if (keyword.value().equals("node")) {
                name = eat(TokenType.STRING).value();
                parseNodeDef();
                parseHeader();
                while (!consumeIf(TokenType.CLOSE_BRACE)) {
                    parseLine();
                }
            } else if (keyword.value().equals("entrypoint")) {
                name = "$entrypoint$";
                eat(TokenType.OPEN_BRACE);
                parseHeader();
                while (!consumeIf(TokenType.CLOSE_BRACE)) {
                    parseLine();
                }
            } else {
                throw new LangCompileTimeException("Unexpected keyword");
            }
            return name;
        }

        private void parseNodeDef() {
            while (!consumeIf(TokenType.OPEN_BRACE)) {
                if (seek().type() == TokenType.EOF) throw new LangCompileTimeException("Unexpected EOF");
                lexer.consume();
            }
        }

        private void parseHeader() {
            Byte[] headers = new Byte[2];

            while (seek().type() != TokenType.DASH) {
                eat(TokenType.HASHTAG);
                String key = eat(TokenType.IDENTIFIER).value();
                if (Objects.equals(key, "stack_size"))
                    headers[0] = (byte) (Integer.parseInt(eat(TokenType.NUMBER).value()));
                else if (Objects.equals(key, "local_size"))
                    headers[1] = (byte) (Integer.parseInt(eat(TokenType.NUMBER).value()));
                else throw new LangCompileTimeException("Unexpected header");
                eat(TokenType.SEMICOLON);
            }

            while (seek().type() == TokenType.DASH) {
                eat(TokenType.DASH);
            }

            addToCurrentNode(headers[0]);
            addToCurrentNode(headers[1]);
        }

        private int parseLine() {
            int numParam = 0;

            int offset = Integer.parseInt(eat(TokenType.NUMBER).value());
            eat(TokenType.COLON);
            String instruction = eat(TokenType.IDENTIFIER).value().toUpperCase();

            String bytecode;

            if (LSb_BYTECODES.containsKey(instruction)) {
                bytecode = parseLSbInstruction(LSb_BYTECODES.get(instruction));
                addToCurrentNode(toByte(bytecode));
                if (bytecode.endsWith("000") || bytecode.endsWith("111")) {
                    Token value = eat(TokenType.NUMBER);
                    addToCurrentNode((byte) (Integer.parseInt(value.value()) & 0xFF));
                    addToCurrentNode((byte) ((Integer.parseInt(value.value()) >>> 8) & 0xFF));
                    numParam += 2;
                }
            } else {
                switch (instruction) {
                    case "U_EFOR":
                    case "O_EFOR":
                        return parseEFORInstruction(instruction.charAt(0), offset);
                    case "U_IFOR":
                    case "O_IFOR":
                        return parseIFORInstruction(instruction.charAt(0), offset);
                    case "I_MAKEARRAY":
                    case "B_MAKEARRAY":
                    case "F_MAKEARRAY":
                    case "R_MAKEARRAY":
                        bytecode = STR."110000\{switch (instruction.charAt(0)) {
                            case 'I' -> "00";
                            case 'B' -> "01";
                            case 'F' -> "10";
                            default -> "11";
                        }}";
                        break;
                    case "RET":
                    case "AWAIT":
                    case "ASYNCCALL":
                    case "JOIN":
                        bytecode = STR."100000\{switch (instruction) {
                            case "RET" -> "00";
                            case "AWAIT" -> "01";
                            case "ASYNCCALL" -> "10";
                            default -> "11";
                        }}";
                        break;
                    default:
                        throw new LangCompileTimeException(STR."Unexpected instruction: \{instruction}");
                }
                addToCurrentNode(toByte(bytecode));
            }
            while (!consumeIf(TokenType.SEMICOLON)) {
                if (seek().type() == TokenType.EOF) throw new LangCompileTimeException("Missing semicolon");
                if (seek().type() == TokenType.STRING) {
                    currentNode.add(new BytecodeNodeName(eat(TokenType.STRING).value()));
                } else {
                    addToCurrentNode((byte) Integer.parseInt(eat(TokenType.NUMBER).value()));
                }
                consumeIf(TokenType.CLOSE_CHEVRON);
                numParam++;
            }

            for (int i = numParam; i < 3; i++) {
                addToCurrentNode((byte) 0);
            }

            return offset;
        }

        private int parseIFORInstruction(char instruction, int offset) {
            eat(TokenType.OPEN_BRACKET);
            int index = currentNode.size() - 1;

            int offset2 = offset;
            while (!consumeIf(TokenType.CLOSE_BRACKET)) {
                offset2 = parseLine();
            }
            eat(TokenType.OPEN_BRACE);
            int offset3 = offset;
            while (!consumeIf(TokenType.CLOSE_BRACE)) {
                offset3 = parseLine();
            }
            addToCurrentNode(index, toByte(STR."0100\{instruction == 'U' ? "0" : "1"}111"));
            addToCurrentNode(index + 1, (byte) offset3);
            addToCurrentNode(index + 2, (byte) (offset2 - offset));
            addToCurrentNode(index + 3, (byte) 0);


            return offset3;
        }

        private int parseEFORInstruction(char instruction, int offset) {
            int index = currentNode.size() - 1;

            eat(TokenType.OPEN_BRACE);
            int offset2 = offset;
            while (!consumeIf(TokenType.CLOSE_BRACE)) {
                offset2 = parseLine();
            }
            addToCurrentNode(index, toByte(STR."0101\{instruction == 'U' ? "0" : "1"}111"));
            addToCurrentNode(index + 1, (byte) offset2);
            addToCurrentNode(index + 2, (byte) 0);
            addToCurrentNode(index + 3, (byte) 0);

            return offset2;
        }

        @NotNull
        private String parseLSbInstruction(String instr) {
            if (consumeIf(TokenType.HASHTAG)) {
                return STR."\{instr}001";
            } else if (consumeIf(TokenType.OPEN_CHEVRON)) {
                return STR."\{instr}010";
            } else if (consumeIf(TokenType.STAR)) {
                return STR."\{instr}011";
            } else if (consumeIf(TokenType.DOLLAR)) {
                return STR."\{instr}100";
            } else if (consumeIf(TokenType.DOT)) {
                if (consumeIf(TokenType.DOT)) return STR."\{instr}101";
                else return STR."\{instr}110";
            } else if (consumeIf(TokenType.DASH)) {
                eat(TokenType.CLOSE_CHEVRON);
                return STR."\{instr}111";
            } else {
                return STR."\{instr}000";
            }
        }
    }
}
