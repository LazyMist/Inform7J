package net.inform7j.transpiler;

public class UnresolvedKindException extends ResolveException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6540361718717468503L;

	public UnresolvedKindException() {}

	public UnresolvedKindException(String message) {
		super(message);
	}

	public UnresolvedKindException(Throwable cause) {
		super(cause);
	}

	public UnresolvedKindException(String message, Throwable cause) {
		super(message, cause);
	}

}
