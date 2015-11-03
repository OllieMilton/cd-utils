package cdutils.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import cdutils.domain.RipProgressEvent;
import cdutils.domain.TOC;
import cdutils.domain.TOCEntry;
import cdutils.exception.DiscReadException;

public class StubCDDA implements CD {
	
	private int tocwait = 0;
	
	@Override
	public TOC getTableOfContents() throws DiscReadException {
		if (tocwait++ < 10) {
			try {
				Thread.sleep(500);
				throw new DiscReadException("No disc");
			} catch (InterruptedException e) {
				throw new RuntimeException();
			}
		}
		TOC toc = new TOC();
		for (int i = 1; i < 11; i++) {
			TOCEntry ent = new TOCEntry();
			ent.setId(i);
			ent.setFirstSector(i*1000);
			ent.setLastSector(i*1000+999);
			ent.setCopyPermitted(true);
			ent.setLinearPreemphasis(false);
			ent.setChannels(2);
			toc.addEntry(ent);
		}
		return toc;
	}

	@Override
	public boolean eject() {
		cancel();
		return true;
	}

	@Override
	public String getCDDBId() throws DiscReadException {
		return "";
	}

	@Override
	public String getMusicBrainzDiscId() throws DiscReadException {
		return "dc16ccbf-ab90-4b3e-86a0-4e1b1b2404bb";
	}

	@Override
	public String getMusicBrainzURL() throws DiscReadException {
		return "http://mm.musicbrainz.org/ws/1/release?type=xml&discid=pbEMghtm0TEFxJq3ZS345qjIiBk-&toc=1+10+167050+150+15345+27050+44940+61170+79155+101660+115450+131945+148855";
	}

	@Override
	public boolean isDiscInDrive() {
		return false;
	}

	@Override
	public AudioInputStream getTrack(int track) throws DiscReadException {
		return getTrack(track, (RipProgressListener) null);
	}

	@Override
	public void getTrack(int track, OutputStream output) throws DiscReadException {
		getTrack(track, null, output);		
	}

	@Override
	public AudioInputStream getTrack(int track, RipProgressListener listener) throws DiscReadException {
		int count = 0;
		try {
			while (count++ < 11) {
				if (listener != null) {
					listener.onRipProgressEvent(new RipProgressEvent(this, count*10));
				}
				Thread.sleep(1000);
			}
			return AudioSystem.getAudioInputStream(new FileInputStream("C:/music/1000.wav"));
		} catch(Exception e) {
			throw new DiscReadException(e);
		}
	}

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

	@Override
	public void cancel() {
		tocwait = 0;
	}

}
