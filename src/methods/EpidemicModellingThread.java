package methods;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator; //Sibylle
import java.util.Set; //Sibylle
import java.util.Iterator;
import java.util.TreeMap;

import javax.swing.SwingWorker;

import utils.FileOutput;
import data.InfectionsData;
import data.MarketsData;
import data.MovementsData;
import data.FarmsData.FarmsDataRecord;
import data.FarmsData;

public class EpidemicModellingThread extends SwingWorker<Integer, Integer> {
	
    FarmsData farmsData;
    MarketsData marketsData;
    MovementsData movementsData;
    InfectionsData infectionsData;
	//	SpatialProbabilityMatrix spatialProbabilityMatrix;
    public int repeats;
    boolean bNew; 		// true/false;
    boolean bLocal; 	// true/false;
    String startDate; 	// "01/08/2011";
    int readRate; 		// 80;
    int SID;
    boolean bInfoWindows;
    boolean bDrawIndexCases;
    
    
	
	static boolean bSummaryHeader = true;
	
    String outputFile;
    static FileOutput summaryFileOutput;
	
    public EpidemicModellingThread(FarmsData premisesData, MarketsData marketsData,
								   MovementsData movementsData, InfectionsData infectionsData,
								   //			SpatialProbabilityMatrix spatialProbabilityMatrix, 
								   int repeats,
								   boolean bNew, boolean bLocal, String startDate, int readRate, int SID, boolean bInfoWindows, boolean bDrawIndexCases, String outputFile, FileOutput summaryFileOutput) {
        super();
        this.farmsData = premisesData;
        this.marketsData = marketsData;
        this.movementsData = movementsData;
        this.infectionsData = infectionsData;
		//		this.spatialProbabilityMatrix = spatialProbabilityMatrix;
        this.repeats = repeats;
        this.bNew = bNew;
        this.bLocal = bLocal;
        this.startDate = startDate;
        this.readRate = readRate;
        this.SID = SID;
        this.bInfoWindows = bInfoWindows;
        this.bDrawIndexCases = bDrawIndexCases;
        this.outputFile = outputFile;
        this.summaryFileOutput = summaryFileOutput;
    }
	// Sibylle: added sychronized to protect variable
    @Override
    protected synchronized Integer doInBackground() throws Exception {
        String SESSION_NAME;
        String sNew = bNew ? "new" : "old";
        String sLocal = bLocal ? "local" : "mov";
		
        SESSION_NAME = "" + repeats + "_" + sNew + "_" + sLocal + "_" + readRate + "_" + SID;
		
        TreeMap<String, Integer> infPremises = new TreeMap<String, Integer>();
        TreeMap<String, Integer> culledPremises = new TreeMap<String, Integer>();
		
        ArrayList<Integer> numInf = new ArrayList<Integer>();
        ArrayList<Integer> epidemicDuration = new ArrayList<Integer>();
		
        FileOutput resultsFile = new FileOutput(outputFile, true);
		
        EpidemicModelling epiMod = new EpidemicModelling();
        System.out.println("Thread: " + SID + ":");
        String indexCase = epiMod.chooseInitiallyInfectedFarms(startDate, infectionsData);
		
        for (int i = 0; i < repeats; i++) {
			
            if (bDrawIndexCases) {
                indexCase = epiMod.chooseInitiallyInfectedFarms(startDate, infectionsData);
            }
            System.out.println("indexCase: " + indexCase);
            epiMod.setSID((double) i / 100 + SID, farmsData, bInfoWindows);
            epiMod.startSimulations(startDate, farmsData, marketsData, movementsData, infectionsData, bNew, bLocal, 1 - ((double) readRate / 100), bInfoWindows);
			
            ArrayList<String> infRes, cullRes;
            cullRes = epiMod.getCulledPremises(); // CULLED
            infRes = epiMod.getInfectiousPremises(); //INFECTED
            numInf.add(epiMod.getNumOfInfectedBeforeNMB());
            epidemicDuration.add(epiMod.getEpidemicDuration());
			
            for (String cullCPH : cullRes) {
                if (culledPremises.containsKey(cullCPH)) {
                    int prev = culledPremises.get(cullCPH);
                    culledPremises.put(cullCPH, prev + 1);
                } else {
                    culledPremises.put(cullCPH, 1);
                }
            }
			
            for (String infCPH : infRes) {
                if (infPremises.containsKey(infCPH)) {
                    int prev = infPremises.get(infCPH);
                    infPremises.put(infCPH, prev + 1);
                } else {
                    infPremises.put(infCPH, 1);
                }
            }
			
            publish((i + 1) * 100 / repeats);
            setProgress((i + 1) * 100 / repeats);
			
            // input parameters 
           resultsFile.getWriter().format("%s,%s,%s,%d,%d,%f,%f,%f,%f,%d,%d,%d,%d,%d,%d,%d,%d,%d,%f,",
										   new SimpleDateFormat("dd-MM-yyyy").format(new Date()),
										   new SimpleDateFormat("HH:mm").format(new Date()),
										   startDate,
										   EpidemicModelling.NUMBER_OF_INDEX_CASES, EpidemicModelling.DURATION_OF_INITIAL_INFECTIONS,
										   EpidemicModelling.BETA, EpidemicModelling.DIRECT_MOVEMENT,
										   EpidemicModelling.OFF_MARKET_MOVEMENT, EpidemicModelling.LOCAL_SPREAD,
										   EpidemicModelling.LATENT_PERIOD, EpidemicModelling.MOVEMENT_RESTRICTIONS,
										   EpidemicModelling.CONTROL_MEASURES_DELAY, EpidemicModelling.MOVEMENT_BAN_DELAY, EpidemicModelling.CONTACT_TRACING_DEPTH,
										   EpidemicModelling.DELAY_LOCAL_CONTACTS, EpidemicModelling.DELAY_DIRECT_MOVEMENTS,
										   EpidemicModelling.DELAY_MARKET_MOVEMENTS, EpidemicModelling.DETECTION_TIME,
										   SpatialProbabilityMatrix.CLOSE_DISTANCE);
			
            //output results	        
            resultsFile.getWriter().format("%d,%d, %s, %d,%d,%d,%d,%d, %d,%d,%d ,%d,%d,%d,%d, %d, %d,%d,%d,%d, %d,%d,%d,%d,%d\n", SID, i,
										   indexCase,
										   epiMod.getEpidemicDuration(),
										   epiMod.getNumOfSusceptible(),
										   epiMod.getNumOfInfectedLocally(),
										   epiMod.getNumOfInfectedViaMovements(),
										   epiMod.getNumOfInfectedBeforeNMB(),
										   epiMod.getExposed(),
										   epiMod.getInfectious(),
										   epiMod.getNumOfCulled(),
										   epiMod.getNumOfConfirmedSusceptible(),
										   epiMod.getNumOfConfirmedRestricted(),
										   epiMod.getNumOfConfirmedExposed(),
										   epiMod.getNumOfConfirmedInfected(),
										   epiMod.getNumOfDetectedRandomly(),
										   epiMod.getExaminationsS(),
										   epiMod.getExaminationsH(),
										   epiMod.getExaminationsE(),
										   epiMod.getExaminationsI(),
										   epiMod.getIntermediateSusceptible(),
										   epiMod.getIntermediateInfectedLocally(),
										   epiMod.getIntermediateExposed(),
										   epiMod.getIntermediateInfectious(),
										   epiMod.getIntermediateCulled()
										   );
            
            
            synchronized (summaryFileOutput) {
	            if (bSummaryHeader) {
	    			summaryFileOutput.getWriter().format("Thread,");
	            	for (Iterator<String> iter = farmsData.getIterator(); iter.hasNext(); ) {
	        			String cph = (String) iter.next();
	        			summaryFileOutput.getWriter().format("%s,", cph);
	            	}
	    			summaryFileOutput.getWriter().format("Total\n");
	    			bSummaryHeader = false;
	            }
	            
	            
	            summaryFileOutput.getWriter().format("%d,", SID);
	            ArrayList<String> culledFarms = epiMod.getCulledPremises();
	            for (Iterator<String> iter = farmsData.getIterator(); iter.hasNext(); ) {
	    			String cph = (String) iter.next();
					//    			FarmsDataRecord record = getRecord(cph);
	            	if (culledFarms.contains(cph))
	            		summaryFileOutput.getWriter().format("1,");
	            	else
	            		summaryFileOutput.getWriter().format("0,");
	            }
	            summaryFileOutput.getWriter().format("%d\n", culledFarms.size());
	            summaryFileOutput.getWriter().flush();			
			}
            
            
			
			
			//	        epiMod.cleanup();
			//	        resultsFile.getWriter().flush();	        
        }
        resultsFile.close();
		
		//		System.out.println("RESULTS:");
		//		
		//		
		////		FileOutput timesFile = new FileOutput("output/times" + SESSION_NAME + ".csv");
		//		Set cphs = infPremises.keySet();
		//		int maxInf = 0;
		//		for (Iterator i = cphs.iterator(); i.hasNext(); ) {
		//			String key = (String) i.next();
		//			System.out.println("\t" + key + ": " + infPremises.get(key));
		////			timesFile.getWriter().format("%s,%d\n", key, infPremises.get(key));
		//			if (maxInf < infPremises.get(key))
		//				maxInf = infPremises.get(key);
		//		}
		////		timesFile.close();
        System.out.println(infPremises);
		
		//		GraphViz.drawHeatMap(premisesData, infPremises, /*TIMES*/ maxInf, "output/infected" + SESSION_NAME + ".gv");
		//		GraphViz.drawHeatMap(premisesData, culledPremises, /*TIMES*/ maxInf, "output/culled" + SESSION_NAME + ".gv");
		//		GraphViz.generateCSVforGephi(premisesData, movementsData, infPremises, /*TIMES*/ maxInf, "output/" + SESSION_NAME + ".csv");
		//	Sibylle: GraphViz.generateCSVforGephi(this.farmsData, movementsData, infPremises, /*TIMES*/maxInf, "output/" + SESSION_NAME + ".csv");
        double meanNumInf = 0, meanDuration = 0;
        int size = numInf.size();
        for (int i = 0; i < size; i++) {
            meanNumInf += numInf.get(i);
            meanDuration += epidemicDuration.get(i);
        }
        meanNumInf /= size;
        meanDuration /= size;
		
        System.out.println("Mean duration: " + meanDuration);
		
        return (int) meanDuration;
    }
	
	//	@Override
	//    protected void done() {
	//        try { 
	//        	FileOutput resultsFile = new FileOutput("output/results.csv");
	//    		for (int i = 0; i < repeats; i++) {
	//
	//    	        resultsFile.getWriter().format("%f,%d\n", (double) i / 100 + SID, 
	//    	        		epidemicDuration.get(i));
	//    		}
	//    		resultsFile.close();
	//        } catch (Exception ignore) {
	//        }
	//    }
	//	@Override
	//	protected void process(List<Integer> arg0) {
	//		FileOutput resultsFile = new FileOutput("output/results.csv");
	//		for (int i = 0; i < repeats; i++) {
	//
	//	        resultsFile.getWriter().format("%f,%d\n", (double) i / 100 + SID, 
	//	        		epidemicDuration.get(i));
	//		}
	//		resultsFile.close();
	//	}
}
