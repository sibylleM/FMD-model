package data;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import utils.FileInput;
import utils.FileOutput;

public class FarmsData {
	public class FarmsDataRecord {		
		public int Location_Easting;
		public int Location_Northing;
		public int Census_Sheep;
		public int Census_Cattle;
		
		public FarmsDataRecord(int location_Easting, int location_Northing, int census_Sheep, int census_Cattle) {
			super();
			Location_Easting = location_Easting;
			Location_Northing = location_Northing;
			Census_Sheep = census_Sheep;
			Census_Cattle = census_Cattle;
		}
		
		public int getX() {
			return Location_Easting;
		}
		
		public int getY() {
			return Location_Northing;
		}
		
		public String toString() {
			return new String() + Location_Easting + "," + Location_Northing + "," + Census_Sheep + "," + Census_Cattle;	
		}
		
	}
	
	private static TreeMap<String, FarmsDataRecord> data;
	
	public FarmsData(String fileName) {
		data = new TreeMap<String, FarmsDataRecord>();
		
		FileInput fileInput = new FileInput(fileName, ",|;");
		

        System.out.print("Started reading locations data from the file " + fileName + "... ");
        try {
            List<String> nextLine;
            fileInput.readLine(); // Ignore first line.
            while ((nextLine = fileInput.readLine()) != null) {
                if (!addRecord(nextLine.get(0),				// CPH
                		nextLine.get(1),					// Species
                		Integer.valueOf(nextLine.get(2)),	// Easting
                		Integer.valueOf(nextLine.get(3)),	// Northing
                		nextLine.get(4),					// OSPAR (to be ignored)
                		Integer.valueOf(nextLine.get(5))	// Farm size
    					)) {
                	System.err.println("ERROR: couldn't interpret read data!");
                }
            }
            fileInput.close();
        } catch (ArrayIndexOutOfBoundsException aioobe) {
        	System.err.println(aioobe.getLocalizedMessage());
        }
        System.out.println("Done!");
        
        System.out.println("\tWe have data about " + data.size() + " farms.");
	}
	
	public boolean addRecord(String cPH, String species, int location_Easting, int location_Northing, String OSPAR, int size) {
		int census_Sheep = 0, census_Cattle = 0;
		
		// REAL DATA??
		if (true) {
			if (Integer.valueOf(cPH.substring(0, cPH.indexOf("/"))) < 61) {
				return true;
			}
			
			if (location_Northing < 531900) {
				return true;
			}
			
	//		Limit dataset
//			if (location_Northing > 600000) {
//				return true;
//			}
//			if (location_Easting < 280000) {
//				return true;
//			}
			
			if (cPH.equals("92/769/0120") || cPH.equals("79/465/0783") || cPH.equals("71/251/0051"))
				return true;
			
		}
		
		if (species.equals("SHEEP"))
			census_Sheep = size;
		else if (species.equals("CATTLE"))
			census_Cattle = size;
		else {
			System.err.println("Unknown species in the dataset!");
			System.exit(1);
		}
		
		if (data.containsKey(cPH)) {
			FarmsDataRecord record = data.get(cPH);
			record.Census_Sheep += census_Sheep;
			record.Census_Cattle += census_Cattle;
//			NewPremisesDataRecord record = new NewPremisesDataRecord(location_Easting, location_Northing, census_Sheep, census_Cattle);
//			int i = 1;
//			while (data.containsKey(cPH + "_" + i))
//				i++;
//			data.put(cPH + "_" + i, record);
			
			if (record.Location_Easting != location_Easting || record.Location_Northing != location_Northing) {
				System.err.println(cPH + ": was (" + record.Location_Easting + ", " + record.Location_Northing + 
						"), now (" + location_Easting + ", " + location_Northing + ").");
			}
		} else {
			FarmsDataRecord record = new FarmsDataRecord(location_Easting, location_Northing, census_Sheep, census_Cattle);
			data.put(cPH, record);
		}
		return true;
	}

	public FarmsDataRecord getRecord(String key) {
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
		
		outputFile.getWriter().format("CPH,Location_Easting,Location_Northing,Census_Sheep,Census_Cattle\n");
		
		for (Iterator<String> i = getIterator(); i.hasNext(); ) {
			String cph = (String) i.next();
			FarmsDataRecord record = getRecord(cph);
			
			outputFile.getWriter().format("%s,%d,%d,%d,%d\n", cph, record.Location_Easting, record.Location_Northing, record.Census_Sheep, record.Census_Cattle);
		}
		outputFile.close();
	}
	
	public int getSize() {
		return data.size();
	}
}

