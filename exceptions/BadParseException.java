package exceptions;

public class BadParseException extends Exception {
	public BadParseException() {
	    super();
	}
	
	public BadParseException(String msg) {
		super(msg);
	}
}
