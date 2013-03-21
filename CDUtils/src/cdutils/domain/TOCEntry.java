package cdutils.domain;

/**
 * <p>
 * Represents a row in the CD's table of centents.
 * 
 * @author ollie
 *
 */
public class TOCEntry {

	private int id;
	private int firstSector;
	private int lastSector;
	private boolean copyPermitted;
	private boolean linearPreemphasis;
	private int channels;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getFirstSector() {
		return firstSector;
	}
	
	public void setFirstSector(int firstSector) {
		this.firstSector = firstSector;
	}
	
	public int getLastSector() {
		return lastSector;
	}
	
	public void setLastSector(int lastSector) {
		this.lastSector = lastSector;
	}
	
	public boolean isCopyPermitted() {
		return copyPermitted;
	}
	
	public void setCopyPermitted(boolean copyPermitted) {
		this.copyPermitted = copyPermitted;
	}
	
	public boolean isLinearPreemphasis() {
		return linearPreemphasis;
	}
	
	public void setLinearPreemphasis(boolean linearPreemphasis) {
		this.linearPreemphasis = linearPreemphasis;
	}
	
	public int getChannels() {
		return channels;
	}
	
	public void setChannels(int channels) {
		this.channels = channels;
	}
	
	public String getDuration() {
		int secs = getDurationSecs();
		int mins = secs/60;
		return String.format("%02d:%02d:%02d", (mins/60), (mins%60), (secs%60)); 
	}
	
	public int getDurationSecs() {
		int totalSectors = lastSector - firstSector;
		int secs = totalSectors/75;
		return secs;
	}
	
	@Override
	public String toString() {
		String pre = linearPreemphasis ? "yes" : "no";
		String cpy = copyPermitted ? "yes" : "no";
		return id+".\t"+firstSector+"\t"+lastSector+"\t"+cpy+"\t"+pre+"\t"+channels+"\t"+getDuration()+"\n";
	}
}
