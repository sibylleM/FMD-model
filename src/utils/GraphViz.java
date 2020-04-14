package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

import data.FarmsData.FarmsDataRecord;
import data.MarketsData;
import data.MarketsData.MarketsDataRecord;
import data.MovementsData;
import data.OldPremisesData;
import data.MovementsData.MovementsDataRecord;
import data.OldPremisesData.OldPremisesDataRecord;
import data.FarmsData;


public class GraphViz {

	final static int SCALE = 300; //800;
	

	public static void drawMap(FarmsData farmsData, MarketsData marketsData,
			ArrayList<String> susceptiblePremises, 
			HashMap<String, Integer> restrictedPremises, 
			HashMap<String, Integer> exposedPremises, //HashMap<String, Integer>
			HashMap<String, Integer> infectiousPremises, 
			ArrayList<String> culledPremises, 
			ArrayList<MovementsDataRecord> dailyMovements, 
			String infoText,
			String fileName) {
		
		System.out.print("Generating map... ");
		FileOutput fileOutput = new FileOutput(fileName, false);
//		fileOutput.getWriter().format("digraph G {\ngraph [center, rankdir=LR];\nsize=\"50,50\";\n");
		fileOutput.getWriter().format("digraph G {\nsize=\"50,50\";\n");
//		fileOutput.getWriter().format("label = \"Legend:\\n1. First line.\\n2. Second line.\"\n labelloc = \"c\"\n");
		fileOutput.getWriter().format("label = \"%s\"\n labelloc = \"c\"\n", infoText);
		
		fileOutput.getWriter().format("fontsize = 30\n");
//		fileOutput.getWriter().format("label = \"Legend:\\n");
//		for (int key : STToColor.keySet()) {
//			fileOutput.getWriter().format("<font color=" + STToColor.get(key) + ">ST " + key + ": " + STToColor.get(key) + "</font>\\n");
//		}
//		fileOutput.getWriter().format("\"");
//		fileOutput.getWriter().format("label = \"FMD map\"\n");
		
//		fileOutput.getWriter().format("label = <<TABLE border=\"1\" cellborder=\"1\"><TR><TD>");
//		for (int key : STToColor.keySet()) {
//			fileOutput.getWriter().format("<font color=\"" + STToColor.get(key) + "\">ST" + key + "</font> ");//<br/>");
//		}
//		fileOutput.getWriter().format("</TD></TR></TABLE>>");
		// See: http://www.graphviz.org/content/node-shapes#html
//		fileOutput.getWriter().format("label = <<TABLE border=\"1\" cellborder=\"1\"><TR><TD><IMG SRC=\"image_name.jpg\"/></TD></TR><TR><TD><font point-size=\"80\">first line</font><br/><font point-size=\"40\">second line</font></TD></TR></TABLE>>");
		fileOutput.getWriter().format("node [" + /*label=\"\", */ "color=gray, style=filled" + //, fillcolor=white" +
				", fontname = \"Helvetica\", fontsize = 24, shape=circle]\n");
		processSpatialData(farmsData, marketsData, susceptiblePremises, restrictedPremises, exposedPremises, infectiousPremises, culledPremises, fileOutput);
//		fileOutput.getWriter().format("edge [color = \"black\", arrowsize = 4, penwidth = 1]\n");
		
//		if (movementsData != null)
//			processMovementsData(movementsData, herdsData, startMonth, endMonth, fileOutput);
		
		processMovementsData(dailyMovements, fileOutput);
		
		fileOutput.getWriter().format("}");
		fileOutput.close();
		System.out.println("... finished!");
	}
	

