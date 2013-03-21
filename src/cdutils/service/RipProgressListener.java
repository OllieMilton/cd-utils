package cdutils.service;

import cdutils.domain.RipProgressEvent;

/**
 * <p>
 * Instances of classes that implements the {@code RipProgressListener} can register for
 * progress information from the {@code CD} regarding ripping a track.
 *  
 * @author ollie
 *
 */
public interface RipProgressListener {

	/**
	 * Informs the listener of a progress update.
	 * @param event - the progress event.
	 */
	public void onRipProgressEvent(RipProgressEvent event);
	
	/**
	 * Indicates that an error occurred while ripping the track.
	 * @param message - the error message.
	 */
	public void onError(String message);
}
