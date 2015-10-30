package cdutils.service;

import java.io.IOException;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tritonus.share.sampled.convert.TAsynchronousFilteredAudioInputStream;

import cdutils.dao.CDDAParanoia;
import cdutils.dao.CDIO;
import cdutils.dao.DiscId;
import cdutils.domain.RipProgressEvent;
import cdutils.domain.TOC;
import cdutils.domain.TOCEntry;
import cdutils.exception.DiscInUseException;
import cdutils.exception.DiscReadException;

/**
 * <p>
 * A mid level wrapper for the low level {@code CDDAParanoia}, {@code LibDiscId} and {@code CDIO} libraries.
 * 
 * @author Ollie
 *
 */
public class CDDA implements CD {

	private final Log logger;
	private volatile boolean busy;
	private String device;
	private CDDAParanoia paranoia;
	private DiscId discId;
	private CDIO cdio;
	private volatile boolean terminated;
	private volatile boolean ripping;
	private AudioFormat	 cddaFormat = new AudioFormat(
		AudioFormat.Encoding.PCM_SIGNED,
		44100.0F, 16, 2, 4, 44100.0F, false);
		
	/**
	 * Constructs a new {@code CDDA} object. The CDROM drive associated with the given 
	 * device locator is used each time a call to the disc is made. 
	 * @param device - the CDROM drive locator.
	 */
	public CDDA(String device) {
		this.device = device;
		busy = false;
		terminated = false;
		ripping = false;
		paranoia = new CDDAParanoia();
		logger = LogFactory.getLog(getClass());
		discId = new DiscId(device);
		cdio = new CDIO();
	}
		
	/* (non-Javadoc)
	 * @see cdutils.service.CD#eject()
	 */
	@Override
	public boolean eject() {
		cancel();
		return cdio.eject(device);
	}
	
	/* (non-Javadoc)
	 * @see cdutils.service.CD#getCDDBId()
	 */
	@Override
	public String getCDDBId() throws DiscReadException {
		return discId.getFreeDdId();
	}
	
	/* (non-Javadoc)
	 * @see cdutils.service.CD#getMusicBrainzDiscId()
	 */
	@Override
	public String getMusicBrainzDiscId() throws DiscReadException {
		return discId.getMusicBrainzDiscId();
	}
		
	/* (non-Javadoc)
	 * @see cdutils.service.CD#getMusicBrainzURL()
	 */
	@Override
	public String getMusicBrainzURL() throws DiscReadException {
		return discId.getMusicBrainzURL();
	}
	
	/* (non-Javadoc)
	 * @see cdutils.service.CD#getTableOfContents()
	 */
	@Override
	public TOC getTableOfContents() throws DiscReadException {
		TOC toc;
		try {
			open();
			toc = buildTOC();
		} finally {
			close();
		}
		return toc;
	}
	
