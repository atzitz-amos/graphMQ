package org.atzitz.graphMQ.exceptions;

public class LangException extends RuntimeException {

    public LangException(String msg) {
        super(msg);
    }

    public LangException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public LangException(Throwable cause) {
        super(cause);
    }

    public LangException() {
        super();
    }
}
