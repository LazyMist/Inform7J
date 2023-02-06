package net.inform7j.transpiler;

public class UnknownLineException extends RuntimeException {
    
    private static final long serialVersionUID = -6392693248233811137L;
    
    public UnknownLineException() {}
    
    public UnknownLineException(String message) {
        super(message);
    }
    
    public UnknownLineException(Throwable cause) {
        super(cause);
    }
    
    public UnknownLineException(String message, Throwable cause) {
        super(message, cause);
    }
}
