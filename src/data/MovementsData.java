package data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JOptionPane;

import utils.Utils;
import utils.FileInput;
import utils.FileOutput;

public class MovementsData {
	public class MovementsDataRecord {
		public String date;	// Movement_Date
		public String srcCPH; //Best_CPH_Dep
		public String mktCPH;
		public String rcpCPH;
		public int size; //Actual_Number_of_Animals_Move
		//80%	90%	92%	95%	97%"
		
		public MovementsDataRecord(String date, String srcCPH, String mktCPH,
				String destCPH, int size) {
			super();
			this.date = date;
			this.srcCPH = srcCPH;
			this.mktCPH = mktCPH;
			this.rcpCPH = destCPH;
			this.size = size;
		}
		
		protected void updateSize(int newSize) {
			size = newSize;
		}

		@Override
		public String toString() {
//			return "[date=" + date + ", srcCPH=" + srcCPH
//			+ ", mktCPH=" + mktCPH + ", rcpCPH=" + rcpCPH + ", size="
//			+ size + "]";
//			return date + "\t" + srcCPH	+ "\t" + mktCPH + "\t" + rcpCPH + "\t" + size;
			return date + ": \t" + srcCPH + "\t -> " + mktCPH + "\t -> " + rcpCPH + "\t (" + size + ")";
		}
	}
	
	private static TreeMap<String,MovementsDataRecord> treeData;
	
	public MovementsData(String fileName, FarmsData farmsData, MarketsData marketsData) {
		treeData = new TreeMap<String, MovementsData.MovementsDataRecord>();
		int records = 0;
		
		FileInput fileInput = new FileInput(fileName, ",|;");
		
		System.out.println("Started reading movements data from the file " + fileName + "... ");
        try {
            List<String> nextLine;
            fileInput.readLine(); // Ignore first line.
            while ((nextLine = fileInput.readLine()) != null) {
            	records++;
//            	System.out.println(nextLine.size());
                if (!addRecord(
//                		nextLine.get(0),					// species
                		Integer.valueOf(nextLine.get(1)),	// year
                		nextLine.get(2),					// date
                		Integer.valueOf(nextLine.get(3)),	// size
    					
                		nextLine.get(4),					// srcCPH
                		nextLine.get(5),					// type
                		Integer.valueOf(nextLine.get(6)),	// east
                		Integer.valueOf(nextLine.get(7)),	// north
    					
                		nextLine.get(8),					// mktCPH
                		Integer.valueOf(nextLine.get(9)),	// 
                		Integer.valueOf(nextLine.get(10)),	// 
                		
    					nextLine.get(11),					// destCPH
                		nextLine.get(12),					// type
                		Integer.valueOf(nextLine.get(13)),	// 
                		Integer.valueOf(nextLine.get(14))	// 
                		, farmsData, marketsData)) {
//                	System.err.println("ERROR: couldn't interpret read data!");
//                	for (String str : nextLine)
//                		System.out.print(str + "\t");
//                	System.out.println();
                }
            }
            fileInput.close();
        } catch (ArrayIndexOutOfBoundsException aioobe) {
        	System.err.println(aioobe.getLocalizedMessage());
        }
        System.out.println("Done!");
        
        System.out.println("\tWe have information about " + treeData.size() + " movements.");
	}
		
