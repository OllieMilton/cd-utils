package cdutils.exception;

/**
 * Indicates failure to read a Compact Disc in a CDROM drive or no disc in drive.
 * 
 * @author Ollie
 *
 */
public class DiscReadException extends Exception {

	private static final long serialVersionUID = -91184480236689272L;

	public DiscReadException() {
		super();
	}
	
	public DiscReadException(String msg) {
		super(msg);
	}
}
