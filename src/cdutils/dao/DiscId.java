package cdutils.dao;

import cdutils.exception.DiscReadException;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

/**
 * <p>
 * A wrapper class around the discid c library.
 * 
 * @author Ollie
 *
 */
public class DiscId {

	/**
	 * An interface to the native discid API
	 */
	private interface LibdiscIdAPI extends Library {
				
		/**
		 * Return a handle for a new DiscId object.
		 *
		 * If no memory could be allocated, NULL is returned. Don't use the created
		 * DiscId object before calling discid_read() or discid_put().
		 *
		 * @return a DiscId object, or NULL.
		 */
		Pointer discid_new();
		
		/**
		 * Release the memory allocated for the DiscId object.
		 *
		 * @param d a DiscId object created by discid_new()
		 */
		void discid_free(Pointer disc);
		
		/**
		 * Read the disc in the given CD-ROM/DVD-ROM drive.
		 *
		 * This function reads the disc in the drive specified by the given device
		 * identifier. If the device is NULL, the default drive, as returned by
		 * discid_get_default_device() is used.
		 *
		 * On error, this function returns false and sets the error message which you
		 * can access using discid_get_error_msg(). In this case, the other functions
		 * won't return meaningful values and should not be used.
		 *
		 * This function may be used multiple times with the same DiscId object.
		 *
		 * @param d a DiscId object created by discid_new()
		 * @param device an operating system dependent device identifier, or NULL
		 * @return true if successful, or false on error.
		 */
		int discid_read(Pointer disc, String device);
		
		/**
		 * Provides the TOC of a known CD.
		 *
		 * This function may be used if the TOC has been read earlier and you
		 * want to calculate the disc ID afterwards, without accessing the disc
		 * drive. It replaces the discid_read function in this case.
		 *
		 * On error, this function returns false and sets the error message which you
		 * can access using discid_get_error_msg(). In this case, the other functions
		 * won't return meaningful values and should not be used.
		 *
		 * The offsets parameter points to an array which contains the track offsets
		 * for each track. The first element, offsets[0], is the leadout track. It
		 * must contain the total number of sectors on the disc.
		 *
		 * @param d a DiscID object created by discid_new()
		 * @param first the number of the first audio track on disc (usually one)
		 * @param last the number of the last audio track on the disc
		 * @param offsets a pointer to an array of 100 track offsets
		 * @return true if the given data was valid, and false on error
		 */
		int discid_put(Pointer d, int first, int last, int[] offsets);
		
		/**
		 * Return a human-readable error message.
		 *
		 * This function may only be used if discid_read() failed. The returned
		 * error message is only valid as long as the DiscId object exists.
		 *
		 * @param d a DiscId object created by discid_new()
		 * @return a string describing the error that occurred
		 */
		String discid_get_error_msg(Pointer d);
		
		/**
		 * Return a MusicBrainz DiscID.
		 *
		 * The returned string is only valid as long as the DiscId object exists.
		 *
		 * @param d a DiscId object created by discid_new()
		 * @return a string containing a MusicBrainz DiscID
		 */
		String discid_get_id(Pointer d);
		
		/**
		 * Return a FreeDB DiscID.
		 *
		 * The returned string is only valid as long as the DiscId object exists.
		 *
		 * @param d a DiscId object created by discid_new()
		 * @return a string containing a FreeDB DiscID
		 */
		String discid_get_freedb_id(Pointer disc);
		
		/**
		 * Return an URL for submitting the DiscID to MusicBrainz.
		 *
		 * The URL leads to an interactive disc submission wizard that guides the
		 * user through the process of associating this disc's DiscID with a
		 * release in the MusicBrainz database.
		 *
		 * The returned string is only valid as long as the DiscId object exists.
		 *
		 * @param d a DiscId object created by discid_new()
		 * @return a string containing an URL
		 */
		String discid_get_submission_url(Pointer d);
		
