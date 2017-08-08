package cdutils.service;

import java.io.OutputStream;

import javax.sound.sampled.AudioInputStream;

import cdutils.domain.TOC;
import cdutils.exception.DiscReadException;

/**
 * <p>
 * This is the main entry point into the CDUtils library. A {@code CD} object 
 * represents an audio compact disc in a CDROM drive. Methods are provided 
 * for extracting audio tracks as well as retrieving the table of contents 
 * and generating id's for obtaining metadata via web services.
 * 
 * @author Ollie
 */
public interface CD {
	
	/**
	 * Gets the table of contents for the disc currently in the drive.
	 * @return The table of contents.
	 * @throws DiscReadException if there is no disc in the drive or the disc cannot be read.
	 */
	public TOC getTableOfContents() throws DiscReadException;
	
	/**
	 * Ejects the CD currently in the drive.
	 * @return True if the eject was successful.
	 */
	public boolean eject();
	
	/**
	 * Gets the CDDB id of the disc for looking up metadata from the CDDB.
	 * @return The CDDB id.
	 * @throws DiscReadException if there is no disc in the drive or the disc cannot be read.
	 */
	public String getCDDBId() throws DiscReadException;
	
	/**
	 * Gets the MusicBrainz disc id for looking up metadata via MusicBrainz web services.
	 * @return The MusicBrainz disc id.
	 * @throws DiscReadException if there is no disc in the drive or the disc cannot be read.
	 */
	public String getMusicBrainzDiscId() throws DiscReadException;
	
	/**
	 * Gets the MusicBrainz web service URL for looking up disc metadata.
	 * @return The MusicBrainz web service URL.
	 * @throws DiscReadException if there is no disc in the drive or the disc cannot be read.
	 */
	public String getMusicBrainzURL() throws DiscReadException;
	
	/**
	 * Determines whether the drive contains a disc.
	 * @return True if there is a disc in the drive. 
	 */
	public boolean isDiscInDrive();
	
	/**
	 * Gets the track with the given id from the disc as an {@code AudioInputStream}.
	 * @param track - the id of the track to get.
	 * @return An {@code AudioInputStream} containing the track.
	 * @throws DiscReadException if there is no disc in the drive or the disc cannot be read.
	 */
	public AudioInputStream getTrack(int track) throws DiscReadException;
	
	/**
	 * Gets the tracks with the given id and passes it throughtto the given output stream.
	 * @param track - the id of the track to get.
	 * @param output - the stream to pass the track through to.
	 * @throws DiscReadException if there is no disc in the drive or the disc cannot be read.
	 */
	public void getTrack(int track, OutputStream output) throws DiscReadException;
	
	/**
	 * Gets the track with the given id from the disc as an {@code AudioInputStream}.
	 * @param track - the id of the track to get.
	 * @param listener - a progress listener.
	 * @return An {@code AudioInputStream} containing the track.
	 * @throws DiscReadException if there is no disc in the drive or the disc cannot be read.
	 */
	public AudioInputStream getTrack(int track, RipProgressListener listener) throws DiscReadException;
	
	/**
	 * Gets the tracks with the given id and passes it throughtto the given output stream.
	 * @param track - the id of the track to get.
	 * @param listener - a progress listener.
	 * @param output - the stream to pass the track through to.
	 * @throws DiscReadException if there is no disc in the drive or the disc cannot be read.
	 */
	public void getTrack(int track, RipProgressListener listener, OutputStream output) throws DiscReadException;
	
	/**
	 * Cancels the current read from the disc and frees all resources.
	 */
	public void cancel();
	
	public void setVerbose();
	
	public String getLibraryVersions();
}
