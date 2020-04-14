package methods;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JOptionPane;

import utils.FileOutput;
import data.InfectionsData;
import data.InfectionsData.InfectionsDataRecord;
import data.MarketsData;
import data.MovementsData;
import data.FarmsData;
import data.OldPremisesData;
import data.MovementsData.MovementsDataRecord;
import data.FarmsData.FarmsDataRecord;
import data.OldPremisesData.OldPremisesDataRecord;

public class DataProcessing {
	
	public static FileOutput logFile = null;
	
	/**
	 * Delete all the premises that: a) had no sheep before the study period, b) were not involved in any movements.
	 * @param premisesData
	 * @param movementsData
	 */
	public static void clearData(OldPremisesData premisesData, MovementsData movementsData) {
		ArrayList<String> cphsUsed = new ArrayList<String>();
		int count = 0;
		
		for (Iterator<String> i = movementsData.getMovementsIterator(); i.hasNext(); ) {
			String key = (String) i.next();
			MovementsDataRecord record = movementsData.getRecord(key);

			cphsUsed.add(record.srcCPH);
			if (!record.mktCPH.equals("")) cphsUsed.add(record.mktCPH);
			cphsUsed.add(record.rcpCPH);
			
			System.out.print(".");
		}
		

		if (logFile != null) logFile.getWriter().format("\nRemoved premises (no movements, no sheep):\n");
		
		for (Iterator<?> i = premisesData.getIterator(); i.hasNext(); ) {
			String cph = (String) i.next();
			OldPremisesDataRecord record = premisesData.getRecord(cph);
			
			if (record.Census_Sheep > 0)
				continue;
			
			if (!cphsUsed.contains(cph)) {
				i.remove();
				count++;
				
				System.out.println(cph + " was removed!");
				if (logFile != null)
					logFile.getWriter().format("%s\n", cph);
			}
		}
		
		System.out.println(count + " premises were removed as they didn't take part in transmission process.");
	}
	
	/**
	 * Tests for missing CPHs.
	 * @param premisesData
	 * @param movementsData
	 */
	public static void testData(OldPremisesData premisesData, MovementsData movementsData) {
		HashSet<String> cphsMissing = new HashSet<String>();
		int count = 0;
		
		for (Iterator<String> i = movementsData.getMovementsIterator(); i.hasNext(); ) {
			String key = (String) i.next();
			MovementsDataRecord record = movementsData.getRecord(key);

			if (premisesData.getRecord(record.srcCPH) == null) {
				cphsMissing.add(record.srcCPH);
				count++;
				System.out.println(record.date);
			}
			if (premisesData.getRecord(record.rcpCPH) == null) {
				cphsMissing.add(record.rcpCPH);
				count++;
				System.out.println(record.date);
			}
			if (!record.mktCPH.isEmpty() && premisesData.getRecord(record.mktCPH) == null) {
				cphsMissing.add(record.mktCPH);
				count++;
				System.out.println(record.date);
			}
			
		}

		for (String cph : cphsMissing)
			System.out.println(cph);
		System.out.println(cphsMissing.size() + " CPHs were missing in the premises data for " + count + " movements.");
		
	}
	
	
	public static void calculateMovsByMonth(OldPremisesData premisesData, MovementsData movementsData) {
		TreeMap<String,Integer> movsByMonth = new TreeMap<String, Integer>();
		
		for (Iterator<String> i = movementsData.getMovementsIterator(); i.hasNext(); ) {
			String key = (String) i.next();
			MovementsDataRecord record = movementsData.getRecord(key);

			String month = (record.date.substring(3));//record.date;//
			if (movsByMonth.containsKey(month)) {
				int prev = movsByMonth.get(month);
				movsByMonth.put(month, prev + 1);
			} else {
				movsByMonth.put(month, 1);
			}
			
		}
		
		for (String month : movsByMonth.keySet()) {
			System.out.println(month + "\t" + movsByMonth.get(month));
		}
	}
	
	
	public static void calculateMovsByPeriod(OldPremisesData premisesData, MovementsData movementsData, int duration) {
		TreeMap<String,Integer> movsByPer = new TreeMap<String, Integer>();
		
		for (Iterator<String> i = movementsData.getMovementsIterator(); i.hasNext(); ) {
			String key = (String) i.next();
			MovementsDataRecord record = movementsData.getRecord(key);

			String month = (record.date.substring(3));//record.date;//
			
			if (Integer.valueOf(record.date.substring(0, 2)) > duration)
				continue;
			
			if (movsByPer.containsKey(month)) {
				int prev = movsByPer.get(month);
				movsByPer.put(month, prev + 1);
			} else {
				movsByPer.put(month, 1);
			}
			
		}
		
		for (String month : movsByPer.keySet()) {
			System.out.println(month + "\t" + movsByPer.get(month));
		}
	}
	
