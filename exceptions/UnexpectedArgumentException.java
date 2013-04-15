package exceptions;

public class UnexpectedArgumentException extends Exception{
	public UnexpectedArgumentException() {
	    super();
	}
	
	public UnexpectedArgumentException(String msg) {
		super(msg);
	}
}
