package cdutils.domain;

import java.util.EventObject;

/**
 * <p>
 * The {@code ProgressEvent} encapsulates progress information that a CD sends to it's listeners
 * to indicate progress change when ripping a track.
 * 
 * @author ollie
 *
 */
public class RipProgressEvent extends EventObject {

	private static final long serialVersionUID = -2107195272187615173L;
	private final int progress;
	
	/**
	 * Constructs a new progress event with the given percentage progress.
	 * @param src - the object that this events originated from.
	 * @param progress - the current percentage progress.
	 */
	public RipProgressEvent(Object src, int progress) {
		super(src);
		this.progress = progress;
	}

	/**
	 * Obtains the current progress percentage.
	 * @return The progress percentage.
	 */
	public int getProgress() {
		return progress;
	}

}
