package cdutils.exception;

/**
 * <p>
 * Since a CD can only be accessed linerly it is not possible for more than one thread
 * to perform any action on the disc at once. This Exception is thrown when a thread
 * attempts to perform any opperation on the disc while it is in use by another thread.
 * 
 * @author ollie
 *
 */
public class DiscInUseException extends RuntimeException {

	private static final long serialVersionUID = -4070210896953201659L;

	public DiscInUseException() {
		super("Disc is use by another process");
	}
	
}