	public static void premisesStats(OldPremisesData premisesData) {
		int premises = 0;
		int farms = 0;
		int markets = 0;
		
		for (Iterator<String> i = premisesData.getIterator(); i.hasNext(); ) {
			String cph = (String) i.next();
			OldPremisesDataRecord record = premisesData.getRecord(cph);
			
			premises++;
			
			if (record.Location_Type.equals("F"))
				farms++;
			else if (record.Location_Type.equals("M"))
				markets++;
			else if (record.Location_Type.equals("X"))
				;
			else 
				System.out.println("Unknown type of premises for CPH " + cph);
		}
		
		System.out.println(premises + " premises: " + farms + " farms, " + markets + " markets, other " + (premises - farms - markets) + " premises.");
	}
	
	public static void movementStats(MovementsData movementsData) {
		int individual = 0;
		int FMF = 0;
		
		for (Iterator<String> i = movementsData.getMovementsIterator(); i.hasNext(); ) {
			String key = (String) i.next();
			MovementsDataRecord record = movementsData.getRecord(key);

			if (record.mktCPH.isEmpty())
				individual++;
			else
				FMF++;
		}
		
		System.out.println((FMF + individual) + " movements: " + FMF + " via market, " + individual + " direct farm-to-farm.");
	}
	
	public static void clearFarms(OldPremisesData premisesData, int x1, int x2, int y1, int y2) {
		int count = 0;
		
		if (logFile != null) logFile.getWriter().format("\nRemoved premises (not in the area [%d, %d], [%d, %d]):\n", x1, y1, x2, y2);
		
		for (Iterator<String> i = premisesData.getIterator(); i.hasNext(); ) {
			String cph = (String) i.next();
			OldPremisesDataRecord record = premisesData.getRecord(cph);
			
			if (!(x1 <= record.Location_Easting && record.Location_Easting <= x2 && y1 <= record.Location_Northing && record.Location_Northing <= y2)) {
				count++;
				i.remove();
				System.out.println(cph + " was removed!");
				if (logFile != null)
					logFile.getWriter().format("%s\n", cph);
			}
		}
		
		System.out.println(count + " premises were removed as they didn't belong to the region of [" + x1 + ", " + y1 + "], [" + x2 + ", " + y2 + "].");
	}
	
	public static void clearMovements(OldPremisesData premisesData, MovementsData movementsData) {
		int count = 0;
		
		if (logFile != null) logFile.getWriter().format("\nRemoved movements (src/mkt/rcp is missing):\n");
		
		for (Iterator<String> i = movementsData.getMovementsIterator(); i.hasNext(); ) {
			String key = (String) i.next();
			MovementsDataRecord record = movementsData.getRecord(key);			

			if (premisesData.getRecord(record.srcCPH) == null || (premisesData.getRecord(record.mktCPH) == null && !record.mktCPH.isEmpty()) || premisesData.getRecord(record.rcpCPH) == null){
				i.remove();
				if (logFile != null)
					logFile.getWriter().format("%s\n", record.toString());
				count++;
			}
			
			System.out.print(".");
		}
		
		System.out.println(count + " movements removed.");
	}
	
	public static void extractIndexCases(FarmsData farmsData, MovementsData movementsData) {
		int count = 0;
		
		if (logFile != null) logFile.getWriter().format("src,rcp,date,from\n");
		
		for (Iterator<String> i = movementsData.getMovementsIterator(); i.hasNext(); ) {
			String from = null;
			String key = (String) i.next();
			MovementsDataRecord record = movementsData.getRecord(key);			

			if (farmsData.getRecord(record.rcpCPH) == null 	 	// recipient is not in Scotland
//					||
//					farmsData.getRecord(record.srcCPH) != null		// source is in Scotland
			)
				continue;	

			if (record.mktCPH.isEmpty())
				from = "Directly from farm";
			else if (farmsData.getRecord(record.mktCPH) == null)
				from = "English market";
			else
				from = "Scottish market";
			
			if (logFile != null)
//				logFile.getWriter().format("%s,%s,%s,%s\n", record.srcCPH, record.rcpCPH, record.date, from);
				logFile.getWriter().format("%s\n", record.toString());
			count++;
			
			System.out.print(".");
		}
		
		System.out.println(count + " movements removed.");
	}
	
