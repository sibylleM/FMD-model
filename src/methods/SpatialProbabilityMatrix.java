package methods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import utils.FileOutput;
import data.MovementsData;
import data.MovementsData.MovementsDataRecord;
import data.OldPremisesData;
import data.OldPremisesData.OldPremisesDataRecord;
import data.FarmsData;
import data.FarmsData.FarmsDataRecord;

public class SpatialProbabilityMatrix {

	// Parameters from Tildesley et al.
//	private static final double DISTANCE_THRESHOLD = 60000;
//	private static final double DELTA = 710;
//	private static final double B = 1.66;
	
	// Parameters from Chis Ster et al.
//	private static final double DISTANCE_THRESHOLD = 60000;
//	private static final double DELTA = 1471;
//	private static final double B = 2.72;
	

	public static double DISTANCE_THRESHOLD;
	public static double DELTA;
	public static double B;
	public static double CLOSE_DISTANCE;
	public static double SURVEILLANCE_DISTANCE;
	
	public static int KERNEL;

	private static HashMap<String, Double> spMatrix;
	private static HashMap<String, Double> distMatrix;
	
	private static HashMap<String, ArrayList<String>> closeFarms, surveillanceZoneFarms;

	public static void init() {
//		spMatrix = new HashMap<String, Double>();
//		distMatrix = new HashMap<String, Double>();
//		
//		closeFarms = new HashMap<String, ArrayList<String>>();
//		surveillanceZoneFarms = new HashMap<String, ArrayList<String>>();

		KERNEL = 0;
		DELTA = 1471; 
		B = 2.72;
		DISTANCE_THRESHOLD = 10000;
		CLOSE_DISTANCE = 500;
		SURVEILLANCE_DISTANCE = 10000;
	}

//	public static void init(FarmsData premisesData) {
//		
////		JOptionPane.showMessageDialog(null, "" + B + ", " + DELTA + ", " + DISTANCE_THRESHOLD);
//		
//		update(premisesData);
//	}

	private static double calculateDistance(FarmsDataRecord srcPremisesRecord, FarmsDataRecord rcpPremisesRecord) {
		double dX = srcPremisesRecord.getX() - rcpPremisesRecord.getX();
		double dY = srcPremisesRecord.getY() - rcpPremisesRecord.getY();
		
		return Math.sqrt(dX * dX + dY * dY);
	}
	
	private static double calculateDistanceKernel(double distance) {		
		if (distance > DISTANCE_THRESHOLD)
			return 0.0;
		
		
		switch (KERNEL) {
			case 0: // Chis Ster et al.
				return Math.pow(1 + distance / DELTA, -B);
			case 1: // Tildesley et al.
				return (distance < DELTA) ? 1.0 : Math.pow(DELTA / distance, B);
			case 2: // Green et al.
				return Math.exp(-B * distance / 1000);
			default:
				return 0;
		}
	}
	
	public static double getProbability(String srcCPH, String rcpCPH) {
		double res = 0;
		String str = hashCode(srcCPH, rcpCPH);
		
		if (spMatrix.containsKey(str))
			res = 1 - Math.exp(-spMatrix.get(str));
		else
			res = 0.0;
		
		return res;
	}
	
	private static String hashCode(String srcCPH, String rcpCPH) {
		return srcCPH + rcpCPH; // not the perfect hash-function...
	}
	
