package net.inform7j.transpiler;

public class ResolveException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4480364621648629188L;

	public ResolveException() {}

	public ResolveException(String message) {
		super(message);
	}

	public ResolveException(Throwable cause) {
		super(cause);
	}

	public ResolveException(String message, Throwable cause) {
		super(message, cause);
	}

}
