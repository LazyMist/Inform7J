package net.inform7j.transpiler;

public class NoSuchKindException extends ResolveException {
    
    /**
     *
     */
    private static final long serialVersionUID = 2415512325985189379L;
    
    public NoSuchKindException() {}
    
    public NoSuchKindException(String message) {
        super(message);
    }
    
    public NoSuchKindException(Throwable cause) {
        super(cause);
    }
    
    public NoSuchKindException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