	public static void clearFarmsScotland(OldPremisesData premisesData, MovementsData movementsData) {
		ArrayList<String> cphsUsed = new ArrayList<String>();
		int count = 0;
		
		if (logFile != null) logFile.getWriter().format("\nRemoved premises (not in Scotland):\n");
		
		for (Iterator<String> i = premisesData.getIterator(); i.hasNext(); ) {
			String cph = (String) i.next();
			OldPremisesDataRecord record = premisesData.getRecord(cph);
			
			if (Integer.valueOf(cph.substring(0, 2)) <= 56 || record.Location_Northing < 531941) {
				i.remove();
				count++;

				System.out.println(cph + " was removed! (Not in Scotland)");
				if (logFile != null)
					logFile.getWriter().format("%s\n", cph);
			}
		}
		

		if (logFile != null) logFile.getWriter().format("\nRemoved movements (src/mkt/rcp not in Scotland):\n");

		// get rid of movements not within/to Scotland
		for (Iterator<String> i = movementsData.getMovementsIterator(); i.hasNext(); ) {
			String key = (String) i.next();
			MovementsDataRecord record = movementsData.getRecord(key);

			boolean b = false;
			if (premisesData.getRecord(record.srcCPH) == null) {
				System.out.print("SRC");
				b = true;
			}
			if (!record.mktCPH.equals("")) {
				if (premisesData.getRecord(record.mktCPH) == null) {
					if (b) System.out.print(", ");
					System.out.print("MKT");
					b = true;
				}
			}
			if (premisesData.getRecord(record.rcpCPH) == null) {
				if (b) System.out.print(", ");
				System.out.print("RCP");
				b = true;
			}
			

			if (b) {
				System.out.println(" are not in the dataset: " + record.toString());
				i.remove();
				if (logFile != null)
					logFile.getWriter().format("%s\n", record.toString());
			}
		}
		
		System.out.println(count + " premises were removed as they didn't take part in transmission process.");
	}
	
	
	public static void checkMarkets(OldPremisesData premisesData, MovementsData movementsData) {
		int count = 0;
		TreeSet<String> fakeFarms = new TreeSet<String>();
		
		for (Iterator<String> i = movementsData.getMovementsIterator(); i.hasNext(); ) {
			String key = (String) i.next();
			MovementsDataRecord mov = movementsData.getRecord(key);
			String pattern;

			if (!premisesData.getRecord(mov.srcCPH).Location_Type.equals("F")) {
//				System.out.println("src " + mov.srcCPH + " is not a farm! " + premisesData.getRecord(mov.srcCPH).Location_Type);
				if (premisesData.getRecord(mov.srcCPH).Location_Type.equals("M")) {
//					System.err.println("MARKET!");
					fakeFarms.add(mov.srcCPH);
				}
				count++;
			}
			if (!mov.mktCPH.isEmpty()) {
				if (!premisesData.getRecord(mov.mktCPH).Location_Type.equals("M")) {
//					System.out.println("mkt " + mov.mktCPH + " is not a market! " + premisesData.getRecord(mov.mktCPH).Location_Type);
					count++;
				}
				pattern = premisesData.getRecord(mov.srcCPH).Location_Type + premisesData.getRecord(mov.mktCPH).Location_Type + premisesData.getRecord(mov.rcpCPH).Location_Type;
			}
			else
				pattern = premisesData.getRecord(mov.srcCPH).Location_Type + "-" + premisesData.getRecord(mov.rcpCPH).Location_Type;
			
			if (!premisesData.getRecord(mov.rcpCPH).Location_Type.equals("F")) {
//				System.out.println("rcp " + mov.rcpCPH + " is not a farm! " + premisesData.getRecord(mov.rcpCPH).Location_Type);
				if (premisesData.getRecord(mov.rcpCPH).Location_Type.equals("M")) {
//					System.err.println("MARKET!");
					fakeFarms.add(mov.rcpCPH);
				}
				count++;
			}
			
			if (!pattern.equals("FMF") && !pattern.equals("F-F") && !pattern.equals("F-X") && !pattern.equals("FMX") && !pattern.equals("XMF") && !pattern.equals("X-F") && !pattern.equals("X-X") && !pattern.equals("XMX")) {
				System.err.println(pattern + ":\t" + mov);
				if (logFile != null)
					logFile.getWriter().format("%s\t%s\n", pattern, mov);
				i.remove(); // REMOVE ALL THE MOVEMENTS THAT DO NOT FOLLOW THE PATTERNS F-F, FMF (X instead of F is ok).
			}
		}
		System.out.println(fakeFarms.size() + ": " + fakeFarms);
		
		for (Iterator<String> i = movementsData.getMovementsIterator(); i.hasNext(); ) {
			String key = (String) i.next();
			MovementsDataRecord mov = movementsData.getRecord(key);
			
			if (fakeFarms.contains(mov.srcCPH) || fakeFarms.contains(mov.rcpCPH))
				;
//				System.out.println(mov);
		}
		
//		System.out.println("In total: " + count + " movements with inconsistencies.");
	}
	
