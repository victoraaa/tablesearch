package exceptions;

public class BadSiteException extends Exception{
	public BadSiteException() {
	    super();
	}
	
	public BadSiteException(String msg) {
		super(msg);
	}
}