		/**
		 * Return an URL for retrieving CD information from MusicBrainz' web service
		 *
		 * The URL provides the CD information in XML. 
		 * See http://musicbrainz.org/development/mmd for details.
		 *
		 * The returned string is only valid as long as the DiscId object exists.
		 *
		 * @param d a DiscId object created by discid_new()
		 * @return a string containing an URL
		 */
		String discid_get_webservice_url(Pointer d);
		
		/**
		 * Return the name of the default disc drive for this operating system.
		 *
		 * @return a string containing an operating system dependent device identifier
		 */
		String discid_get_default_device();
		
		/**
		 * Return the number of the first track on this disc.
		 *
		 * @param d a DiscId object created by discid_new()
		 * @return the number of the first track
		 */
		int discid_get_first_track_num(Pointer d);
		
		/**
		 * Return the number of the last track on this disc.
		 *
		 * @param d a DiscId object created by discid_new()
		 * @return the number of the last track
		 */
		int discid_get_last_track_num(Pointer d);
		
		/**
		 * Return the length of the disc in sectors.
		 *
		 * @param d a DiscId object created by discid_new()
		 * @return the length of the disc in sectors
		 */
		int discid_get_sectors(Pointer d);
		
		/**
		 * Return the sector offset of a track.
		 *
		 * Only track numbers between (and including) discid_get_first_track_num()
		 * and discid_get_last_track_num() may be used.
		 *
		 * @param d a DiscId object created by discid_new()
		 * @param track_num the number of a track
		 * @return sector offset of the specified track
		 */
		int discid_get_track_offset(Pointer d, int track_num);
		
		/**
		 * Return the length of a track in sectors.
		 *
		 * Only track numbers between (and including) discid_get_first_track_num()
		 * and discid_get_last_track_num() may be used.
		 *
		 * @param d a DiscId object created by discid_new()
		 * @param track_num the number of a track
		 * @return length of the specified track
		 */
		int discid_get_track_length(Pointer d, int track_num);
	}
	
	private LibdiscIdAPI lib;
	private String device;
	
	/**
	 * Creates a new {@code DiscId} object using the given CDROM drive locator.
	 * @param device - the locator of the CDROM drive.
	 */
	public DiscId(String device) {
		lib = (LibdiscIdAPI) Native.loadLibrary("discid", LibdiscIdAPI.class);
		this.device = device;
	}
	
	/**
	 * Reads the disc and return a pointer to the drive.
	 * @return A Pointer to the drive.
	 * @throws DiscReadException - if there is no disc in the drive.
	 */
	private Pointer readDisc() throws DiscReadException {
		Pointer disc = lib.discid_new(); 
		if (lib.discid_read(disc, device) == 0) {
			String error = lib.discid_get_error_msg(disc);
			lib.discid_free(disc);
			throw new DiscReadException(error);
		}
		return disc;
	}
	
	/**
	 * Gets the MusicBrainz disc id for looking up metadata via MusicBrainz web services.
	 * @return The MusicBrainz disc id.
	 * @throws DiscReadException - if there is no disc in the drive of the disc cannot be read.
	 */
	public String getMusicBrainzDiscId() throws DiscReadException {
		Pointer disc = readDisc();
		String id = lib.discid_get_id(disc);
		lib.discid_free(disc);
		return id;
	}
	
	/**
	 * Gets the CDDB id of the disc for looking up metadata from the CDDB.
	 * @return The CDDB id.
	 * @throws DiscReadException - if there is no disc in the drive of the disc cannot be read.
	 */
	public String getFreeDdId() throws DiscReadException {
		Pointer disc = readDisc();
		String id = lib.discid_get_freedb_id(disc);
		lib.discid_free(disc);
		return id;
	}
	
	/**
	 * Gets the MusicBrainz web service URL for looking up disc metadata.
	 * @return The MusicBrainz web service URL.
	 * @throws DiscReadException - if there is no disc in the drive of the disc cannot be read.
	 */
	public String getMusicBrainzURL() throws DiscReadException {
		Pointer disc = readDisc();
		String url = lib.discid_get_webservice_url(disc);
		lib.discid_free(disc);
		return url;
	}
}