	private static void processSpatialData(FarmsData farmsData, MarketsData marketsData,
			ArrayList<String> susceptiblePremises, 
			HashMap<String, Integer> restrictedPremises, 
			HashMap<String, Integer> exposedPremises, 
			HashMap<String, Integer> infectiousPremises, ArrayList<String> culledPremises, FileOutput fileOutput) {
		int countF = 0, countM = 0;
		
		for (Iterator<String> iterator = farmsData.getIterator(); iterator.hasNext(); ) {
			String cph = (String) iterator.next();
			
			FarmsDataRecord record = farmsData.getRecord(cph);

			String infStateStr;
			
			if (culledPremises.contains(cph)) // should be first
				infStateStr = "color=black";
			else if (infectiousPremises.containsKey(cph))
				infStateStr = "color=red";
			else if (susceptiblePremises.contains(cph)) {
				if (record.Census_Cattle + record.Census_Sheep == 0)
					infStateStr = "color=pink";
				else
					infStateStr = "color=gray";
			} else if (restrictedPremises.containsKey(cph))
				infStateStr = "color=cyan";
			else if (exposedPremises.containsKey(cph))
				infStateStr = "color=green";
			else
				infStateStr = "color=brown"; // unknown case (confirmed IP)
			
			fileOutput.getWriter().format("node [" + infStateStr + ", shape=circle]\n"); 			
			fileOutput.getWriter().format("\"" + cph + "\"[pos=\"" + (double) record.getX() / SCALE + "," + (double) record.getY() / SCALE + "!\"]\n");
			countF++;
		}

		fileOutput.getWriter().format("node [color=blue, shape=diamond]\n"); 
		
		// MARKETS
		for (Iterator<String> iterator = marketsData.getIterator(); iterator.hasNext(); ) {
			String cph = (String) iterator.next();
			MarketsDataRecord record = marketsData.getRecord(cph);
			fileOutput.getWriter().format("\"" + cph + "\"[pos=\"" + (double) record.getX() / SCALE + "," + (double) record.getY() / SCALE + "!\"]\n");
			countM++;
		}
		
		
		System.out.println("\n\tGenerated a map with " + countF + " farms and " + countM + " markets.");
	}
	
	
	private static void processMovementsData(ArrayList<MovementsDataRecord> dailyMovements, FileOutput fileOutput) {		
		int count = 0;
		
		ArrayList<String> was = new ArrayList<String>();

		for (MovementsDataRecord movement : dailyMovements) {		
			final int BATCH_DENOMINATOR = 50;
			
			if (movement.mktCPH.isEmpty()) {
				if (!was.contains(movement.srcCPH + movement.rcpCPH)) {
					fileOutput.getWriter().format("edge [penwidth = " + ((double) movement.size) / BATCH_DENOMINATOR + ", arrowsize = 1, color=black] "); 
					fileOutput.getWriter().format("\"" + movement.srcCPH + "\" -> \"" + movement.rcpCPH + "\"\n");
				}
			}
			else {
				if (!was.contains(movement.srcCPH + movement.mktCPH)) {
					fileOutput.getWriter().format("edge [penwidth = " + ((double) movement.size) / BATCH_DENOMINATOR + ", arrowsize = 1, color=blue] ");
					fileOutput.getWriter().format("\"" + movement.srcCPH + "\" -> \"" + movement.mktCPH + "\"\n");
					was.add(movement.srcCPH + movement.mktCPH);
				}
				
				if (!was.contains(movement.mktCPH + movement.rcpCPH)) {
					fileOutput.getWriter().format("edge [penwidth = " + ((double) movement.size) / BATCH_DENOMINATOR + ", arrowsize = 1, color=cyan] ");
					fileOutput.getWriter().format("\"" + movement.mktCPH + "\" -> \"" + movement.rcpCPH + "\"\n");
					was.add(movement.mktCPH + movement.rcpCPH);
				}
			}
				
			count++;
		}
		System.out.print("Farm to farm movements: " + count + ". ");
	}

	
	
	
	
	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	// AVERAGED PLOTS
	public static void drawHeatMap(OldPremisesData premisesData, TreeMap<String, Integer> infPremises, int samples, String fileName) {
		System.out.print("Generating map... ");
		FileOutput fileOutput = new FileOutput(fileName, false);
		fileOutput.getWriter().format("digraph G {\ngraph [center, rankdir=LR];\nsize=\"100,100\";\n");
//		fileOutput.getWriter().format("label = \"Legend:\\n1. First line.\\n2. Second line.\"\n labelloc = \"c\"\n");
		fileOutput.getWriter().format("fontsize = 30\n");
//		fileOutput.getWriter().format("label = \"Legend:\\n");
//		for (int key : STToColor.keySet()) {
//			fileOutput.getWriter().format("<font color=" + STToColor.get(key) + ">ST " + key + ": " + STToColor.get(key) + "</font>\\n");
//		}
//		fileOutput.getWriter().format("\"");
//		fileOutput.getWriter().format("label = \"FMD map\"\n");
		
//		fileOutput.getWriter().format("label = <<TABLE border=\"1\" cellborder=\"1\"><TR><TD>");
//		for (int key : STToColor.keySet()) {
//			fileOutput.getWriter().format("<font color=\"" + STToColor.get(key) + "\">ST" + key + "</font> ");//<br/>");
//		}
//		fileOutput.getWriter().format("</TD></TR></TABLE>>");
		// See: http://www.graphviz.org/content/node-shapes#html
//		fileOutput.getWriter().format("label = <<TABLE border=\"1\" cellborder=\"1\"><TR><TD><IMG SRC=\"image_name.jpg\"/></TD></TR><TR><TD><font point-size=\"80\">first line</font><br/><font point-size=\"40\">second line</font></TD></TR></TABLE>>");
		fileOutput.getWriter().format("node [" + /*"label=\"\"," +*/ "color=gray, style=filled" + //, fillcolor=white" +
				", fontname = \"Helvetica\", fontsize = 24, shape=circle]\n");
		processNodes(premisesData, infPremises, samples, fileOutput);
//		fileOutput.getWriter().format("edge [color = \"black\", arrowsize = 4, penwidth = 1]\n");
		
//		if (movementsData != null)
//			processMovementsData(movementsData, herdsData, startMonth, endMonth, fileOutput);
		
		fileOutput.getWriter().format("}");
		fileOutput.close();
		System.out.println("... finished!");
	}


