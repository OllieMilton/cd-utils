package cdutils.service;

import java.io.File;

import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioSystem;

import cdutils.domain.RipProgressEvent;

/**
 * <p>
 * A test programme for testing the CDUtils library. Capable of displaying the
 * discs table of contents and ripping tracks to a wav file.
 * 
 * @author Ollie
 *
 */
public class Main {

	/**
	 * Main method for test programme. Takes two arguments, argument 0 is the
	 * CDROM device locator string. Argument 1 is the id of the track to rip.
	 * If the argument 1 is not supplied then the programme just prints out
	 * the table of contents.
	 * @param args - args[0] = device args[1] track to rip.
	 */
	public static void main(String[] args) throws Exception {
		System.setProperty("jna.nosys", "true");
		CD cd = new CD(args[0]);
		long start = System.currentTimeMillis();
		if (args.length > 1) {
			File f = new File("/home/ollie/test"+args[1]+".wav");
			AudioSystem.write(cd.getTrack(Integer.valueOf(args[1]), new PL()), Type.WAVE, f);
			long dur = System.currentTimeMillis()-start;
			int secs = (int) dur/1000;
			System.out.println(String.format("Total rip time %02d:%02d", (secs/60), (secs%60)));
		} else {
			System.out.println(cd.getTableOfContents());
		}
		System.out.println(cd.getMusicBrainzURL());
		cd.eject();
	}

	static class PL implements RipProgressListener {

		@Override
		public void onRipProgressEvent(RipProgressEvent event) {
			System.out.println(event.getProgress());
		}

		@Override
		public void onError(String message) {
			System.out.println(message);
		}
	}
}
