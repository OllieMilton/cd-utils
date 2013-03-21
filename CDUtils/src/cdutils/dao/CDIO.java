package cdutils.dao;

import java.io.IOException;

import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * <p>
 * A low level wrapper around the native cdio library.
 * 
 * @author Ollie
 *
 */
public class CDIO {

	private interface LibCDIOAPI extends Library {
		int	cdio_eject_media_drive(String device);
	}
	
	private final LibCDIOAPI cdio;
	
	public CDIO() {
		cdio = (LibCDIOAPI) Native.loadLibrary("cdio", LibCDIOAPI.class);
	}
		
	/**
	 * Ejects the device with the given device locator 
	 * @param device - the device locator.
	 * @return True if seccessful.
	 */
	public boolean eject(String device) {
		boolean result = cdio.cdio_eject_media_drive(device) >= 0;
		if (!result) {
			try {
				Runtime rt = Runtime.getRuntime();
				Process proc = rt.exec(new String[] {"eject", device});
				proc.waitFor();
				result = true;
			} catch (IOException e) {
				result = false;
			} catch (InterruptedException e) {
				result = false;
			}
		}
		return result;
	}
	
}