	public static void checkIndexCases(OldPremisesData premisesData, InfectionsData infectionsData) {
		ArrayList<InfectionsDataRecord> toDelete = new ArrayList<InfectionsData.InfectionsDataRecord>();
		
		for (InfectionsDataRecord record : infectionsData.getData()) {
			if (premisesData.getRecord(record.CPH) == null)
				toDelete.add(record);
			else if (!premisesData.getRecord(record.CPH).Location_Type.equals("F"))
				toDelete.add(record);
		}
		
		for (InfectionsDataRecord record : toDelete)
			infectionsData.getData().remove(record);
	}
	
	public static void generateLocations(int x, int y, String fileName) {		
		FileOutput outputFile = new FileOutput(fileName);
		outputFile.getWriter().format("CPH,Location_Type,Location_Easting,Location_Northing,Census_Sheep\n");
		
		for (int i = 0; i < x * y; i++) {
			String cph = "F" + String.valueOf(i);
			String type = "F";
			int east;
			int north;
			int sheep = 10;
			
			east = (i / x) * 1000;
			north = (i - i / x * y) * 1000;
			
			outputFile.getWriter().format("%s,%s,%d,%d,%d\n", cph, type, east, north, sheep);
		}
		
		outputFile.close();
	}
	
	public static void comparePremisesData(OldPremisesData premisesData, FarmsData newPremisesData) {
		FileOutput fileOutput = new FileOutput("output/diffLoc.gv", false);
		fileOutput.getWriter().format("digraph G {\ngraph [center, rankdir=LR];\nsize=\"50,50\";\n");
		fileOutput.getWriter().format("fontsize = 30\n");
		fileOutput.getWriter().format("node [" + /*label=\"\", */ "color=gray, style=filled" + //, fillcolor=white" +
				", fontname = \"Helvetica\", fontsize = 24, shape=circle]\n");
		
		double SCALE = 100;
		
		int count = 0, diffLoc = 0;
		for (Iterator<String> iterator = newPremisesData.getIterator(); iterator.hasNext(); ) {
			String cph = (String) iterator.next();
			
			FarmsDataRecord rec = newPremisesData.getRecord(cph);
			
			cph = cph.substring(0, Math.max(cph.indexOf("_"), cph.length()));
			
			
			OldPremisesDataRecord rec2;
			if ((rec2 = premisesData.getRecord(cph)) != null) {
				count++;
				double dist = Math.sqrt(((double) rec.Location_Easting - rec2.Location_Easting) * (rec.Location_Easting - rec2.Location_Easting) + 
						((double) rec.Location_Northing - rec2.Location_Northing) * (rec.Location_Northing - rec2.Location_Northing));

				rec2.Census_Sheep = rec.Census_Sheep;
				rec2.Census_Cattle = rec.Census_Cattle;
				
				if (dist == 0.0)
					continue;
//				System.err.println(cph + "\t" + dist);
				System.err.println(cph + ": was (" + rec2.Location_Easting + ", " + rec2.Location_Northing + 
						"), now (" + rec.Location_Easting + ", " + rec.Location_Northing + ")\t" + dist);
				diffLoc++;
				
				fileOutput.getWriter().format("node [color = gray]\n"); 

				fileOutput.getWriter().format("\"" + cph + "_1\"[pos=\"" + (double) rec2.Location_Easting / SCALE + "," + (double) rec2.Location_Northing / SCALE + "!\"]\n");
				fileOutput.getWriter().format("\"" + cph + "_2\"[pos=\"" + (double) rec.Location_Easting / SCALE + "," + (double) rec.Location_Northing / SCALE + "!\"]\n");
				
				
				fileOutput.getWriter().format("edge [penwidth = " + 1 + ", arrowsize = 15, color=blue] ");
				fileOutput.getWriter().format("\"" + cph + "_1" + "\" -> \"" + cph + "_2"  + "\"\n");
				
				
			}
		}
		
		fileOutput.getWriter().format("}");
		fileOutput.close();
		
		newPremisesData.writeToFile("input/cs/1.csv");

		System.out.println(count + " matches.");
		System.out.println(diffLoc + " different locations.");
	}
	