	// AVERAGED PLOTS
	private static void processNodes(OldPremisesData premisesData, TreeMap<String, Integer> infTimes, int samples, FileOutput fileOutput) {
		int count = 0;
		
		for (Iterator<String> iterator = premisesData.getIterator(); iterator.hasNext(); ) {
			String cph = (String) iterator.next();
			
			OldPremisesDataRecord record = premisesData.getRecord(cph);

			String infStateStr;
			boolean bInf = infTimes.containsKey(cph);
			if (bInf) {
				double hue = 0.0 + 0.0 * ((double) infTimes.get(cph)) / samples;
				double saturation = 0.1 + 0.9 * (((double) infTimes.get(cph)) / samples);
				double value = 1.0;
				
//				value = Integer.valueOf(cph.substring(0, 2)) / 100.0;
				
				infStateStr = String.format("color=\"%.3f %.3f %.3f\", style=filled", hue, saturation, value);
				
				if (Integer.valueOf(cph.substring(0, 2)) > 50)
					infStateStr = "color=\"red\", style=filled";
				else
					infStateStr = "color=\"blue\", style=filled";
			} else
				infStateStr = "color=black, style=solid";
			
			String typeStr;
			if (record.Location_Type.equals("F"))
				typeStr = "shape=circle";
			else if (record.Location_Type.equals("M"))
				typeStr = "shape=diamond, ";
			else if (record.Location_Type.equals("X"))
				typeStr = "shape=square";
			else
				typeStr = "shape=triangle";
			
			if (!bInf)
				typeStr = "shape=point";
			
			fileOutput.getWriter().format("node [" + infStateStr + ", " + typeStr + "]\n"); 
			
			fileOutput.getWriter().format("\"" + cph + "\"[pos=\"" + (double) record.getX() / SCALE + "," + (double) record.getY() / SCALE + "!\"]\n");
			count++;
		}
		System.out.println("\n\tGenerated a map with " + count + " nodes.");
	}
	
	
	// GEPHI
	public static void generateCSVforGephi(OldPremisesData premisesData, MovementsData movementsData, TreeMap<String, Integer> infTimes, int samples, String fileName) {
		System.out.println("Started generating CSV files for a map of movements... ");
		FileOutput fileOutput = new FileOutput(fileName.substring(0, fileName.length() - 4) + "-nodes.csv");
		fileOutput.getWriter().format("label, x, y, infected, inMovs, sheep\n");
		
		int nodes = 0;
//		int skippedNodes = 0;
//		int susceptibleNodes = 0;
//		int unknownNodes = 0;
//		int infectedNodes = 0;
		
		for (Iterator<String> iterator = premisesData.getIterator(); iterator.hasNext(); ) {
			String cph = (String) iterator.next();
			
			OldPremisesDataRecord record = premisesData.getRecord(cph);

			// DON'T PLOT EMPTY FARMS!
			if (record.Census_Sheep == 0)
				continue;
			
			String infStateStr;
			boolean bInf = infTimes.containsKey(cph);
			if (bInf) {
//				double hue = 0.0 + 0.0 * ((double) infTimes.get(cph)) / samples;
//				double saturation = 0.1 + 0.9 * (((double) infTimes.get(cph)) / samples);
//				double value = 1.0;
//				infStateStr = String.format("color=\"%.3f %.3f %.3f\", style=filled", hue, saturation, value);
				infStateStr = "" + infTimes.get(cph);
			} else
				infStateStr = "0";
			
			String typeStr = "";
//			if (record.Location_Type.equals("F"))
//				typeStr = "shape=circle";
//			else if (record.Location_Type.equals("M"))
//				typeStr = "shape=diamond, ";
//			else if (record.Location_Type.equals("X"))
//				typeStr = "shape=square";
//			else
//				typeStr = "shape=triangle";
//			
//			if (!bInf)
//				typeStr = "shape=point";
			
			fileOutput.getWriter().format(cph + "," + (double) record.getX() / SCALE + "," + (double) record.getY() / SCALE + "," 
					+ infStateStr + typeStr + "," + movementsData.getNumberOfInMovements(cph) + "," + movementsData.getNumberOfInSheep(cph) + "\n");
			
			nodes++;
		}
		System.out.println("\tGenerated CSV datafile on " + nodes + " nodes.");
		fileOutput.close();
		
		
		// MOVEMENTS (EDGES)

//		int edges = 0;
//		int skippedEdges = 0;
//		fileOutput = new FileOutput(fileName.substring(0, fileName.length() - 4) + "-edges.csv");
//		fileOutput.getWriter().format("source,target,number\n");
//		for (Iterator iterator = movementsData.getMovementsIterator(); iterator.hasNext(); ) {			
//			int i = (Integer) iterator.next();
//			MovementsDataRecord movement = movementsData.getRecord(i);
//			
//			// we only consider the dates from the specified period
//			if (movement.mov_month < startMonth || movement.mov_month > endMonth)
//				continue;
//
//			// we do not consider movements to the same farm
//			if (movement.anon_herd == movement.anon_herd_from) {
//				skippedEdges++;
//				continue;
//			}
//			
//			// missing src/dest?
//			if (movement.anon_herd_from == 0) {
//				skippedEdges++;
//				continue;
//			}
//			if (movement.anon_herd == 0) {
//				skippedEdges++;
//				continue;
//			}
//			
//			// no src/dest in the dataset?
//			if (herdsData.findRecord(movement.anon_herd_from) == null) {
//				skippedEdges++;
//				continue;
//			}
//			if (herdsData.findRecord(movement.anon_herd) == null) {
//				skippedEdges++;
//				continue;
//			}
//			
////			if (herdsData.findRecord(movement.anon_herd).x == 0)
////				continue;
////			if (herdsData.findRecord(movement.anon_herd_from).x == 0)
////				continue;
////			if (herdsData.findRecord(movement.anon_herd).y == 0)
////				continue;
////			if (herdsData.findRecord(movement.anon_herd_from).y == 0)
////				continue;
//			
//			final int BATCH_DENOMINATOR = 5;
//			fileOutput.getWriter().format(movement.anon_herd_from + "," + movement.anon_herd + "," + ((double) movement.anon_animals.size()) / BATCH_DENOMINATOR + "\n");
////			System.out.println(movement.anon_herd_from + "," + movement.anon_herd + "," + ((double) movement.anon_animals.size()) / BATCH_DENOMINATOR);
//			edges++;
//		}
//		System.out.println("\tGenerated CSV datafile on " + edges + " edges (" + skippedEdges + " were skipped).");
//		fileOutput.close();
		
		System.out.println("... finished!");
	}
	
	

