package data;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import utils.FileInput;
import utils.FileOutput;

public class OldPremisesData {
	public class OldPremisesDataRecord {
//		String CPH;
		public String Location_Type;
		public int Location_Easting;
		public int Location_Northing;
		public int Census_Sheep;
		public int Census_Cattle;
		
		public OldPremisesDataRecord(/*String cPH,*/ String location_Type, int location_Easting, int location_Northing, int census_Sheep) {
			super();
//			CPH = cPH;
			Location_Type = location_Type;
			Location_Easting = location_Easting;
			Location_Northing = location_Northing;
			Census_Sheep = census_Sheep;
		}
		
		public int getX() {
			return Location_Easting;
		}
		
		public int getY() {
			return Location_Northing;
		}
		
	}
	
	private static TreeMap<String, OldPremisesDataRecord> data;
	
	public OldPremisesData(String fileName) {
		data = new TreeMap<String, OldPremisesDataRecord>();
		
		FileInput fileInput = new FileInput(fileName, ",|;");
		

        System.out.print("Started reading locations data from the file " + fileName + "... ");
        try {
            List<String> nextLine;
            fileInput.readLine(); // Ignore first line.
            while ((nextLine = fileInput.readLine()) != null) {
//            	if (Integer.valueOf(nextLine.get(4)) == 0) // skip premises with NO sheep
//            			continue;
                if (!addRecord(nextLine.get(0),				// CPH
                		nextLine.get(1),					// Type
                		Integer.valueOf(nextLine.get(2)),	// Easting
                		Integer.valueOf(nextLine.get(3)),	// Northing
                		Integer.valueOf(nextLine.get(4))	// Farm size
    					)) {
                	System.err.println("ERROR: couldn't interpret read data!");
                }
            }
            fileInput.close();
        } catch (ArrayIndexOutOfBoundsException aioobe) {
        	System.err.println(aioobe.getLocalizedMessage());
        }
        System.out.println("Done!");
        
        System.out.println("\tWe have data for " + data.size() + " premises.");
	}
	
	private boolean addRecord(String cPH, String location_Type, int location_Easting, int location_Northing, int census_Sheep) {
		OldPremisesDataRecord record = new OldPremisesDataRecord(/*String cPH,*/ location_Type, location_Easting, location_Northing, census_Sheep);
		data.put(cPH, record);		
		return true;
	}

	public OldPremisesDataRecord getRecord(String key) {
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
		
		outputFile.getWriter().format("CPH,Location_Type,Location_Easting,Location_Northing,Census_Sheep\n");
		
		for (Iterator<String> i = getIterator(); i.hasNext(); ) {
			String cph = (String) i.next();
			OldPremisesDataRecord record = getRecord(cph);
			
			outputFile.getWriter().format("%s,%s,%d,%d,%d\n", cph, record.Location_Type, record.Location_Easting, record.Location_Northing, record.Census_Sheep);
		}
		outputFile.close();
	}
}
