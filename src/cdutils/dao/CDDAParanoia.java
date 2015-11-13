package cdutils.dao;

import cdutils.exception.DiscReadException;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

/**
 * <p>
 * A low level java wrapper around the cdda_paranoia and cdda_interface native libraries.
 * 
 * @author ollie
 *
 */
public class CDDAParanoia {

	public static final int cddaFrameSize = 2352;
	private final int maxRetries = 20;
	private final int PARANOIA_MODE_FULL = 0xff;
	private final int PARANOIA_MODE_NEVER_SKIP = 0x20;
	private final int MODE = PARANOIA_MODE_FULL^PARANOIA_MODE_NEVER_SKIP;
	private final int SEEK_SET = 0;
	
	/**
	 * Wrapper interface for the native CDDAParanoia library.
	 */
	private interface LibCDDAParanoiaAPI extends Library {
		void paranoia_modeset(Pointer cdpar, int paranoiaMode);
		Pointer paranoia_init(Pointer cdpar);
		String paranoia_version();
		long paranoia_seek(Pointer cdpar,long seek,int mode);
		Pointer paranoia_read_limited(Pointer cdrom, Pointer callback, int max_retries);
		void paranoia_free(Pointer cdpar);
	}
	
	/**
	 * Wrapper interface for the native CDDA library.
	 */
	private interface LibCDDAInterfaceAPI extends Library {
		String cdda_version();
		Pointer cdda_identify(String device, int messagedest, String message);
		Pointer cdda_find_a_cdrom(int messagedest, String message);
		int cdda_close(Pointer cdrom);
		long cdda_tracks(Pointer cdrom);
		long cdda_track_firstsector(Pointer cdrom, int track);
		long cdda_track_lastsector(Pointer cdrom, int track);
		String cdda_messages(Pointer cdrom);
		String cdda_errors(Pointer cdrom);
		void close();
		void cdda_verbose_set(Pointer cdrom, int err_action, int mes_action);
		int cdda_open(Pointer cdrom);
		int cdda_track_copyp(Pointer cdrom, int track);
		int cdda_track_channels(Pointer cdrom, int track);
		int cdda_track_preemp(Pointer cdrom, int track);
		int cdda_speed_set(Pointer cdrom, int speed);
		int cdda_track_audiop(Pointer cdrom,int track);
	}
	
	private final LibCDDAParanoiaAPI libpara;
	private final LibCDDAInterfaceAPI libcdda;
	private Pointer cdpar;
	private Pointer cdrom;
	private int verbose = 0;
	
	public CDDAParanoia() {
		libpara = (LibCDDAParanoiaAPI) Native.loadLibrary("cdda_paranoia", LibCDDAParanoiaAPI.class);
		libcdda = (LibCDDAInterfaceAPI) Native.loadLibrary("cdda_interface", LibCDDAInterfaceAPI.class);
	}
		
	/**
	 * Gets the version numbers of the libraries.
	 * @return Library version.
	 */
	public String getVersion() {
		return "Para-"+libpara.paranoia_version()+" CDDA-"+libcdda.cdda_version();
	}
	
	/**
	 * Opens the disc using the given drive locator.
	 * @param device - the string identifying the drive.
	 * @return True is successful.
	 * @throws DiscReadException - if there is no disc in the drive or we were unable to read the disc.
	 */
	public void open(String device) throws DiscReadException {
		cdrom = libcdda.cdda_identify(device, verbose, null);
		if (cdrom == null) {
			throw new DiscReadException("CDROM drive ["+device+"] not found.");
		}
		openCdrom();
	}
	
	/**
	 * Searches for a drive containing an audio CD and opens it. 
	 * @return True is successful.
	 * @throws DiscReadException - if there is no disc in the drive or we were unable to read the disc.
	 */
	public void open() throws DiscReadException {
		cdrom = libcdda.cdda_find_a_cdrom(verbose, null);
		if (cdrom == null) {
			throw new DiscReadException("No CDROM drive found.");
		}
		openCdrom();
	}
	