	@Deprecated
	public static void generateNewPremisesFile(OldPremisesData premisesData, MovementsData movementsData) {
		TreeSet<String> cphsUsed = new TreeSet<String>();
		FileOutput fileOutput = new FileOutput("input/scotland/premises.csv");
		
		for (Iterator<String> i = movementsData.getMovementsIterator(); i.hasNext(); ) {
			String key = (String) i.next();
			MovementsDataRecord record = movementsData.getRecord(key);

			if (!cphsUsed.contains(record.srcCPH))
				cphsUsed.add(record.srcCPH);
			if (!record.mktCPH.equals("")) {
				if (!cphsUsed.contains(record.mktCPH))
					cphsUsed.add(record.mktCPH);
			}
			if (!cphsUsed.contains(record.rcpCPH))
				cphsUsed.add(record.rcpCPH);
		}
		
		System.out.println(cphsUsed.size() + " premises were mentioned in movements files!");
		
		fileOutput.getWriter().format("CPH,Location_Type,Location_Easting,Location_Northing,Census_Sheep\n");
		
		for (Iterator<String> i = premisesData.getIterator(); i.hasNext(); ) {
			String cph = (String) i.next();
			OldPremisesDataRecord record = premisesData.getRecord(cph);
			
//			if (record.Census_Sheep > 0)
//				continue;
			
			if (!cphsUsed.contains(cph))
				continue;
			
//			if (record.Location_Type.equals("X"))
//				continue;
			
			fileOutput.getWriter().format("%s,%s,%s,%s,%s\n", cph, record.Location_Type, record.Location_Easting, record.Location_Northing, record.Census_Sheep);
		}
		
		fileOutput.close();
	}
	
	
	private static void addClosePair(String srcCPH, String rcpCPH, HashMap<String, ArrayList<String>> farms) {
		if (farms.containsKey(srcCPH)) {
			farms.get(srcCPH).add(rcpCPH);
		} else {
			ArrayList<String> list = new ArrayList<String>();
			list.add(rcpCPH);
			farms.put(srcCPH, list);
		}
		
//		if (farms.containsKey(rcpCPH)) {
//			farms.get(rcpCPH).add(srcCPH);
//		} else {
//			ArrayList<String> list = new ArrayList<String>();
//			list.add(srcCPH);
//			farms.put(rcpCPH, list);
//		}
	}
	
//	@Deprecated
//	public ArrayList<String> getCloseFarms(String CPH, double radius, PremisesData premisesData) {
//		ArrayList<String>  res = new ArrayList<String>();
//		
//		for (Iterator i = premisesData.getIterator(); i.hasNext(); ) {
//			String tmpCPH = (String) i.next();
//			
//			if (tmpCPH.equals(CPH)) continue;
//			
//			String key = hashCode(tmpCPH, CPH);
//			
//			if (spMatrix.containsKey(key)) {
//				if (spMatrix.get(key) < radius)
//					res.add(tmpCPH);
//			} else {
////				System.out.println("Ooops! " + tmpCPH + ", " + CPH);
////				System.exit(0);
//			}
//		}
//		
//		return res;
//	}
	
	public static ArrayList<String> getCloseFarms(String CPH) {
		ArrayList<String> res = new ArrayList<String>();
		if (closeFarms.get(CPH) != null)
			res.addAll(closeFarms.get(CPH));
		return res;
	}
	
	public static ArrayList<String> getFarmsInSurveillanceZone(String CPH) {
		ArrayList<String> res = new ArrayList<String>();
		if (surveillanceZoneFarms.get(CPH) != null)
			res.addAll(surveillanceZoneFarms.get(CPH));
		return res;
	}
	
	public static void printDistanceKernel(double step, double max) {
		double cur = 0;
		while (cur <= max) {
			System.out.println("" + cur + "\t" + calculateDistanceKernel(cur));
			cur += step;
		}
	}
	
	public static ArrayList<Double> listDistanceKernel(double step, double max) {
		double cur = 0;
		ArrayList<Double> res = new ArrayList<Double>();
		while (cur <= max) {
			res.add(calculateDistanceKernel(cur));
			cur += step;
		}
		return res;
	}
	
	public static void printNumOfCloseFarms() {
		for (Iterator<String> iter = closeFarms.keySet().iterator(); iter.hasNext(); ) {
			String CPH = iter.next();			
			int num = closeFarms.get(CPH).size();
			System.out.println("" + CPH + "\t" + num);
		}
	}
	
	public static void printMeanNumOfCloseFarms() {
		int sum = 0;
		for (Iterator<String> iter = closeFarms.keySet().iterator(); iter.hasNext(); ) {
			String CPH = iter.next();			
			int num = closeFarms.get(CPH).size();
			sum += num;
		}

		System.out.println("Mean number of closely situated farms:" + "\t" + sum / closeFarms.keySet().size());
	}
	
	public static void printProbabilities() {
		for (Iterator<String> iter = spMatrix.keySet().iterator(); iter.hasNext(); ) {
			String CPH = iter.next();			
			double prob = spMatrix.get(CPH);
			System.out.println("" + prob);
		}
	}
	
