package org.atzitz.graphMQ.compiler.lexer;

import org.atzitz.graphMQ.compiler.utils.Location;

public record Token(String value, TokenType type, Location loc) {
}