	boolean addRecord(Integer year, String date, Integer size, 
			String srcCPH, String srcType, Integer srcEast, Integer srcNorth,
			String mktCPH, Integer mktEast, Integer mktNorth,
			String rcpCPH, String rcpType, Integer rcpEast, Integer rcpNorth, FarmsData farmsData, MarketsData marketsData) {
		

//		ArrayList<String> manuallyExclude = new ArrayList<String>();
		if (true) {
//			manuallyExclude.add("80/472/0041");
//			manuallyExclude.add("68/138/0028");
//			manuallyExclude.add("66/666/6666");
//			manuallyExclude.add("79/433/0120");
//			manuallyExclude.add("79/433/0120");
//			manuallyExclude.add("79/433/0120");
//			manuallyExclude.add("79/439/0034");
//			manuallyExclude.add("75/321/0066");
//			manuallyExclude.add("79/450/0023");
//			manuallyExclude.add("83/638/0009");
//			manuallyExclude.add("96/840/0021");
//			manuallyExclude.add("82/513/0011");
//			manuallyExclude.add("79/465/0157");
//			manuallyExclude.add("82/499/0095");
//			
//			manuallyExclude.add("92/769/0120");
//			manuallyExclude.add("79/465/0783");
//			manuallyExclude.add("71/251/0051");
//			manuallyExclude.add("71/251/7005"); //empty
//			manuallyExclude.add("88/644/0026"); //empty
					
			
//			if (year == 2010) // SKIP 2010 movements
//				return true;
			
			// exclude movements to England; Sibylle: FIX here!!!!
			if (Integer.valueOf(rcpCPH.substring(0, rcpCPH.indexOf("/"))) < 61) {
				return true;
			}
			
			// exclude movements from England
			if (Integer.valueOf(srcCPH.substring(0, srcCPH.indexOf("/"))) < 61) {
	//			System.err.println("From England:\t" + date + "\t"+ rcpCPH);
				
	//			if (!rcpType.contains("Slaughterhouse") && !rcpType.contains("Market") && farmsData.getRecord(rcpCPH) != null) {			
	//				FileOutput outputFile = new FileOutput("input/newIndex.csv", true);
	//				outputFile.getWriter().format("%s,%s\n", rcpCPH, date);
	//				outputFile.close();
	//			}
				
				return true;
			}
		}
		
		// check markets
		if (!mktCPH.isEmpty() && marketsData.getRecord(mktCPH) == null) {
			System.err.println("Warning: market " + mktCPH + " is not in the markets data.");
			switch (JOptionPane.showConfirmDialog(null, "Market " + mktCPH + " is not in the markets data. Would you like to add it?", "Warning!", JOptionPane.YES_NO_OPTION)) {
				case 0: // YES
					String east = JOptionPane.showInputDialog(null, "Please provide Easting coordinate for market " + mktCPH + ":", "Adding market...", JOptionPane.PLAIN_MESSAGE);
					if (east != null) {
						String north = JOptionPane.showInputDialog(null, "Please provide Northing coordinate for market " + mktCPH + ":", "Adding market...", JOptionPane.PLAIN_MESSAGE);
						if (north != null)
							marketsData.addRecord(mktCPH, Integer.valueOf(east), Integer.valueOf(north));
					}
					break;
				case 1: // NO
					// Do nothing. Just skip it.
					break;
				default:
					;
			}
		    
			return true;
		}
		
		// exclude movements to slaughterhouses
		if (rcpType.contains("Slaughterhouse"))
			return true;
		


		if (rcpType.contains("Market")) {
//			System.err.println("rcp=mkt: " + date + " " + srcCPH + " -> " + mktCPH + " -> " + rcpCPH);
			return false;
		}
		
		if (srcType.contains("Market")) {
//			System.err.println("src=mkt: " + date + " " + srcCPH + " -> " + mktCPH + " -> " + rcpCPH);
			return false;
		}
		
		// Manual exclusion of CPHs
//		if (manuallyExclude.contains(srcCPH) || manuallyExclude.contains(rcpCPH))
//			return true;
		
		// check locations
		if (farmsData.getRecord(srcCPH) == null) {
			if (srcNorth > 531900)
				farmsData.addRecord(srcCPH, "SHEEP", srcEast, srcNorth, "", 0);
//			System.out.println(srcCPH + " added to the dataset.");
		} else if (farmsData.getRecord(srcCPH).Location_Easting != srcEast || farmsData.getRecord(srcCPH).Location_Northing != srcNorth) {
			double dist = Math.sqrt((farmsData.getRecord(srcCPH).Location_Easting - srcEast) * (farmsData.getRecord(srcCPH).Location_Easting - srcEast) +
					(farmsData.getRecord(srcCPH).Location_Northing - srcNorth) * (farmsData.getRecord(srcCPH).Location_Northing - srcNorth));
//			System.out.println("Inconsistent location for\t" + srcCPH + "\t: was " + 
//					farmsData.getRecord(srcCPH).Location_Easting + ", " + farmsData.getRecord(srcCPH).Location_Northing + ", became " +
//					srcEast + ", " + srcNorth + ".\t " + dist);
		}
		if (farmsData.getRecord(rcpCPH) == null) {
			if (rcpNorth > 531900)
				farmsData.addRecord(rcpCPH, "SHEEP", rcpEast, rcpNorth, "", 0);
//			System.out.println(rcpCPH + " added to the dataset.");
		} else if (farmsData.getRecord(rcpCPH).Location_Easting != rcpEast || farmsData.getRecord(rcpCPH).Location_Northing != rcpNorth) {
			double dist = Math.sqrt((farmsData.getRecord(rcpCPH).Location_Easting - rcpEast) * (farmsData.getRecord(rcpCPH).Location_Easting - rcpEast) +
									(farmsData.getRecord(rcpCPH).Location_Northing - rcpNorth) * (farmsData.getRecord(rcpCPH).Location_Northing - rcpNorth));
//			System.out.println("Inconsistent location for\t" + rcpCPH + "\t: was " + 
//					farmsData.getRecord(rcpCPH).Location_Easting + ", " + farmsData.getRecord(rcpCPH).Location_Northing + ", became " +
//					rcpEast + ", " + rcpNorth + ".\t" + dist);
		}
		
		String key = getKey(date, srcCPH, mktCPH, rcpCPH);
		MovementsDataRecord record = treeData.get(key);
		if (record == null) {
			record = new MovementsDataRecord(date, srcCPH, mktCPH, rcpCPH, size);
			treeData.put(key, record);
		} else {
			System.err.println("Warning: duplicate movement for the same route (" + srcCPH + " to " + rcpCPH + 
					") on the same date (" + date + ")! Adding batches " + record.size + " and " + size + ".");
			record.updateSize(record.size + size);
		}				
		return true;
	}
	