	private void openCdrom() throws DiscReadException {
		if (cdrom != null) {
			libcdda.cdda_verbose_set(cdrom, verbose, verbose);
			switch(libcdda.cdda_open(cdrom)){
			  case -2:case -3:case -4:case -5:
				  throw new DiscReadException("Unable to open disc. Is there an audio CD in the drive?");
			  case -6:
				  throw new DiscReadException("Could not find a way to read audio from this drive.");
			  case 0:
				  break;
			  default:
				  throw new DiscReadException("Unable to open disc.");
			}
			cdpar = libpara.paranoia_init(cdrom);
			if (cdpar == null) {
				throw new DiscReadException("Unable to initialise the cd paranoia library.");
			} else {
				libpara.paranoia_modeset(cdpar, MODE);
			}
		} 
	}
	
	/**
	 * Closes the disc and frees any resources.
	 */
	public void close() {
		if (cdrom != null) {
			libcdda.cdda_close(cdrom);
			cdrom = null;
		}
		if (cdpar != null) {
			libpara.paranoia_free(cdpar);
			cdpar = null;
		}
	}
	
	/**
	 * Gets the total number of tracks on the disc.
	 * NOTE, this will return all tracks including non audio tracks.
	 * @return The total number of tracks.
	 */
	public int getTracks() {
		return (int) libcdda.cdda_tracks(cdrom);
	}
	
	/**
	 * Gets the first sector of the disc for the given track.
	 * @param track - the track number.
	 * @return The tracks first sector.
	 */
	public int getFirstSector(int track) {
		return (int) libcdda.cdda_track_firstsector(cdrom, track);
	}
	
	/**
	 * Gets the last sector of the disc for the given track.
	 * @param track - the track number.
	 * @return The tracks last sector.
	 */
	public int getLastSector(int track) {
		return (int) libcdda.cdda_track_lastsector(cdrom, track);
	}
	
	/**
	 * Gets any messages that occurred during the last read.
	 * @return Read messages.
	 */
	public String getMessage() {
		return libcdda.cdda_messages(cdrom);
	}
	
	/**
	 * Gets any error that occurred during the last read.
	 * @return Error messages.
	 */
	public String getErrors() {
		return libcdda.cdda_errors(cdrom);
	}
	
	/**
	 * Sets the position to start reading from.
	 * @param seekSector - the sector to start reading from.
	 * @return
	 */
	public long seek(long seekSector) {
		return libpara.paranoia_seek(cdpar, seekSector, SEEK_SET);
	}
	
	/**
	 * Reads the next frame from the current read position.
	 * @return The next frame of data.
	 */
	public byte[] readNextFrame() throws DiscReadException {
		Pointer data = libpara.paranoia_read_limited(cdpar, null, maxRetries);
		if (data == null) {
			throw new DiscReadException("Cannot read disc");
		}
		return data.getByteArray(0, cddaFrameSize);
	}
	
	/**
	 * Determines whether copy is permitted on the given track.
	 * @param track - the id of the track to check.
	 * @return True if copy is permitted.
	 */
	public boolean copyPermitted(int track) {
		int i = libcdda.cdda_track_copyp(cdrom, track);
		return i == 1;
	}
	
	/**
	 * Determines the number of channels the given track has.
	 * @param track - the id of the track to check.
	 * @return The number of channels.
	 */
	public int getChannels(int track) {
		return libcdda.cdda_track_channels(cdrom, track);
	}
	
	/**
	 * Determines whether the given track was encoded with preemphasis.
	 * @param track - the id of the track to check.
	 * @return True if the track was encoded with preemphasis.
	 */
	public boolean linearPreemphasis(int track) {
		int i = libcdda.cdda_track_preemp(cdrom, track);
		return i == 1;
	}
	
	/**
	 * Sets the speed at which to read the disc.
	 * @param speed - the disc speed (-1 for full speed).
	 * @return True if the speed was set successfully.
	 */
	public boolean setSpeed(int speed) {
		int i = libcdda.cdda_speed_set(cdrom, speed);
		return i == 1;
	}
	
	/**
	 * Determines whether the given track is an audio track.
	 * @param track the track to check.
	 * @return True if the track is an audio track.
	 */
	public boolean isAudio(int track) {
		int i = libcdda.cdda_track_audiop(cdrom, track);
		return i == 1;
	}
	
	/**
	 * Whether to log verbose messages from the native libraries.
	 * @param verbose - true for verbose logging.
	 */
	public void setVerbose(boolean verbose) {
		if (verbose) {
			this.verbose = 1;
		} else {
			this.verbose = 0;
		}
	}
	
}
