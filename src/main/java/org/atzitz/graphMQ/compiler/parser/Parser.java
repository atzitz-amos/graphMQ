package org.atzitz.graphMQ.compiler.parser;


import lombok.Getter;
import org.atzitz.graphMQ.compiler.lexer.Constants;
import org.atzitz.graphMQ.compiler.lexer.Token;
import org.atzitz.graphMQ.compiler.lexer.TokenType;
import org.atzitz.graphMQ.compiler.lexer.Tokenizer;
import org.atzitz.graphMQ.compiler.parser.nodes.*;
import org.atzitz.graphMQ.compiler.utils.Location;
import org.atzitz.graphMQ.exceptions.compile.LangCompileTimeException;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Parser {
    public final Tokenizer tokenizer;
    private final Stack<ASTScope> scopes = new Stack<>();
    private @Getter ASTProgram program;


    public Parser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    public Parser(String raw) {
        this.tokenizer = new Tokenizer(raw);
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
                for each (int[] i of arr) {
                    for each (int j of el) {
                        arr[i][j] = 1;
                    }
                }
                print(getResult(arr));
                """);
        parser.parse();
    }

    public void parse() {
        scopes.add(new ASTScope(ASTScope.ASTScopeType.GLOBAL, true, ASTScope.ScopeHeaviness.UNKNOWN));
        program = new ASTProgram(parseBlockStatements(), tokenizer.empty() ? Location.of(0, 0, 0, 0) : Location.of(tokenizer.getAt(0)
                .loc(), tokenizer.getLast().loc()));
    }

    // -----------------------------------------------
    // |                  UTILITIES                  |
    // -----------------------------------------------
    private Token seek(int d) {
        return tokenizer.lookAhead(d);
    }

    private Token seek() {
        return seek(1);
    }

    private Token seekNext() {
        return seek(2);
    }

    private @NotNull Token eat(TokenType type) {
        Token token = tokenizer.consume();
        if (token.type() != type) {
            if (token.type() == TokenType.SemiColon) return eat(type);
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

    private ASTIdentifier asIdentifier(Token identifier) {
        return new ASTIdentifier(identifier.value(), identifier.loc());
    }

    private ASTNode requireLineEnd(ASTNode nd) {
        if (seek().type() != TokenType.SemiColon) {
            throw new LangCompileTimeException("Missing semi-colon");
        }
        eat(TokenType.SemiColon);
        return nd;
    }

    private ASTScope openScope(ASTScope.ASTScopeType type, boolean isDominant, ASTScope.ScopeHeaviness heaviness) {
        scopes.add(new ASTScope(type, isDominant, heaviness));
        return scopes.peek();
    }

    private ASTScope openScope(ASTScope.ASTScopeType type, boolean isDominant) {
        return openScope(type, isDominant, ASTScope.ScopeHeaviness.UNKNOWN);
    }

    // -----------------------------------------------
    // |                   PARSING                   |
    // -----------------------------------------------
    private ASTScope parseBlockStatements() {
        Collection<ASTNode> result = new ArrayList<>();

        while (true) {
            Token token = seek();
            if (token.type() == TokenType.EOF) {
                if (scopes.size() != 1) throw new LangCompileTimeException("Unexpected EOF");
                break;
            } else if (consumeIf(TokenType.CloseBrace)) {
                if (scopes.isEmpty()) throw new LangCompileTimeException("Unexpected '}'");
                break;
            } else {
                ASTNode node = parseStmt();
                result.add(node);
            }
        }
        scopes.peek().addAll(result);
        return scopes.pop();
    }

    // STATEMENTS

    private ASTNode parseStmt() {
        Token token = seek();
        if (token.type() == TokenType.Keyword) {
            eat(TokenType.Keyword);
            if (Objects.equals(token.value(), Constants.KEYWORDS.IF)) {
                return parseIfStmts();
            } else if (Objects.equals(token.value(), Constants.KEYWORDS.FOR)) {
                return parseForStmt(ASTScope.ScopeHeaviness.UNKNOWN);
            } else if (Objects.equals(token.value(), Constants.KEYWORDS.HEAVY)) {
                eat(TokenType.Keyword);
                return parseForStmt(ASTScope.ScopeHeaviness.HEAVY);
            } else if (Objects.equals(token.value(), Constants.KEYWORDS.LIGHT)) {
                eat(TokenType.Keyword);
                return parseForStmt(ASTScope.ScopeHeaviness.LIGHT);
            } else if (Objects.equals(token.value(), Constants.KEYWORDS.ELSE)) {
                throw new LangCompileTimeException("Else outside of if statement");
            } else if (Objects.equals(token.value(), Constants.KEYWORDS.RETURN)) {
                return requireLineEnd(new ASTReturnStmt(parseExpr(), Location.of(token.loc(), tokenizer.lookBehind()
                        .loc())));
            } else if (Objects.equals(token.value(), Constants.KEYWORDS.CONST)) {
                return requireLineEnd(parseDeclarationStmt());
            } else {
                throw new LangCompileTimeException(STR."Unexpected keyword '\{token.value()}'");
            }

        } else if (token.type() == TokenType.Identifier) {
            if (seekNext().type() == TokenType.Identifier) {
                if (seek(3).type() == TokenType.OpenParen) return parseFunctionDefStmt();
                return requireLineEnd(parseDeclarationStmt());
            } else if (seekNext().type() == TokenType.Equals) {
                return requireLineEnd(parseAssignmentStmt());
            } else if (seekNext().type() == TokenType.OpEquals) {
                return requireLineEnd(parseOperatorAssignmentStmt());
            } else if (seekNext().type() == TokenType.OpenBracket) {
                int i;
                if ((i = seekVarType()) != -1 && seek(i).type() == TokenType.Identifier) {
                    if (seek(i).type() == TokenType.Identifier) {
                        if (seek(i + 2).type() == TokenType.OpenParen) return requireLineEnd(parseFunctionDefStmt());
                        return requireLineEnd(parseDeclarationStmt());
                    } else if (seek(i + 1).type() == TokenType.Equals) {
                        return requireLineEnd(parseAssignmentStmt());
                    } else if (seek(i + 1).type() == TokenType.OpEquals) {
                        return requireLineEnd(parseOperatorAssignmentStmt());
                    }
                }
                // Either array access or array assignment
                return requireLineEnd(parseArrayStmt());
            }
        }
        return requireLineEnd(parseExpr());
    }

    private ASTNode parseArrayStmt() {
        try {
            ASTArrayAccess base = (ASTArrayAccess) parseIdentifierGroup();
            if (consumeIf(TokenType.Equals))
                return new ASTAssignStmt(base, "=", parseExpr(), Location.of(base.loc, tokenizer.lookBehind().loc()));
            else if (seek().type() == TokenType.OpEquals) {
                return new ASTAssignStmt(base, eat(TokenType.OpEquals).value(), parseExpr(), Location.of(base.loc, tokenizer.lookBehind()
                        .loc()));
            } else {
                return base;
            }
        } catch (ClassCastException e) {
            throw new LangCompileTimeException("Syntax Error");
        }

    }

    private ASTNode parseForStmt(ASTScope.ScopeHeaviness heaviness) {
        Token begin = tokenizer.lookBehind();  // FOR
        eat(TokenType.Keyword);  // EACH
        eat(TokenType.OpenParen);

        ASTVarType varType = parseVarType();
        ASTIdentifier id = asIdentifier(eat(TokenType.Identifier));

        eat(TokenType.Keyword);  // OF
        ASTNode iterable = parseExpr();
        eat(TokenType.CloseParen);
        eat(TokenType.OpenBrace);

        openScope(ASTScope.ASTScopeType.FOR, false, heaviness);

        return new ASTForEachStmt(varType, id, iterable, parseBlockStatements(), Location.of(begin.loc(), tokenizer.lookBehind()
                .loc()));
    }

    private ASTNode parseFunctionDefStmt() {
        Token type = eat(TokenType.Identifier);
        ASTIdentifier id = asIdentifier(eat(TokenType.Identifier));

        ASTScope parentScope = scopes.peek();
        ASTScope currentScope = openScope(ASTScope.ASTScopeType.FUNC, true);

        eat(TokenType.OpenParen);
        List<ASTParameter> args = new ArrayList<>();
        while (seek().type() != TokenType.CloseParen) {
            ASTVarType tp = parseVarType();
            ASTIdentifier name = asIdentifier(eat(TokenType.Identifier));
            args.add(new ASTParameter(tp, name));
            if (!consumeIf(TokenType.Comma)) break;
        }
        eat(TokenType.CloseParen);

        if (seek().type() == TokenType.Keyword && seek().value().equals(Constants.KEYWORDS.HEAVY)) {
            eat(TokenType.Keyword);
            currentScope.setHeaviness(ASTScope.ScopeHeaviness.HEAVY);
        } else if (seek().type() == TokenType.Keyword && seek().value().equals(Constants.KEYWORDS.LIGHT)) {
            eat(TokenType.Keyword);
            currentScope.setHeaviness(ASTScope.ScopeHeaviness.LIGHT);
        }

        eat(TokenType.OpenBrace);

        ASTFunctionDef astFuncDef = new ASTFunctionDef(type.value(), id.name, args, parseBlockStatements(), Location.of(type.loc(), tokenizer.lookBehind()
                .loc()));
        parentScope.addFunc(id.name, astFuncDef);
        return astFuncDef;
    }

    private ASTNode parseAssignmentStmt() {
        ASTIdentifier id = asIdentifier(eat(TokenType.Identifier));

        eat(TokenType.Equals);
        return new ASTAssignStmt(id, "=", parseExpr(), Location.of(id.loc, tokenizer.lookBehind().loc()));
    }

    private ASTNode parseDeclarationStmt() {
        ASTVarType type = parseVarType();
        ASTIdentifier id = asIdentifier(eat(TokenType.Identifier));

        scopes.peek()
                .addDeclaration(new ASTVar(id.name, type, scopes.size() == 1 ? ASTVar.ASTVarDeclarationType.GLOBAL : ASTVar.ASTVarDeclarationType.LOCAL, scopes.peek()));

        ASTDeclareStmt result;
        if (consumeIf(TokenType.Equals)) {
            result = new ASTDeclareStmt(type, id, parseExpr(), Location.of(type.loc, tokenizer.lookBehind().loc()));
        } else result = new ASTDeclareStmt(type, id, null, Location.of(type.loc, id.loc));
        return result;
    }

    private ASTNode parseOperatorAssignmentStmt() {
        Token id = eat(TokenType.Identifier);
        return new ASTAssignStmt(asIdentifier(id), eat(TokenType.OpEquals).value(), parseExpr(), Location.of(id.loc(), tokenizer.lookBehind()
                .loc()));
    }

    private ASTIfStmt parseIfStmt() {
        Token begin = tokenizer.lookBehind();
        eat(TokenType.OpenParen);
        ASTExprStmt expr = (ASTExprStmt) parseExpr();
        eat(TokenType.CloseParen);
        eat(TokenType.OpenBrace);

        openScope(ASTScope.ASTScopeType.IF, false);
        return new ASTIfStmt(expr, parseBlockStatements(), Location.of(begin.loc(), tokenizer.lookBehind().loc()));
    }

    private ASTNode parseIfStmts() {
        ASTIfStmt stmt = parseIfStmt();
        ASTIfStmt base = stmt;

        while (seek().type() == TokenType.Keyword) {
            if (seek().value().equals(Constants.KEYWORDS.ELSE)) {
                eat(TokenType.Keyword);
                if (seek().value().equals(Constants.KEYWORDS.IF)) {
                    eat(TokenType.Keyword);
                    stmt = stmt.alternative(parseIfStmt());
                } else {
                    stmt = stmt.alternative(parseElseStmt());
                }
            } else {
                break;
            }
        }
        return base;
    }

    private ASTIfStmt parseElseStmt() {
        Token begin = eat(TokenType.OpenBrace);

        openScope(ASTScope.ASTScopeType.IF, false);
        return new ASTIfStmt(null, parseBlockStatements(), Location.of(begin.loc(), tokenizer.lookBehind().loc()));
    }

    // EXPRESSIONS

    private ASTNode parseFunctionCall(ASTIdentifier id) {
        return new ASTFunctionCall(id, parseParams(), Location.of(id.loc, tokenizer.lookBehind().loc()));
    }

    private List<ASTNode> parseParams() {
        eat(TokenType.OpenParen);
        List<ASTNode> args = new ArrayList<>();
        while (seek().type() != TokenType.CloseParen) {
            args.add(parseExpr());
            if (seek().type() != TokenType.Comma) break;
            eat(TokenType.Comma);
        }
        eat(TokenType.CloseParen);
        return args;
    }

    private ASTNode parseExpr() {
        Token begin = seek();
        ASTNode node = parseMathExpr();
        while (seek().type() == TokenType.Comparison) {
            Token tk = seek();
            eat(TokenType.Comparison);
            node = new ASTComparison(node, tk.value(), parseMathExpr(), Location.of(begin.loc(), tokenizer.lookBehind()
                    .loc()));
        }

        return new ASTExprStmt(node);
    }

    private ASTNode parseMathExpr() {
        Token begin = seek();
        ASTNode node = parseTerm();
        while (seek().type() == TokenType.Operator && (seek().value().equals(Constants.OPERATORS.PLUS) || seek().value()
                .equals(Constants.OPERATORS.MINUS))) {
            node = new ASTBinaryOp(node, eat(TokenType.Operator).value(), parseTerm(), Location.of(begin.loc(), tokenizer.lookBehind()
                    .loc()));
            begin = seek();
        }

        return node;
    }

    private ASTNode parseTerm() {
        Token begin = seek();
        ASTNode node = parseFactor();

        while (seek().type() == TokenType.Operator && (seek().value()
                .equals(Constants.OPERATORS.MULTIPLY) || seek().value()
                .equals(Constants.OPERATORS.DIVIDE)) || seek().value().equals(Constants.OPERATORS.MODULO)) {
            node = new ASTBinaryOp(node, eat(TokenType.Operator).value(), parseFactor(), Location.of(begin.loc(), tokenizer.lookBehind()
                    .loc()));
            begin = seek();
        }

        return node;
    }

    private ASTNode parseFactor() {
        Token tk = seek();
        if (tk.type() == TokenType.Number && seekNext().type() != TokenType.Dot) {
            return new ASTNumber(Integer.parseInt(tk.value()), tk.value(), eat(TokenType.Number).loc());
        } else if (tk.type() == TokenType.OpenParen) {
            eat(TokenType.OpenParen);
            ASTExprStmt expr = (ASTExprStmt) this.parseExpr();
            eat(TokenType.CloseParen);
            return expr;
        } else {
            return parseIdentifierGroup();
        }
    }

    private ASTNode parseArray() {
        Token begin = eat(TokenType.OpenBracket);
        List<ASTNode> elements = new ArrayList<>();
        while (seek().type() != TokenType.CloseBracket) {
            if (seek().type() == TokenType.EOF) {
                throw new LangCompileTimeException("Unexpected EOF in array declaration");
            }
            elements.add(parseExpr());
            if (!consumeIf(TokenType.Comma)) {
                break;
            }
        }
        return new ASTArray(elements, Location.of(begin.loc(), eat(TokenType.CloseBracket).loc()));
    }

    private ASTNode parseIdentifierGroup() {
        Token tok = seek();
        if (tok.type() == TokenType.ExclamationMark) {
            eat(TokenType.ExclamationMark);
            return new ASTUnaryOp("!", parseIdentifierGroup(), Location.of(tok.loc(), tokenizer.lookBehind().loc()));
        } else if (tok.type() == TokenType.Operator && Objects.equals(tok.value(), "-")) {
            eat(TokenType.Operator);
            return new ASTUnaryOp("-", parseIdentifierGroup(), Location.of(tok.loc(), tokenizer.lookBehind().loc()));
        } else if (tok.type() == TokenType.OpenBracket) {
            return parseArray();
        } else if (tok.type() == TokenType.Number) {
            eat(TokenType.Number);
            eat(TokenType.Dot);
            Token floating = eat(TokenType.Number);

            return new ASTFloat(Integer.parseInt(tok.value()), Integer.parseInt(floating.value()), Location.of(tok, floating));
        } else if (tok.type() == TokenType.Literal) {
            return parseLiteral();
        } else if (tok.type() == TokenType.Keyword && Objects.equals(tok.value(), Constants.KEYWORDS.NEW)) {
            eat(TokenType.Keyword);
            return new ASTAlloc(parseVarType(), Location.of(tok.loc(), tokenizer.lookBehind().loc()));
        } else {
            ASTNode result = asIdentifier(eat(TokenType.Identifier));

            while (seek().type() == TokenType.OpenBracket || seek().type() == TokenType.OpenParen) {
                result = parseSubscription(result);
            }
            return result;
        }
    }

    private ASTNode parseSubscription(ASTNode base) {
        if (seek().type() == TokenType.OpenParen) {
            eat(TokenType.OpenParen);
            List<ASTNode> params = new ArrayList<>();
            while (seek().type() != TokenType.CloseParen) {
                if (seek().type() == TokenType.EOF) {
                    throw new LangCompileTimeException("Unexpected EOF in function call");
                }
                params.add(parseExpr());
                if (!consumeIf(TokenType.Comma)) {
                    break;
                }
            }
            eat(TokenType.CloseParen);
            return new ASTFunctionCall(base, params, Location.of(base.loc, tokenizer.lookBehind().loc()));
        } else if (seek().type() == TokenType.OpenBracket) {
            eat(TokenType.OpenBracket);
            ASTNode index = parseExpr();
            eat(TokenType.CloseBracket);
            return new ASTArrayAccess(base, index, Location.of(base.loc, tokenizer.lookBehind().loc()));
        }
        return base;
    }

    private ASTVarType parseVarType() {
        boolean isFinal = false;
        if (seek().type() == TokenType.Keyword && seek().value().equals(Constants.KEYWORDS.CONST)) {
            eat(TokenType.Keyword);
            isFinal = true;
        }
        Token tok = eat(TokenType.Identifier);
        List<Integer> values = new ArrayList<>();
        while (seek().type() == TokenType.OpenBracket) {
            eat(TokenType.OpenBracket);
            if (seek().type() == TokenType.Number) {
                values.add(Integer.parseInt(eat(TokenType.Number).value()));
            } else {
                values.add(null);
            }
            eat(TokenType.CloseBracket);
        }
        return new ASTVarType(tok.value(), values, isFinal, tok.loc());
    }

    private int seekVarType() {
        int i = 1;
        if (seek(i).type() == TokenType.Keyword && seek(i).value().equals(Constants.KEYWORDS.CONST)) i++;

        if (seek(i++).type() != TokenType.Identifier) return -1;
        while (seek(i).type() == TokenType.OpenBracket) {
            if (seek(++i).type() == TokenType.Number) {
                i++;
            }
            if (seek(i++).type() != TokenType.CloseBracket) return -1;
        }
        return i;
    }

    @NotNull
    private ASTLiteral parseLiteral() {
        Token tok = eat(TokenType.Literal);
        return new ASTLiteral(tok.value(), tok.loc());
    }
}
