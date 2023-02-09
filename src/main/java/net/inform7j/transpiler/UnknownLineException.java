package net.inform7j.transpiler;

import net.inform7j.transpiler.tokenizer.TokenString;

public class UnknownLineException extends RuntimeException {
    
    private static final long serialVersionUID = -6392693248233811137L;
    public final transient TokenString line;
    
    public UnknownLineException(String message, TokenString line) {
        super(message);
        this.line = line;
    }
    public UnknownLineException(String message, TokenString line, Throwable cause) {
        super(message, cause);
        this.line = line;
    }
}