	public static void update(FarmsData premisesData) {		
		spMatrix = new HashMap<String, Double>();
		distMatrix = new HashMap<String, Double>();
		closeFarms = new HashMap<String, ArrayList<String>>();
		surveillanceZoneFarms = new HashMap<String, ArrayList<String>>();
		
		System.out.print("Started calculating the spatial matrix...");
		
		for (Iterator<String> i = premisesData.getIterator(); i.hasNext(); ) {
			String srcCPH = (String) i.next();
			FarmsDataRecord srcPremisesRecord = premisesData.getRecord(srcCPH);
			
//			if (srcPremisesRecord.Location_Type.contains("M")) // skip markets
//				continue;
		
			for (Iterator<String> j = premisesData.getIterator(); j.hasNext(); ) {
				String rcpCPH = (String) j.next();
				FarmsDataRecord rcpPremisesRecord = premisesData.getRecord(rcpCPH);
				
//				if (rcpPremisesRecord.Location_Type.contains("M")) // skip markets
//					continue;
				
				if (srcCPH.equals(rcpCPH))
					continue;
				
				double distance = calculateDistance(srcPremisesRecord, rcpPremisesRecord);
				
				if (distance > DISTANCE_THRESHOLD)
					continue;

				if (distance <= CLOSE_DISTANCE)
					addClosePair(srcCPH, rcpCPH, closeFarms);
				
				if (distance <= SURVEILLANCE_DISTANCE)
					addClosePair(srcCPH, rcpCPH, surveillanceZoneFarms);				
				
				double probability = calculateDistanceKernel(distance);
				
				probability *= getSpeciesRate(premisesData.getRecord(srcCPH), premisesData.getRecord(rcpCPH));
				
				String key = hashCode(srcCPH, rcpCPH);
				
				if (spMatrix.containsKey(key)) {
					System.err.println("WARNING: for " + srcCPH + " and " + rcpCPH + " we already have a record in spatial probability matrix, key: " + key);
				} else {
					spMatrix.put(key, probability);
					distMatrix.put(key, distance);
//					spMatrix.put(key, distance);
//					System.out.println("For " + srcCPH + " and " + rcpCPH + " the probability of local infection: " + probability);
				}
//				System.out.println(srcCPH + " -> " + rcpCPH + ": " + probability);
			}
		}
		
		System.out.println(" done!");
//		printMeanNumOfCloseFarms();
	}
	

	private static double getSpeciesRate(FarmsDataRecord srcRec, FarmsDataRecord rcpRec) {
		// Savill et al. 2007
		/*private final double Tsheep = 11.1 * Math.pow(10, -6);
		private final double Scattle = 7.14;
		private final double Tcattle = 6.34 * Math.pow(10, -6);
		private final double ps = 1.0;
		private final double pc = 1.0;
		private final double qs = 1.0;
		private final double qc = 1.0;*/

		// Tildesley et al. 2008 (Rest of England)
//		final double Tsheep = 23.2 * Math.pow(10, -4);
//		final double Scattle = 2.3;
//		final double Tcattle = 8.2 * Math.pow(10, -4);
//		final double ps = 0.3;
//		final double pc = 0.42;
//		final double qs = 0.37;
//		final double qc = 0.44;
		
		// Tildesley, Keeling, 2009. Is R0...? (Scotland) WRONG
//		final double Tsheep = 9.7 * Math.pow(10, -4);
//		final double Scattle = 10.8;
//		final double Tcattle = 8.4 * Math.pow(10, -4);
//		final double ps = 0.33;
//		final double pc = 0.23;
//		final double qs = 0.40;
//		final double qc = 0.20;
		
		// Tildesley 2008, Accuracy
		final double Tsheep = 28.2 * Math.pow(10, -4);
		final double Scattle = 10.2;
		final double Tcattle = 23.2 * Math.pow(10, -4);
		final double ps = 0.33;
		final double pc = 0.23;
		final double qs = 0.40;
		final double qc = 0.20;

		// Tildesley, Keeling. 2009 (Rest of England)
		/*private final double Tsheep = 9.9 * Math.pow(10, -7);
		private final double Scattle = 8.0;
		private final double Tcattle = 11.9 * Math.pow(10, -7);
		private final double ps = 0.3;
		private final double pc = 0.42;
		private final double qs = 0.37;
		private final double qc = 0.44;*/
		
		
		double rate = 1.0;
		
		rate *= Math.pow(srcRec.Census_Sheep, ps) * 1 +  Math.pow(srcRec.Census_Cattle, pc) * Scattle;
		rate *= Math.pow(rcpRec.Census_Sheep, qs) * Tsheep +  Math.pow(rcpRec.Census_Cattle, qc) * Tcattle;
		
		
		return rate;
	}

	public static double getDistance(String srcCPH, String rcpCPH) {
		double res = 0;
		String str = hashCode(srcCPH, rcpCPH);
		
		if (distMatrix.containsKey(str))
			res = distMatrix.get(str);
		else
			res = 0.0;
		
		return res;
	}
}