	/* (non-Javadoc)
	 * @see cdutils.service.CD#isDiscInDrive()
	 */
	@Override
	public boolean isDiscInDrive() {
		boolean result = true;
		try {
			open();
		} catch (DiscReadException e) {
			result = false;
		} catch (DiscInUseException e) {
			// do nothing, if the disc is in use then there must be a disc in the drive
		} finally {
			close();
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see cdutils.service.CD#getTrack(int)
	 */
	@Override
	public AudioInputStream getTrack(int track) throws DiscReadException {
		return getTrack(track, (RipProgressListener) null);
	}

	/* (non-Javadoc)
	 * @see cdutils.service.CD#getTrack(int, java.io.OutputStream)
	 */
	@Override
	public void getTrack(int track, OutputStream output) throws DiscReadException {
		getTrack(track, null, output);		
	}
	
	/* (non-Javadoc)
	 * @see cdutils.service.CD#getTrack(int, cdutils.service.RipProgressListener)
	 */
	@Override
	public AudioInputStream getTrack(int track, RipProgressListener listener) throws DiscReadException {
		AudioInputStream ais;
		logger.info("Starting rip on track ["+track+"]");
		try {
			open();
			TOC toc = buildTOC();
			if (track < 1 || track > toc.size()) {
				throw new IllegalArgumentException("Track ["+track+"] is an invalid track no");
			}
			ais = new CddaAudioInputStream(track, listener);
		} catch (Exception e) {
			close();
			throw new RuntimeException(e);
		}
		return ais;
	}
	
	/* (non-Javadoc)
	 * @see cdutils.service.CD#getTrack(int, cdutils.service.RipProgressListener, java.io.OutputStream)
	 */
	@Override
	public void getTrack(int track, RipProgressListener listener, OutputStream output) throws DiscReadException {
		AudioInputStream ais = getTrack(track, listener);
		byte[] bout = new byte[1024];
		try {
			while (ais.read(bout) > 0) {
				output.write(bout);
			}
		} catch (IOException e) {
			throw new DiscReadException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see cdutils.service.CD#cancel()
	 */
	@Override
	public void cancel() {
		if (busy) {
			if (ripping) {
				terminated = true;
				while (terminated) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			} else {
				close();
			}
		}
	}
	
	/**
	 * Opens the disc using the specified drive, searches for a drive containing
	 * an audio CD if no device has been configured.   
	 * @throws DiscReadException - if there is no disc in the drive, the disc cannot be read or no drive can be found.
	 */
	private void open() throws DiscReadException {
		if (busy) {
			throw new DiscInUseException();
		}
		if (device != null) {
			logger.info("Opening cdrom ["+device+"]");
			paranoia.open(device);
		} else {
			logger.info("Searching for cdrom.");
			paranoia.open();
		}
		busy = true;
	}
	
	/**
	 * Closes the CDROM device and releases any resources ready for reuse. 
	 */
	private void close() {
		paranoia.close();
		busy = false;
		terminated = false;
		ripping = false;
	}
	
	/**
	 * Builds a table of contents object, assumes that the opening and closing of the disc
	 * will be handled by the caller. 
	 * @return The table of contents.
	 */
	private TOC buildTOC() {
		TOC toc = new TOC();
		int tracks = paranoia.getTracks();
		for (int i = 1; i < tracks+1; i++) {
			if (paranoia.isAudio(i)) {
				TOCEntry ent = new TOCEntry();
				ent.setId(i);
				ent.setFirstSector(paranoia.getFirstSector(i));
				ent.setLastSector(paranoia.getLastSector(i));
				ent.setCopyPermitted(paranoia.copyPermitted(i));
				ent.setLinearPreemphasis(paranoia.linearPreemphasis(i));
				ent.setChannels(paranoia.getChannels(i));
				toc.addEntry(ent);
			}
		}
		logger.debug("Got table of contents:\n"+toc.toString());
		return toc;
	}
	
	/**
	 * An {@code AudioInputStream} that reads the track passed into the constructor from the 
	 * disc currently in the drive.
	 */
	private class CddaAudioInputStream extends TAsynchronousFilteredAudioInputStream {
		
		RipProgressListener listener;
		int frameCount = 0;
		int firstFrame;
		int lastFrame;
		int totalFrames;
		int track;
		
		/**
		 * Constructs a new {@code CddaAudioInputStream} for the given track. 
		 * @param track - the track to read from the disc.
		 */
		public CddaAudioInputStream(int track, RipProgressListener listener) {
			super(cddaFormat, AudioSystem.NOT_SPECIFIED);
			firstFrame = paranoia.getFirstSector(track);
			lastFrame = paranoia.getLastSector(track);
			totalFrames = lastFrame - firstFrame;
			paranoia.seek(firstFrame);
			this.listener = listener;
			this.track = track;
			ripping = true;
		}
		
		@Override
		public void execute() {
			if (terminated) {
				closeAIS();
				logger.info("Terminated, releasing resources.");
				return;
			}
			try {
				while (frameCount < totalFrames && getCircularBuffer().availableWrite() >= CDDAParanoia.cddaFrameSize) {
					getCircularBuffer().write(paranoia.readNextFrame());
					frameCount++;
				}
				if (listener != null) {
					listener.onRipProgressEvent(new RipProgressEvent(CDDA.this, ((frameCount*100)/totalFrames)));
				}
			} catch (Exception e) {
				closeAIS();
				if (listener != null) {
					listener.onError(e.getMessage());
				}
				logger.error("Error reading CD.");
			} finally {
				if (frameCount >= totalFrames) {
					closeAIS();
					logger.info("Reached end of audio stream on track ["+track+"], releasing resources.");
				}
			}
		}
		
		void closeAIS() {
			getCircularBuffer().close();
			CDDA.this.close();
		}
	}
}