	public static void drawJustMap(OldPremisesData premisesData, String fileName) {
		System.out.print("Generating map... ");
		FileOutput fileOutput = new FileOutput(fileName);
		fileOutput.getWriter().format("digraph G {\ngraph [center, rankdir=LR];\nsize=\"100,100\";\n");
		fileOutput.getWriter().format("fontsize = 30\n");

		fileOutput.getWriter().format("node [" + /*"label=\"\"," +*/ "color=gray, style=filled" + //, fillcolor=white" +
				", fontname = \"Helvetica\", fontsize = 24, shape=circle]\n");
		int count = 0;
		
		for (Iterator<String> iterator = premisesData.getIterator(); iterator.hasNext(); ) {
			String cph = (String) iterator.next();
			
			OldPremisesDataRecord record = premisesData.getRecord(cph);

			String infStateStr;
			if (Integer.valueOf(cph.substring(0, 2)) <= 56) // England and Wales
				infStateStr = "color=\"blue\", style=filled";
//				continue;
			else //if (Integer.valueOf(cph.substring(0, 2)) > 90)
				infStateStr = "color=\"pink\", style=filled";
			
			
//			if (record.Location_Northing < 531941) {
//				System.out.println("NORTHERN: " + cph);
//				infStateStr = "color=\"red\", style=filled";
//			}
			
			String typeStr;
			if (record.Location_Type.equals("F"))
				typeStr = "shape=circle";
			else if (record.Location_Type.equals("M"))
				typeStr = "shape=diamond, ";
			else if (record.Location_Type.equals("X"))
				typeStr = "shape=square";
			else
				typeStr = "shape=triangle";
			
			fileOutput.getWriter().format("node [" + infStateStr + ", " + typeStr + "]\n"); 
			
			fileOutput.getWriter().format("\"" + cph + "\"[pos=\"" + (double) record.getX() / SCALE + "," + (double) record.getY() / SCALE + "!\"]\n");
			count++;
		}
		System.out.println("\n\tGenerated a map with " + count + " nodes.");
		
		fileOutput.getWriter().format("}");
		fileOutput.close();
		System.out.println("... finished!");
	}
}

