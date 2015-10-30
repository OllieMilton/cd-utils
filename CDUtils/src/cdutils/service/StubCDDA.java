package cdutils.service;

import java.io.OutputStream;

import javax.sound.sampled.AudioInputStream;

import cdutils.domain.TOC;
import cdutils.exception.DiscReadException;

public class StubCDDA implements CD {

	@Override
	public TOC getTableOfContents() throws DiscReadException {
		return null;
	}

	@Override
	public boolean eject() {
		return false;
	}

	@Override
	public String getCDDBId() throws DiscReadException {
		return null;
	}

	@Override
	public String getMusicBrainzDiscId() throws DiscReadException {
		return null;
	}

	@Override
	public String getMusicBrainzURL() throws DiscReadException {
		return null;
	}

	@Override
	public boolean isDiscInDrive() {
		return false;
	}

	@Override
	public AudioInputStream getTrack(int track) throws DiscReadException {
		return null;
	}

	@Override
	public void getTrack(int track, OutputStream output) throws DiscReadException {
		
	}

	@Override
	public AudioInputStream getTrack(int track, RipProgressListener listener) throws DiscReadException {
		return null;
	}

	@Override
	public void getTrack(int track, RipProgressListener listener, OutputStream output) throws DiscReadException {
		
	}

	@Override
	public void cancel() {
		
	}

}
