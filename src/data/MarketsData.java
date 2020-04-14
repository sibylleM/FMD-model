package data;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import utils.FileInput;
import utils.FileOutput;

public class MarketsData {
	public class MarketsDataRecord {	
//		public String CPH;
		public int Location_Easting;
		public int Location_Northing;
		
		public MarketsDataRecord(int location_Easting, int location_Northing) {
			super();
			Location_Easting = location_Easting;
			Location_Northing = location_Northing;
		}
		
		public int getX() {
			return Location_Easting;
		}
		
		public int getY() {
			return Location_Northing;
		}
		
		public String toString() {
			return new String() + Location_Easting + "," + Location_Northing;	
		}
		
	}
	
	private static TreeMap<String, MarketsDataRecord> data;
	
	public MarketsData(String fileName) {
		data = new TreeMap<String, MarketsDataRecord>();
		
		FileInput fileInput = new FileInput(fileName, ",|;");
		

        System.out.print("Started reading locations data from the file " + fileName + "... ");
        try {
            List<String> nextLine;
            fileInput.readLine(); // Ignore first line.
            while ((nextLine = fileInput.readLine()) != null) {
                if (!addRecord(nextLine.get(0),				// CPH
                		Integer.valueOf(nextLine.get(1)),	// Easting
                		Integer.valueOf(nextLine.get(2))	// Northing
    					)) {
                	System.err.println("ERROR: couldn't interpret read data!");
                }
            }
            fileInput.close();
        } catch (ArrayIndexOutOfBoundsException aioobe) {
        	System.err.println(aioobe.getLocalizedMessage());
        }
        System.out.println("Done!");
        
        System.out.println("\tWe have data about " + data.size() + " markets.");
	}
	
	public boolean addRecord(String cPH, int location_Easting, int location_Northing) {		
		if (data.containsKey(cPH)) {
			MarketsDataRecord record = data.get(cPH);
			System.err.println(cPH + " (" + record.Location_Easting + ", " + record.Location_Northing + ") already exists in the dataset.");
		} else {
			MarketsDataRecord record = new MarketsDataRecord(location_Easting, location_Northing);
			data.put(cPH, record);
		}
		return true;
	}

	public MarketsDataRecord getRecord(String key) {
		return data.get(key);
	}
	
	public Iterator<String> getIterator() {
		Set<String> keys = data.keySet();
		Iterator<String> iterator = keys.iterator();
		return iterator;
	}
	
	public void deleteRecord(String cph) {
		data.remove(cph);
	}
	
	public void writeToFile(String fileName) {
		FileOutput outputFile = new FileOutput(fileName);
		
		outputFile.getWriter().format("CPH,Location_Easting,Location_Northing\n");
		
		for (Iterator<String> i = getIterator(); i.hasNext(); ) {
			String cph = (String) i.next();
			MarketsDataRecord record = getRecord(cph);
			
			outputFile.getWriter().format("%s,%d,%d\n", cph, record.Location_Easting, record.Location_Northing);
		}
		outputFile.close();
	}
}