package cdutils.domain;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>
 * Holds the table of contents for a single CD.
 * 
 * @author ollie
 *
 */
public class TOC {

	private List<TOCEntry> entries;
			
	public TOC() {
		entries = new ArrayList<TOCEntry>();
	}
	
	public void addEntry(TOCEntry tocent) {
		entries.add(tocent);
	}

	public List<TOCEntry> entries() {
		return entries;
	}
	
	public int size() {
		return entries.size();
	}
	
	public String getDuration() {
		int totalSectors = getTotalSectors();
		int secs = totalSectors/75;
		int mins = secs/60;
		return String.format("%02d:%02d:%02d", (mins/60), (mins%60), (secs%60)); 
	}
	
	public int getTotalSectors() {
		return entries.get(size()-1).getLastSector();
	}
	
	@Override
	public String toString() {
		String s = "\nTable of contents\n\nId\tFirst\tLast\tCopy\tPre\tChan\tDuration\n" +
				"==========================================================\n";
		for (TOCEntry ent : entries) {
			s+=ent.toString();
		}
		s+="==========================================================\n" +
		" Total sectors: "+getTotalSectors()+" duration: "+getDuration()+"\n" +
		"==========================================================\n\n";
		return s;
	}
}