	/**
	 * Prepares data by making sure it's correct.
	 * @param farmsData
	 * @param marketsData
	 * @param movementsData
	 */
	public static void prepareData(FarmsData farmsData, MarketsData marketsData, MovementsData movementsData) {
		ArrayList<MovementsDataRecord> movsToDel = new ArrayList<MovementsDataRecord>();
		ArrayList<String> mkts = new ArrayList<String>();
		
		int countS = 0, countR = 0, countDirect = 0;

		// Iterate movements
		for (Iterator<String> i = movementsData.getMovementsIterator(); i.hasNext(); ) {
			String key = (String) i.next();
			MovementsDataRecord record = movementsData.getRecord(key);
			boolean bDel = false, bMarket = true;

			if (farmsData.getRecord(record.srcCPH) == null) {
				System.err.println("Error: " + record.srcCPH + " is not in farms data." + " Movement " + record + " will be deleted!");
				JOptionPane.showMessageDialog(null, "Error: " + record.srcCPH + " is not in farms data." + " Movement " + record + " will be deleted!");
				countS++;
				bDel = true;
			}
			if (!record.mktCPH.equals("")) {
				if (marketsData.getRecord(record.mktCPH) == null) {
					System.err.println("Error: " + record.mktCPH + " is not in markets data.");
					JOptionPane.showMessageDialog(null, "Error: " + record.mktCPH + " is not in markets data." + " Movement " + record + " will be deleted!");
//					count++;
					bDel = true;
				}
				if (!mkts.contains(record.mktCPH))
					mkts.add(record.mktCPH);
			} else {
				countDirect++;
				bMarket = false;
			}
			
			if (farmsData.getRecord(record.rcpCPH) == null) {
				System.err.println("Error: " + record.rcpCPH + " is not in farms data." + " Movement " + record + " will be deleted!");
				JOptionPane.showMessageDialog(null, "Error: " + record.rcpCPH + " is not in farms data." + " Movement " + record + " will be deleted!");
				countR++;
				bDel = true;
				if (!bMarket)
					countDirect--;
			}
			
			if (bDel)
				movsToDel.add(record);
		}
		
		for (MovementsDataRecord rec : movsToDel) {
			movementsData.removeRecord(rec);
		}
		
		
		System.out.println(countS + "  / " + countR + "  / " +  "movements contained incorrect CPH(s).");
		System.out.println(movementsData.getSize() + " movements left.");
		System.out.println(countDirect + " direct movements.");
		
//		System.out.println(mkts + ": " + mkts.size());
		
		
		System.out.println(farmsData.getSize() + " farms.");
		
	}

	public static void checkIndexCases(FarmsData farmsData,
			InfectionsData infectionsData) {
		ArrayList<InfectionsDataRecord> toDelete = new ArrayList<InfectionsData.InfectionsDataRecord>();
		
		for (InfectionsDataRecord rec : infectionsData.getData()) {
			if (farmsData.getRecord(rec.CPH) == null) {
				System.err.println(rec.CPH + "\tis not in the farms data!");
				toDelete.add(rec);
			}
		}
		
		for (InfectionsDataRecord rec : toDelete)
			infectionsData.getData().remove(rec);
	}
}