	private String getKey(String date, String srcCPH, String mktCPH, String rcpCPH) {
		return (date + srcCPH + mktCPH + rcpCPH);
	}
	
//	public void update(String fileName, int readNum) {
//		int records = 0;
//		
//		FileInput fileInput = new FileInput(fileName, ",|;");
//		
//		System.out.print("Started reading movements data from the file " + fileName + "... ");
//        try {
//            List<String> nextLine;
//            fileInput.readLine(); // Ignore first line.
//            while ((nextLine = fileInput.readLine()) != null) {
//            	records++;
////            	System.out.println(nextLine.size());
//                if (!addRecord(
//                		nextLine.get(0),					// date
//    					nextLine.get(1),					// srcCPH
//    					nextLine.get(2),					// mktCPH
//    					nextLine.get(3),					// destCPH
//    					Integer.valueOf(nextLine.get(4 + readNum))	// size
//    					)) {
//                	System.err.println("ERROR: couldn't interpret read data!");
//                }
//            }
//            fileInput.close();
//        } catch (ArrayIndexOutOfBoundsException aioobe) {
//        	System.err.println(aioobe.getLocalizedMessage());
//        }
//        System.out.println("Done!");
//        
//        System.out.println("\tWe have information about " + treeData.size() + " movements.");
//	}
	
	public int getSize() {
		return treeData.size();
	}

	public MovementsDataRecord getRecord(String key) {
		return treeData.get(key);
	}
	
	public void removeRecord(int key) {
		treeData.remove(key);
	}
	
	public void removeRecord(MovementsDataRecord record) {
		treeData.remove(getKey(record.date, record.srcCPH, record.mktCPH, record.rcpCPH));
	}
	
//	private int hash(int anon_herd, int anon_herd_from, int mov_month) {
//		return anon_herd * anon_herd_from * mov_month;
//	}
	
	public Iterator<String> getMovementsIterator() {
		Set<String> keys = treeData.keySet();
		Iterator<String> iterator = keys.iterator();
		return iterator;
	}
	
//	public SortedMap<String, MovementsDataRecord> getMovements(String date) {		
//		return treeData.subMap(treeData.ceilingKey(date), treeData.floorKey(date));
//	}
	
	public ArrayList<MovementsDataRecord> getMovingOnDatePremises(Calendar cal) {
		ArrayList<MovementsDataRecord> movements = new ArrayList<MovementsDataRecord>();
		for (Iterator<String> i = getMovementsIterator(); i.hasNext(); ) {
			String key = (String) i.next();
			MovementsDataRecord movementRecord = treeData.get(key);
			
//			Calendar calMov = Utils.fromStringToCalendar(movementRecord.date);
			
			if (cal.compareTo(Utils.fromStringToCalendar(movementRecord.date)) == 0)
				movements.add(movementRecord);
		}
		return movements;
	}

	public int getNumberOfInMovements(String cph) {
		int res = 0;
		
		ArrayList<MovementsDataRecord> movements = new ArrayList<MovementsDataRecord>();
		for (Iterator<String> i = getMovementsIterator(); i.hasNext(); ) {
			String key = (String) i.next();
			MovementsDataRecord movementRecord = treeData.get(key);
			
			if (movementRecord.rcpCPH.equals(cph))
				res++;
		}
		return res;
	}
	
	public int getNumberOfInSheep(String cph) {
		int res = 0;
		
		ArrayList<MovementsDataRecord> movements = new ArrayList<MovementsDataRecord>();
		for (Iterator<String> i = getMovementsIterator(); i.hasNext(); ) {
			String key = (String) i.next();
			MovementsDataRecord movementRecord = treeData.get(key);
			
			if (movementRecord.rcpCPH.equals(cph))
				res += movementRecord.size;
		}
		return res;
	}
	
//	public ArrayList<String> getSourcesForFarm(String rcpCPH, Calendar cal) {
//		ArrayList<String> res = new ArrayList<String>();
//		for (Iterator i = getMovementsIterator(); i.hasNext(); ) {
//			String key = (String) i.next();
//			MovementsDataRecord movementRecord = treeData.get(key);
//			
//			if (cal.compareTo(Aux.fromStringToCalendar(movementRecord.date)) == 0 && movementRecord.rcpCPH.equals(rcpCPH))
//				res.add(movementRecord.srcCPH);
//		}
//		
//		return res;
//	}
	
	
	public void writeToFile(String fileName) {
		FileOutput outputFile = new FileOutput(fileName);
		
		outputFile.getWriter().format("CPH,Location_Type,Location_Easting,Location_Northing,Census_Sheep\n");
		
		for (Iterator<String> i = getMovementsIterator(); i.hasNext(); ) {
			String key = (String) i.next();
			MovementsDataRecord movRec = treeData.get(key);
			
			outputFile.getWriter().format("%s,%s,%s,%s,%d\n", movRec.date, movRec.srcCPH, movRec.mktCPH, movRec.rcpCPH, movRec.size);
		}
		outputFile.close();
	}
	
}
