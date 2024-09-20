package org.atzitz.graphMQ.exceptions.compile;

import org.atzitz.graphMQ.exceptions.LangException;

public class LangCompileTimeException extends LangException {
    public LangCompileTimeException() {
        super();
    }

    public LangCompileTimeException(String msg) {
        super(msg);
    }

    public LangCompileTimeException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public LangCompileTimeException(Throwable cause) {
        super(cause);
    }
}
