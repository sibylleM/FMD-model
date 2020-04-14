package methods;

import gui.EpidemicCurvesPanel;

import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import utils.Utils;
import utils.FileOutput;
import utils.GraphViz;
import data.InfectionsData;
import data.MarketsData;
import data.MovementsData;
import data.MovementsData.MovementsDataRecord;
import data.FarmsData;
import java.util.Random;

public class EpidemicModelling {
	
    ArrayList<String> susceptiblePremises;
    HashMap<String, Integer> restrictedPremises;
    HashMap<String, Integer> exposedPremises;
    HashMap<String, Integer> infectiousPremises; // cph, date of infection (step)	
    ArrayList<String> culledPremises;
	
    ArrayList<String> confirmedIPs;
    HashMap<String, Integer> investigationQueue;
	
    ArrayList<String> predefinedInfectiousFarms = new ArrayList<String>();
	
    ////////////// DISTRIBUTIONS //////////////
    HashMap<String, Integer> latentPeriods;
	//	HashMap<String, Integer> datesOfInfection; // already stored in infectious premises, BUT NOT FOR RESTRICTED
    HashMap<String, Integer> clinicalSigns; // days of clinical signs before detection ex DETECTION_TIME
	
    ///////////////////////////////////////////
    private int epidemicDuration;
    private int numOfInfectedBeforeNMB;
    private int numOfLocallyInfected;
    private int numOfInfectedByMov;
	
    int initiallyInfected;
	
    private int numOfConfirmedInfected;
    private int numOfConfirmedSusceptible;
    private int numOfConfirmedExposed;
    private int numOfConfirmedRestricted;
	
    private int numOfDetectedRandomly;
	//	private int numOfDetectedMovements; // farms that were added to investigation queue because of dangerous movement contacts
    // one farm can be detected several times
	//	private int numOfDetectedLocal;
	
    private int intermediateSusceptible,
	intermediateInfectedLocally,
	intermediateExposed,
	intermediateInfectious,
	intermediateCulled;
	
    private int allExaminedS, allExaminedH, allExaminedE, allExaminedI;
	
    double SID; // session ID (from the EpiModThread)
	
    public static int NUMBER_OF_INDEX_CASES;
    public static int DURATION_OF_INITIAL_INFECTIONS; // DURATION OF IN MOVEMENTS FROM ENGLAND THAT CAN CAUSE INFECTIONS
	
    public static double BETA;
    public static double DIRECT_MOVEMENT;
    public static double OFF_MARKET_MOVEMENT;
    public static double LOCAL_SPREAD;
	
    public static int MOVEMENT_RESTRICTIONS;
	
    /////////
    public static int CONTROL_MEASURES_DELAY;
    public static int MOVEMENT_BAN_DELAY;
	
    public static int CONTACT_TRACING_DEPTH; // when we confirm IP, we look at all the movements up to X days back
    public static int DELAY_LOCAL_CONTACTS;
    public static int DELAY_DIRECT_MOVEMENTS;
    public static int DELAY_MARKET_MOVEMENTS;
	
    // NOT USED!
    public static int LATENT_PERIOD;
    public static int DETECTION_TIME;
    /////////
	
    static boolean NEW;
	
    public static boolean bGraphviz;
	
    public static boolean bLogIndividual = true;
    public static boolean bLogDaily = false;
    public static boolean bLogMovs = true;
    public static boolean bOutputDaily = true;
    public static boolean bOutputCulledDaily =true;
	
    JFrame frame;
    JLabel label;
    JTextArea additionalInfo;
    EpidemicCurvesPanel epidemicCurves;
    EpidemicCurvesPanel R0curve;
	
    Random rand;
	
    public EpidemicModelling() {
    }
	
    public void setSID(double d, FarmsData premisesData, boolean bInfoWindow) {
        this.SID = d;
        setup(premisesData, bInfoWindow);
		
    }
	
    private void setup(FarmsData premisesData, boolean bInfoWindow) {
        // We mark all the premises as susceptible
        susceptiblePremises = new ArrayList<String>();
        for (Iterator<String> iterator = premisesData.getIterator(); iterator.hasNext();) {
            String cph = (String) iterator.next();
			
			//			if (premisesData.getRecord(cph).Location_Type.equals("F")) // ADD ONLY FARMS!!! MARKETS AND OTHER PREMISES CAN'T BE INFECTED!!!
			//			if (premisesData.getRecord(cph).Location_Type.equals("M")) // ADD ONLY FARMS AND X!!! MARKETS CAN'T BE INFECTED!!!
			//				continue;
            // NO EMPTY FARMS
			//			if (premisesData.getRecord(cph).Census_Sheep == 0)
			//				continue;
            susceptiblePremises.add(cph);
        }
		
        restrictedPremises = new HashMap<String, Integer>();
        exposedPremises = new HashMap<String, Integer>();
        infectiousPremises = new HashMap<String, Integer>();
		
        culledPremises = new ArrayList<String>();
        confirmedIPs = new ArrayList<String>();
        investigationQueue = new HashMap<String, Integer>();
		
        latentPeriods = new HashMap<String, Integer>();
        clinicalSigns = new HashMap<String, Integer>();
		
        if (bInfoWindow) {
            frame = new JFrame("Running simulations... SID: " + SID);
			//			frame.setLayout(new GridLayout(0, 1, 0, 0));
            frame.setLayout(new FlowLayout());
			
            label = new JLabel();
            label.setText("<html>Day: " + 0 + "\t<FONT COLOR=GREEN>Susceptible</FONT>: " + susceptiblePremises.size()
						  + "\t<s>Exposed</s>: " + exposedPremises.size()
						  + "\t<s>Restricted</s>: " + restrictedPremises.size()
						  + "\t<FONT COLOR=RED>Infected</FONT>: " + infectiousPremises.size()
						  + "\t<FONT COLOR=BLACK>Removed</FONT>: " + culledPremises.size() + "</html>");
            epidemicCurves = new EpidemicCurvesPanel(500, 200, 50, 11, 8, 3);
            R0curve = new EpidemicCurvesPanel(500, 200, 50, 11, 8, 1);
			
            additionalInfo = new JTextArea();
            additionalInfo.setEditable(false);
            additionalInfo.setCursor(null);
            additionalInfo.setOpaque(false);
            additionalInfo.setFocusable(false);
			
            frame.add(label);
			
            frame.add(new JLabel("Epidemic curves: S, I, R"));
            frame.add(epidemicCurves);
			
            frame.add(new JLabel("R_0 * 100"));
            frame.add(R0curve);
			
            frame.add(additionalInfo);
            frame.setSize(600, 800);
            frame.setLocationByPlatform(true);
            frame.setVisible(true);
            frame.setResizable(false);
			
            epidemicCurves.addVerticalLine(CONTROL_MEASURES_DELAY);
			
            R0curve.addHorisontalLine(100);
            R0curve.addVerticalLine(CONTROL_MEASURES_DELAY);
        }
		
        epidemicDuration = 0;
        numOfInfectedBeforeNMB = 0;
        numOfLocallyInfected = 0;
        numOfInfectedByMov = 0;
        initiallyInfected = 0;
        numOfConfirmedInfected = 0;
        numOfConfirmedSusceptible = 0;
        numOfConfirmedExposed = 0;
        numOfConfirmedRestricted = 0;
        numOfDetectedRandomly = 0;
        allExaminedS = 0;
        allExaminedH = 0;
        allExaminedE = 0;
        allExaminedI = 0;
    }
	
    public String chooseInitiallyInfectedFarms(String startDateString, InfectionsData infectionsData) {// Determine initially infected farms...	
        Calendar nowCal = Utils.fromStringToCalendar(startDateString);
		
        for (int i = 0; i < DURATION_OF_INITIAL_INFECTIONS; i++) {
            ArrayList<String> predefinedToday = new ArrayList<String>();/////////////////////////
			
            //predefinedInfections
            nowCal.add(Calendar.DATE, i);
            predefinedToday = infectionsData.getInfectedOnDatePremises(nowCal);
            for (String cph : predefinedToday) {
                if (!predefinedInfectiousFarms.contains(cph)) {
                    predefinedInfectiousFarms.add(cph);
                    ////////////////////////////////////// ONLY ONE INDEX CASE!!!!!
					//					return predefinedInfectiousFarms.get(0);
                    ///////////////////////////////////// only one!
                }
            }
            nowCal.add(Calendar.DATE, -i);
        }
		
        while (predefinedInfectiousFarms.size() > NUMBER_OF_INDEX_CASES) {
			//			int index = (int) (Math.random() * predefinedInfectiousFarms.size()); // RANDOM
			
            int index = predefinedInfectiousFarms.size() - 1;
            predefinedInfectiousFarms.remove(index);
        }
		//		if (bLogDaily)
        System.out.println("Initially infected farms: " + predefinedInfectiousFarms.toString());
        int min = 0;
        int max = predefinedInfectiousFarms.size();
        int randomNum = min + (int) (Math.random() * ((max - min) + 1));
        System.out.println("Random index chosen is: " + randomNum);
        if(randomNum>0)
            randomNum=randomNum-1;
        return predefinedInfectiousFarms.get(randomNum);
    }
	
    public void startSimulations(String startDateString,
								 FarmsData farmsData, MarketsData marketsData, MovementsData movementsData, InfectionsData infectionsData,
								 //			SpatialProbabilityMatrix spatialProbabilityMatrix, 
								 boolean bNew, boolean bLocal, double errorRate, boolean bInfoWindows) {
		//		setup(farmsData, bInfoWindows);
        NEW = bNew;
		
        String outputPath = "output/" + DELAY_MARKET_MOVEMENTS + "-" + SpatialProbabilityMatrix.CLOSE_DISTANCE + "/" + startDateString.replaceAll("/", "-");
		
        int step = 0;
        initiallyInfected = 0;
		
        final int POPULATION_SIZE = susceptiblePremises.size();
		
        double crudeR0 = 0.0; // 		
		//		int infToCull = 0;
		
        FileOutput rFile = null;
        FileOutput culledDailyFile = null;
        
        
        if (bOutputCulledDaily) {
        	culledDailyFile = new FileOutput(outputPath + "/culledDaily" + SID + ".log");
            //header 
        	culledDailyFile.getWriter().format("Date\tCPH\n");
            
        }
		
        if (bOutputDaily) {
            rFile = new FileOutput(outputPath + "/daily" + SID + ".log");
            //header 
            rFile.getWriter().format("Step\t"
									 + "S\t"
									 + "H\t"
									 + "E\t"
									 + "I\t"
									 + "C\t"
									 + "R\t"
									 + "New local infections\t"
									 + "New movement infections\t"
									 + "New infectious\t"
									 + "numOfConfirmedSusceptible\t"
									 + "numOfConfirmedRestricted\t"
									 + "numOfConfirmedExposed\t"
									 + "numOfConfirmedInfected\t"
									 + "examinedS\t"
									 + "examinedH\t"
									 + "examinedE\t"
									 + "examinedI\t"
									 + "localForceOfInfection\t"
									 + "forceOfInfection\t"
									 + "R0 (forceOfInfection / infectiousSize)\t"
									 + "crudeR0\t"
									 + "numOfLocallyInfected\t"
									 + "numOfLocallyInfected\t"
									 + "newCulled\t"
									 + "infToCull (sum of examinedI)\t"
									 + "numOfDetectedRandomly\t"
									 + //								"numOfDetectedMovements" +
									 "\n");
        }
		
        Calendar nowCal = Utils.fromStringToCalendar(startDateString);
		
		//		chooseInitiallyInfectedFarms(startDateString, infectionsData);
        //check initial infections (delete unknown farms)
		//		ArrayList<InfectionsDataRecord> cphstodel = new ArrayList<InfectionsDataRecord>();
		//		for (InfectionsDataRecord rec : infectionsData.getData()) {
		//			if (!susceptiblePremises.contains(rec.CPH)) {
		//				System.out.println(rec.CPH);
		//				cphstodel.add(rec);
		//			}
		//		}
		//		for (InfectionsDataRecord rec : cphstodel) {
		//			infectionsData.getData().remove(rec);
		//			System.out.println(rec + "removed");
		//		}
		//		infectionsData.writeToFile("output/new.csv");
        // MAIN CYCLE
        String infoText = "";
		
        for (;; nowCal.add(Calendar.DATE, 1)) {
            ArrayList<MovementsDataRecord> dailyMovements = new ArrayList<MovementsData.MovementsDataRecord>();
            if (step < MOVEMENT_BAN_DELAY) // MOVEMENT BAN AFTER CERTAIN DAYS
            {
                dailyMovements = movementsData.getMovingOnDatePremises(nowCal);
            } else if (step == MOVEMENT_BAN_DELAY) {
                System.out.println("NMB applied: no more movements allowed!");
            }
			
            System.out.println("*** " + SID + ": " + nowCal.getTime());
			
			////////////	INITIAL INFECTIONS
            if (step < DURATION_OF_INITIAL_INFECTIONS) {
                ArrayList<String> predefinedToday = infectionsData.getInfectedOnDatePremises(nowCal);
                for (String infCPH : predefinedToday) {
                    if (predefinedInfectiousFarms.contains(infCPH)) {
						//						if (restrictedPremises.containsKey(infCPH)) // if already infected 
						//							continue;					
                        if (infectiousPremises.containsKey(infCPH)) // if already infected 
                        {
                            continue;
                        }
						
                        if (susceptiblePremises.contains(infCPH)) {
                            susceptiblePremises.remove(infCPH);
                        } else {
                            System.err.print("ERROR: unknown farm with CPH " + infCPH + "!");
                            JOptionPane.showMessageDialog(null, "Problem with SID " + SID + ": ERROR: unknown farm with CPH " + infCPH + "! Was supposed to be index case.");
							//							System.exit(1);
                            continue;
                        }
						//						restrictedPremises.put(infCPH, MOVEMENT_RESTRICTIONS);
                        infectiousPremises.put(infCPH, 0);
                        initiallyInfected++;
						//						if (bLogIndividual) System.out.println("\t" + infCPH + " was infected (in movement from England).");
                        if (bLogIndividual) {
                            System.out.println("\t" + infCPH + " was index case.");
                        }
                        infoText += "\t" + infCPH + " was index case.\n";
                    }
                }
            }
			
            /////////////////////////////////////////////////////////////
            // DRAWING DAILY MAP!!!
            if (bGraphviz) {
                GraphViz.drawMap(farmsData, marketsData, susceptiblePremises, restrictedPremises, exposedPremises, infectiousPremises, culledPremises, dailyMovements,
								 infoText,
								 "output/FMDmap" + step + ".gv");
            }
            /////////////////////////////////////////////////////////////
            infoText = "";
			
            /////////////////////////////////////////////////////////////
            /////////////////////////////////////////////////////////////
            // UPDATE FRAME
            if (bInfoWindows) {
                label.setText("<html>Day: " + step + "\t<FONT COLOR=GREEN>Susceptible</FONT>: " + susceptiblePremises.size()
							  + "\t<s>Exposed</s>: " + exposedPremises.size()
							  + "\t<s>Restricted</s>: " + restrictedPremises.size()
							  + "\t<FONT COLOR=RED>Infected</FONT>: " + infectiousPremises.size()
							  + "\t<FONT COLOR=BLACK>Removed</FONT>: " + culledPremises.size() + "</html>");
                label.repaint();
            }
			
            if (susceptiblePremises.size() + exposedPremises.size() + restrictedPremises.size()
				+ infectiousPremises.size() + culledPremises.size() + confirmedIPs.size() != POPULATION_SIZE) {
                System.err.println("Size of the overall population has changed! Should be " + POPULATION_SIZE + ", not "
								   + (susceptiblePremises.size() + exposedPremises.size() + restrictedPremises.size()
									  + infectiousPremises.size() + culledPremises.size() + confirmedIPs.size()) + ": "
								   + susceptiblePremises.size() + " (S) + "
								   + exposedPremises.size() + " (E) + "
								   + restrictedPremises.size() + " (H) + "
								   + infectiousPremises.size() + " (I) + "
								   + confirmedIPs.size() + " (confirmed) + "
								   + culledPremises.size() + " (R)!");
                JOptionPane.showMessageDialog(null, "Problem with SID " + SID + ": total population size have changed!");
                System.exit(0);
            }
			
            if (bInfoWindows) {
                epidemicCurves.updateX(0, infectiousPremises.size());
                epidemicCurves.updateX(1, susceptiblePremises.size());
                epidemicCurves.updateX(2, culledPremises.size());
                epidemicCurves.repaint();
				
                additionalInfo.setText("Infected locally: " + numOfLocallyInfected
									   + "\nInitially infected: " + initiallyInfected
									   + "\nInfected by movements: " + numOfInfectedByMov
									   + "\nInfected before NMB: " + numOfInfectedBeforeNMB
									   + "\nCulled: " + culledPremises.size()
									   + "\nConfirmed (susceptible): " + numOfConfirmedSusceptible
									   + "\nConfirmed (restricted): " + numOfConfirmedRestricted
									   + "\nConfirmed (exposed): " + numOfConfirmedExposed
									   + "\nConfirmed (infectious): " + numOfConfirmedInfected
									   + "\nDetected (randomly): " + numOfDetectedRandomly
									   + //						"\nDetected (movements): " + numOfDetectedMovements + 
									   //						"\nDetected (local): " + numOfDetectedLocal + 
									   //						"\nExaminations: " + numOfExaminations + 
									   "\nExamined (susceptible):" + allExaminedS
									   + "\nExamined (restriced):" + allExaminedH
									   + "\nExamined (exposed):" + allExaminedE
									   + "\nExamined (infectious):" + allExaminedI
									   + "\n");
                additionalInfo.repaint();
				
                R0curve.updateX(0, (int) (crudeR0 * 100));
                R0curve.repaint();
            }
			
            /////////////////////////////////////////////////////////////
            // STOP WHEN ALL INFECTED FARMS ARE CULLED (test after initial infections)
			// if (step >= DURATION_OF_INITIAL_INFECTIONS && infectiousPremises.size() == 0 && exposedPremises.size() == 0 && restrictedPremises.size() == 0
            //        && confirmedIPs.size() == 0 && investigationQueue.size() == 0)
            
            //Sibylle: took out exposed.Premises.size() ==0 as this results in an endless loop; does not go to zero as this case is not removed at all;
            if (step >= DURATION_OF_INITIAL_INFECTIONS && infectiousPremises.size() == 0 && restrictedPremises.size() == 0
				&& confirmedIPs.size() == 0 && investigationQueue.size() == 0){
                System.out.println("SIMULATIONS ARE FINISHED! ALL INFECTED FARMS ARE CULLED!");
                epidemicDuration = step;
				
                if (bOutputDaily) {
                    // final map Sibylle: took that out
					//                    GraphViz.drawMap(farmsData, marketsData, susceptiblePremises, restrictedPremises, exposedPremises, infectiousPremises, culledPremises, dailyMovements,
					//                            "Simulations are finished!",
					//                            outputPath + "/map" + SID + ".gv");
					
                    // culled farms
                    saveCulledFarms(outputPath + "/culled" + SID + ".csv");
                }
				
                break;
            } else if (step == MOVEMENT_BAN_DELAY + CONTACT_TRACING_DEPTH + 14) {
                intermediateSusceptible = susceptiblePremises.size();
                intermediateInfectedLocally = numOfLocallyInfected;
                intermediateExposed = exposedPremises.size();
                intermediateInfectious = infectiousPremises.size();
                intermediateCulled = culledPremises.size();
            }
			
            // TRANSITIONS FOR PENDING INFECTED FARMS (restrictions, latent periods)
            int prevInf = infectiousPremises.size();
			//Sibylle: changed Iterator variable for comparetments H -> I and E -> I as they were identical for both loops;
            // H -> I			
            Iterator<Map.Entry<String, Integer>> RPintIterator = restrictedPremises.entrySet().iterator();
            while (RPintIterator.hasNext()) {
                Map.Entry<String, Integer> entry = RPintIterator.next();
                int prev = entry.getValue();
                entry.setValue(prev - 1);
                if (entry.getValue() == 0) {
                    RPintIterator.remove();
                    infectiousPremises.put(entry.getKey(), step);
                    if (bLogIndividual) {
                        System.out.println("\t" + entry.getKey() + " became infectious (I) after being restricted (H).");
                    }
                    infoText += "\t" + entry.getKey() + " became infectious (I) after being restricted (H).\n";
                }
            }
			
            // E -> I
            Iterator<Map.Entry<String, Integer>> EPintIterator = exposedPremises.entrySet().iterator();
            while (EPintIterator.hasNext()) {
                Map.Entry<String, Integer> entry = EPintIterator.next();
                int prev = entry.getValue();
                entry.setValue(prev - 1);
                if (entry.getValue() == 0) {
                    EPintIterator.remove();
                    infectiousPremises.put(entry.getKey(), step);
                    if (bLogIndividual) {
                        System.out.println("\t" + entry.getKey() + " became infectious (I) after being exposed (E).");
                    }
                    infoText += "\t" + entry.getKey() + " became infectious (I) after being exposed (E).\n";
                }
            }
			
            int newInf = infectiousPremises.size() - prevInf;
			
            /////////////////////////////////////////////////////////////
            // INFECTION STAGE
            HashMap<String, Double> pendingRestrictedFarms = new HashMap<String, Double>();
            HashMap<String, Double> pendingExposedFarms = new HashMap<String, Double>();
			
            HashMap<String, Double> infectedMarkets = new HashMap<String, Double>(); // list of markets where infected sheep were moved to (with summed probability of infection)
            HashMap<String, ArrayList<String>> mktToFarms = new HashMap<String, ArrayList<String>>(); // list of all the rcp farms for every market
            HashMap<String, Integer> animalsFromInfected = new HashMap<String, Integer>(); // number of animals from infected farms
            HashMap<String, Integer> animalsFromSusceptible = new HashMap<String, Integer>(); // number of animals from susceptible farms
			
            // VIA MARKETS
            for (MovementsDataRecord mov : dailyMovements) {
                if (mov.mktCPH.isEmpty()) // skip movements not through markets
                {
                    continue;
                }
				
                if (!infectedMarkets.containsKey(mov.mktCPH)) // skip already added markets
                {
                    if (infectiousPremises.containsKey(mov.srcCPH) || exposedPremises.containsKey(mov.srcCPH)) // add only if there is a movement from an infected or exposed farm
                    {
                        infectedMarkets.put(mov.mktCPH, 0.0);
                    }
                }
				
                if (mktToFarms.containsKey(mov.mktCPH)) {
                    mktToFarms.get(mov.mktCPH).add(mov.rcpCPH);
                } else {
                    ArrayList<String> tmp = new ArrayList<String>();
                    tmp.add(mov.rcpCPH);
                    mktToFarms.put(mov.mktCPH, tmp);
                }
				
                if (infectiousPremises.containsKey(mov.srcCPH) || exposedPremises.containsKey(mov.srcCPH)) {
                    int prev = (animalsFromInfected.get(mov.rcpCPH) == null) ? 0 : animalsFromInfected.get(mov.rcpCPH);
                    animalsFromInfected.put(mov.rcpCPH, prev + mov.size);
                } else if (susceptiblePremises.contains(mov.srcCPH)) { // || restrictedPremises.containsKey(mov.srcCPH)) {
                    int prev = (animalsFromSusceptible.get(mov.rcpCPH) == null) ? 0 : animalsFromSusceptible.get(mov.rcpCPH);
                    animalsFromSusceptible.put(mov.rcpCPH, prev + mov.size);
                } else if (restrictedPremises.containsKey(mov.srcCPH)) {
                    int prev = (animalsFromSusceptible.get(mov.rcpCPH) == null) ? 0 : animalsFromSusceptible.get(mov.rcpCPH);
                    animalsFromSusceptible.put(mov.rcpCPH, prev + mov.size);
                    // breach of standstill policy
					//					JOptionPane.showMessageDialog(null, "Breach of standstill policy by farm " + mov.srcCPH + "!");
                }
            }
			
            // calculate old probabilities of infection
            Iterator<Map.Entry<String, Double>> mktIterator = infectedMarkets.entrySet().iterator();
            while (mktIterator.hasNext()) {
                Map.Entry<String, Double> entry = mktIterator.next();
				
                double overallMeanInfected = 0.0;
				
                for (String farm : mktToFarms.get(entry.getKey())) {
                    int numInf = (animalsFromInfected.get(farm) == null) ? 0 : animalsFromInfected.get(farm);
                    int numSusc = (animalsFromSusceptible.get(farm) == null) ? 0 : animalsFromSusceptible.get(farm);
                    double prob = 1 - Math.pow(1 - OFF_MARKET_MOVEMENT, numInf + numSusc);
                    overallMeanInfected += prob;
					//			    	System.out.println("Farm " + farm + " can become infected with prob " + prob + ").");
                    if (!NEW) {
                        putToPending(pendingRestrictedFarms, farm, prob);
                    }
                }
				
                entry.setValue(overallMeanInfected);
            }
			
            // calculate NEW probabilities of infection
            if (NEW) {
                mktIterator = infectedMarkets.entrySet().iterator();
                while (mktIterator.hasNext()) {
					
                    final double EPS = 0.000000001;
                    double error = 100.0;
                    double NEW_OFF = OFF_MARKET_MOVEMENT;
                    double stepVal = OFF_MARKET_MOVEMENT / 2;
					
                    Map.Entry<String, Double> entry = mktIterator.next();
					
                    while (Math.abs(error) > EPS) {
                        double overallMeanInfected = 0.0;
						
                        for (String farm : mktToFarms.get(entry.getKey())) {
                            int numInf = (animalsFromInfected.get(farm) == null) ? 0 : animalsFromInfected.get(farm);
                            int numSusc = (animalsFromSusceptible.get(farm) == null) ? 0 : animalsFromSusceptible.get(farm);
                            double prob = 1 - Math.pow(1 - NEW_OFF, numSusc + (int) (numInf * errorRate)) * Math.pow(1 - DIRECT_MOVEMENT, numInf - (int) (numInf * errorRate));
                            overallMeanInfected += prob;
                        }
						
                        if (Math.abs(error) < Math.abs(entry.getValue() - overallMeanInfected)) {
                            //				    	System.err.println("ERROR: " + error + ", NEW_OFF: " + NEW_OFF + ", step: " + stepVal);
                            //				    	System.exit(0);
                        } else if (Math.abs(error - entry.getValue() + overallMeanInfected) < EPS) {
                            System.err.println("LOOOOOOOP!!!");
                        }
						
                        error = entry.getValue() - overallMeanInfected;
						
                        //				    System.out.println("Error: " + error);
                        if (error > 0) {
                            NEW_OFF += stepVal;
                        } else if (error < 0) {
                            NEW_OFF -= stepVal;
                        } else {
                            break;
                        }
						
                        stepVal /= 2;
						
                        if (NEW_OFF < EPS) {
                            NEW_OFF = 0.0;
                            break;
                        }
                    }
					
                    if (bLogMovs) {
                        System.out.println("New value for OFF_MOVEMENT for " + entry.getKey() + " is " + NEW_OFF);
                    }
                    error += 0.0;
					
                    if (NEW_OFF < 0) {
                        System.out.println("O_o NEW_OFF < 0! " + NEW_OFF);
                        JOptionPane.showMessageDialog(null, "Problem with SID " + SID + ": O_o NEW_OFF < 0! " + NEW_OFF);
						//						System.exit(1);
                        continue;
                    }
					
                    // APPLY NEW PROBABILITY
                    for (String farm : mktToFarms.get(entry.getKey())) {
                        int numInf = (animalsFromInfected.get(farm) == null) ? 0 : animalsFromInfected.get(farm);
                        int numSusc = (animalsFromSusceptible.get(farm) == null) ? 0 : animalsFromSusceptible.get(farm);
						
                        //			    	NEW_OFF = 0.0; // NO EFFECT OF "UNKNOWN" MOVEMENTS!
                        double prob = 1 - Math.pow(1 - NEW_OFF, numSusc + (int) (numInf * errorRate)) * Math.pow(1 - DIRECT_MOVEMENT, numInf - (int) (numInf * errorRate));
                        double prevProb = 1 - Math.pow(1 - OFF_MARKET_MOVEMENT, numInf + numSusc);
						
                        putToPending(pendingRestrictedFarms, farm, prob);
                        if (bLogIndividual) {
                            System.out.println("Farm " + farm + " will be infected with prob " + prob + " (was " + prevProb + ").");
                        }
                        if (Math.abs(prob - prevProb) > 0.05) {
                            if (bLogIndividual) {
                                System.out.println("Difference is " + (prob - prevProb) + ": " + numInf + "(inf), " + numSusc + "(susc), " + (int) (numInf * errorRate) + "(unknown).");
                            } else
								;//System.out.println("Difference is " + (prob - prevProb) + ": " + numInf + "(inf), " + numSusc + "(susc), " + (int) (numInf * errorRate) + "(unknown).");
                        }
                    }
                }
            }
			
            ////////////////////
            // DIRECT MOVEMENTS (not via markets)	
            for (MovementsDataRecord mov : dailyMovements) {
                if (!mov.mktCPH.isEmpty()) // movements through markets are not considered
                {
                    continue;
                }
				
                // infections via direct movements from INFECTIOUS farms
                if (infectiousPremises.containsKey(mov.srcCPH)) // iterate ONLY infected farms
                {
                    putToPending(pendingRestrictedFarms, mov.rcpCPH, 1 - Math.pow(1 - DIRECT_MOVEMENT, mov.size));
                }
				
                // infections via DIRECT MOVEMENTS from EXPOSED farms
                if (exposedPremises.containsKey(mov.srcCPH)) {
                    putToPending(pendingRestrictedFarms, mov.rcpCPH, 1 - Math.pow(1 - DIRECT_MOVEMENT, mov.size)); // it doesn't really matter for the state of animals as they are isolated for 13 days
                }
            }
			
            // LOCAL SPREAD
            // Distance Kernel model
			//			if (bLocal) {
			//				Iterator<Map.Entry<String,Integer>> infectiousIterator = infectiousPremises.entrySet().iterator();
			//				while (infectiousIterator.hasNext()) {			    
			//				    String srcCPH = infectiousIterator.next().getKey();
			//					    
			//					if (culledPremises.contains(srcCPH)) { // if farm is already culled, it can't infect
			//						System.err.println(srcCPH + " was about to infect?!");
			//						JOptionPane.showMessageDialog(null, "Problem with SID " + SID + ": " + srcCPH + " was about to infect?!");
			//						System.exit(0);
			//						continue;
			//					}
			//					
			//					// infect susceptible farms
			//					for (String rcpCPH : susceptiblePremises) {
			//						double prob = spatialProbabilityMatrix.getProbability(srcCPH, rcpCPH) * LOCAL_SPREAD;
			//						putToPending(pendingExposedFarms, rcpCPH, prob);
			//					}				
			//					
			//					// restricted farms can also be infected locally!
			//					intIterator = restrictedPremises.entrySet().iterator();
			//					while (intIterator.hasNext()) {
			//					    Map.Entry<String,Integer> entry = intIterator.next();
			//					    double prob = spatialProbabilityMatrix.getProbability(srcCPH, entry.getKey()) * LOCAL_SPREAD;
			//						putToPending(pendingExposedFarms, entry.getKey(), prob);
			//					}
			//				}
			//			}
            // Green et al. 2006 model
            if (bLocal) {
                Iterator<Map.Entry<String, Integer>> infectiousIterator = infectiousPremises.entrySet().iterator();
                while (infectiousIterator.hasNext()) {
                    String srcCPH = infectiousIterator.next().getKey();
					
                    int numOfNewInfections = Utils.drawFromPoisson(BETA);
					//				    System.out.println(numOfNewInfections);
					
                    if (numOfNewInfections < 1) {
                        continue;
                    }
					
                    ArrayList<String> potentialNewInf;
					//				    potentialNewInf = spatialProbabilityMatrix.getCloseFarms(srcCPH);
                    potentialNewInf = SpatialProbabilityMatrix.getFarmsInSurveillanceZone(srcCPH);
					
                    ArrayList<Double> weights = new ArrayList<Double>();
                    ArrayList<String> toDelete = new ArrayList<String>();
					
                    double overallWeight = 0;
                    for (String rcpCPH : potentialNewInf) {
						
                        if (susceptiblePremises.contains(rcpCPH) || restrictedPremises.containsValue(rcpCPH)) {
                            double distance = SpatialProbabilityMatrix.getDistance(srcCPH, rcpCPH);
                            if (distance == 0) {
								//								System.err.println("No distance for " + srcCPH + " and " + rcpCPH);
								//								System.exit(0);
                            }
                            double weight;
							
                            weight = 1 / Math.pow(Math.E, (distance / 1000) * 0.5);
							//							weight = SpatialProbabilityMatrix.getProbability(srcCPH, rcpCPH);
							
                            weights.add(weight);
                            overallWeight += weight;
                        } else {
                            toDelete.add(rcpCPH);
                        }
                    }
					
                    for (String cph : toDelete) {
                        potentialNewInf.remove(cph);
                    }
					
                    int newInfections = 0;
                    while (newInfections < numOfNewInfections && !potentialNewInf.isEmpty()) {
                        double randomWeight = Math.random() * overallWeight;
                        double sumWeight = 0;
                        int i = 0;
                        while (sumWeight < randomWeight) {
                            sumWeight += weights.get(i);
                            i++;
                        }
                        overallWeight -= weights.get(i - 1);
                        weights.remove(i - 1);
                        putToPending(pendingExposedFarms, potentialNewInf.get(i - 1), 1.0);	// probability of 1.0
                        if (bLogIndividual) {
                            System.out.println("Farm " + potentialNewInf.get(i - 1) + " was infected locally by " + srcCPH);
                        }
                        infoText += "Farm " + potentialNewInf.get(i - 1) + " was infected locally by " + srcCPH + "\n";
                        potentialNewInf.remove(i - 1);
                        newInfections++;
                    }
                }
            }
			
            int infectiousSize = infectiousPremises.size();
			
            crudeR0 = 1.0 / infectiousSize;
			
            double forceOfInfection = 0.0, localForceOfInfection = 0.0;
			
            // MOVE FARMS TO NEW COMPARTMENTS (to H and E)
            if (bLogDaily) {
                System.out.println("Farms at risk: " + pendingExposedFarms.size() + pendingRestrictedFarms.size()); // some farms can be calculated twice!!!
            }
            int infLocal = 0;
            Iterator<Map.Entry<String, Double>> pendingIterator = pendingExposedFarms.entrySet().iterator();
            while (pendingIterator.hasNext()) {
                Map.Entry<String, Double> entry = pendingIterator.next();
                String CPH = entry.getKey();
				
                forceOfInfection += entry.getValue();
				
                if (bLogIndividual) {
                    System.out.println("\t" + CPH + " can become infected locally with p = " + entry.getValue());
                }
				
                double pick = Math.random();
                if (pick < entry.getValue()) {
                    if (susceptiblePremises.contains(CPH)) {
                        susceptiblePremises.remove(CPH);
                        int latentPeriod = drawLatentPeriod();
                        latentPeriods.put(CPH, latentPeriod);
                        exposedPremises.put(CPH, latentPeriod);
						
                        if (bLogIndividual) {
                            System.out.println("\t" + CPH + " infected locally!");
                        }
                        infoText += "\t" + CPH + " infected locally!\n";
                        infLocal++;
                    }
					//					else if (restrictedPremises.containsKey(CPH)) {
					//						if (restrictedPremises.get(CPH) > LATENT_PERIOD) { // if not, farm will become infectious before new moved on sheep will become infectious
					//							restrictedPremises.remove(CPH); // transfer farm from H to E (if it will incubate virus before movement restrictions elapsed)
					//							exposedPremises.put(CPH, LATENT_PERIOD);
					//							
					//							if (bLogIndividual) System.out.println("\t" + CPH + " infected locally!");
					//							infLocal++;
					//						}
					//					}
                }
            }
			
            localForceOfInfection = forceOfInfection;
            if (bLogDaily) {
                System.out.println("Local force of infection: " + localForceOfInfection);
            }
			
            int infMov = 0;
            pendingIterator = pendingRestrictedFarms.entrySet().iterator();
            while (pendingIterator.hasNext()) {
                Map.Entry<String, Double> entry = pendingIterator.next();
                String CPH = entry.getKey();
				
                forceOfInfection += entry.getValue();
				
                if (bLogIndividual) {
                    System.out.println("\t" + CPH + " can become infected by movement with p = " + entry.getValue());
                }
				
                double pick = Math.random();
                if (pick < entry.getValue()) {
                    if (susceptiblePremises.contains(CPH)) { // if farm was infected through movement and locally, we put it into E earlier
                        susceptiblePremises.remove(CPH);
                        restrictedPremises.put(CPH, MOVEMENT_RESTRICTIONS);
                        latentPeriods.put(CPH, drawLatentPeriod());
						
                        if (bLogIndividual) {
                            System.out.println("\t" + CPH + " infected through movement!");
                        }
                        infoText += "\t" + CPH + " infected through movement!\n";
                        infMov++;
                    }
                }
            }
            if (bLogDaily) {
                System.out.println("Overall force of infection: " + forceOfInfection);
            }
            if (bLogDaily) {
                System.out.println("Overall infected: " + (infLocal + infMov));
            }
			
            if (bLogDaily) {
                System.out.println("R0 = " + forceOfInfection + " / " + infectiousSize + " = " + forceOfInfection / infectiousSize);
            }
			
            crudeR0 *= (infLocal + infMov);
			//			crudeR0 = forceOfInfection / infectiousSize;
			
            if (bLogDaily) {
                System.out.println("crudeR0 = " + crudeR0);
            }
			
            numOfLocallyInfected += infLocal;
            numOfInfectedByMov += infMov;
			
            // REMOVAL STAGE
            // CONTROL STRATEGIES
            int examinedI = 0, examinedE = 0, examinedH = 0, examinedS = 0;
            int newCulled = 0;
			
            if (step < CONTROL_MEASURES_DELAY) {
                numOfInfectedBeforeNMB = numOfLocallyInfected + numOfInfectedByMov;
            }
			
            // cull all the confirmed IPs (the next day, after 24 hours after confirmation)
            ArrayList<String> toCull = new ArrayList<String>();
            for (String cph : confirmedIPs) {
                if (culledPremises.contains(cph)) {
                    System.err.println("WAAAT?! " + cph + " already culled!");
                    JOptionPane.showMessageDialog(null, "Problem with SID " + SID + ": WAAAT?! " + cph + " already culled!");
                    System.exit(0);
                }
                culledPremises.add(cph);
                toCull.add(cph);
                if (bLogIndividual) {
                    System.out.println("\t" + cph + " was culled!");
                }
                infoText += "\t" + cph + " was culled!\n";
            }
			
            for (String cph : toCull) {
            	// Output culled farms
            	if (bOutputCulledDaily) {
                	culledDailyFile.getWriter().format("%d\t%s\n", step, cph);
                	culledDailyFile.getWriter().flush();
            	}
            	
                confirmedIPs.remove(cph);
                newCulled++;
            }
            // process investigationQueue: examine premises, add new ones to the queue
            ArrayList<String> toExamine = new ArrayList<String>();
            for (Iterator<String> iter = investigationQueue.keySet().iterator(); iter.hasNext();) {
                String CPH = iter.next();
				
                int prev = investigationQueue.get(CPH);
                if (prev == 0) {
                    toExamine.add(CPH);
                } else {
                    investigationQueue.put(CPH, prev - 1); // update time
                }
				
                infoText += "Farm " + CPH + " will be examined in " + prev + " days.\n";
            }
			
            // examine suspicious farms
            for (String cphExam : toExamine) {
                investigationQueue.remove(cphExam);
				
                if (confirmedIPs.contains(cphExam) || culledPremises.contains(cphExam)) { // it became confirmed after having been put in investigationQueue
					//						System.err.println("########################## ?! " + cphExam + " is already confirmed! #############################");
					//						System.exit(0);
                    continue;
                }
				
                if (exposedPremises.containsKey(cphExam)) {
                    examinedE++;
                    continue; // no clinical signs, visit another farm
                } else if (susceptiblePremises.contains(cphExam)) {
                    examinedS++;
                    continue; // no clinical signs, visit another farm
                } else if (restrictedPremises.containsKey(cphExam)) {
                    if (restrictedPremises.get(cphExam) < MOVEMENT_RESTRICTIONS - latentPeriods.get(cphExam)) {
                        // farm is in H, but shows clinical signs!
                        examinedH++; // TODO: should be different counter for this case
                        confirmedIPs.add(cphExam);
                        restrictedPremises.remove(cphExam);
                        if (bLogIndividual) {
                            System.out.println("\tRestricted farm (with infectious animals) " + cphExam + " is confirmed!");
                        }
                        infoText += "\tRestricted farm (with infectious animals) " + cphExam + " is confirmed!\n";
						//						JOptionPane.showMessageDialog(null, "\tRestricted farm (with infectious animals) " + cphExam + " is confirmed!");
                    } else {
                        examinedH++;
                        continue; // no clinical signs, visit another farm
                    }
                } else if (infectiousPremises.containsKey(cphExam)) {
                    confirmedIPs.add(cphExam);
                    examinedI++;
                    infectiousPremises.remove(cphExam);
					
                    if (bLogIndividual) {
                        System.out.println("\tInfectious farm " + cphExam + " is confirmed!");
                    }
                    infoText += "\tInfectious farm " + cphExam + " is confirmed!\n";
                } else {
                    System.err.println("########################## WHERE WAS " + cphExam + "??? #############################");
					
                    JOptionPane.showMessageDialog(null, "Problem with SID " + SID + ": ########################## WHERE WAS " + cphExam + "??? #############");
                    System.exit(0);
                }
				
                // IF WE GET HERE, THE FARM cph WAS DETECTED!
                // NEW IP!!!
                ////////////////////////////////////////////////////////////////
                // GET CLOSELY SITUATED FARMS		
				//					for (String farm : SpatialProbabilityMatrix.getCloseFarms(cphExam)) {
				//						if (confirmedIPs.contains(farm) || culledPremises.contains(farm)) // do not cull two times!
				//							continue;
				//						else
				//							confirmedIPs.add(farm); // to cull next day
				//						
				//						if (susceptiblePremises.contains(farm)) {// pre-emptive cull of susceptible premises
				//							susceptiblePremises.remove(farm);
				//							numOfConfirmedSusceptible++;
				//							if (bLogIndividual) System.out.println("\t" + farm + " was susceptible, but is set to cull on suspicion (close to IP)!");
				//							continue;
				//						} else if (exposedPremises.containsKey(farm)) {
				//							exposedPremises.remove(farm);
				//							numOfConfirmedExposed++;
				//							if (bLogIndividual) System.out.println("\t" + farm + " was exposed (E), but is set to cull on suspicion (close to IP)!");
				//							continue;
				//						} else if (restrictedPremises.containsKey(farm)) {
				//							restrictedPremises.remove(farm);
				//							numOfConfirmedRestricted++;
				//							if (bLogIndividual) System.out.println("\t" + farm + " was restrictde (H), but is set to cull on suspicion (close to IP)!");
				//							continue;
				//						} else if (infectiousPremises.containsKey(farm)) {
				//							infectiousPremises.remove(farm);
				//							numOfConfirmedInfected++;
				//							if (bLogIndividual) System.out.println("\t" + farm + " was infectious (I), but is set to cull on suspicion (close to IP)!");
				//							continue;
				//						} else {
				//							System.err.println("########################## WHERE IS " + farm + "??? #############################");
				//							JOptionPane.showMessageDialog(null, "Problem with SID " + SID + ": ########################## WHERE IS " + farm + "??? #############");
				//							System.exit(0);
				//						}
				//					}
				//					ArrayList<String> surveillanceZoneFarms = spatialProbabilityMatrix.getFarmsInSurveillanceZone(cphExam);					
				//					for (String farm : SpatialProbabilityMatrix.getFarmsInSurveillanceZone(cphExam)) {
				//						if (updateInvestigationQueue(farm, DELAY_LOCAL_CONTACTS))
				//							;//numOfExaminations++;
				//					}
                // CONTACT TRACING: GET MOVEMENT CONTACTS (direct and via markets)
                if (step >= CONTROL_MEASURES_DELAY) {
                    System.out.println("\tContact tracing for " + cphExam);
                    Calendar startCTCal = Utils.fromStringToCalendar(startDateString);
                    startCTCal.add(Calendar.DATE, CONTROL_MEASURES_DELAY);
					
                    Calendar pastCal = (Calendar) nowCal.clone();
                    pastCal.add(Calendar.DATE, -CONTACT_TRACING_DEPTH);
					
                    if (startCTCal.compareTo(pastCal) <= 0) {	//  start of CT <= pastCal
                        pastCal = (Calendar) startCTCal.clone();
                    }
					
                    HashMap<String, String> dangerousMarkets = new HashMap<String, String>(); // CPH, date
					//					ArrayList<String> dangerousMarkets = new ArrayList<String>();
					
                    for (Iterator<String> i = movementsData.getMovementsIterator(); i.hasNext();) {
                        String key = (String) i.next();
                        MovementsDataRecord movementRecord = movementsData.getRecord(key);
						
                        Calendar tmpCal = Utils.fromStringToCalendar(movementRecord.date);
						
                        if (startCTCal.compareTo(tmpCal) >= 0 && // tmp < start of CT
							pastCal.compareTo(tmpCal) <= 0) { 	// past = (now - CONTACT_TRACING_DEPTH) <= tmp
                            if (movementRecord.srcCPH.equals(cphExam)) {
                                if (movementRecord.mktCPH.isEmpty()) {
                                    if (updateInvestigationQueue(movementRecord.rcpCPH, DELAY_DIRECT_MOVEMENTS)) {// direct
										//										numOfDetectedMovements++;
                                        System.err.println("\t" + movementRecord.rcpCPH + " traced (direct)!");
                                        infoText += "\t" + movementRecord.srcCPH + " traced (direct)! " + DELAY_DIRECT_MOVEMENTS + " days delay.\n";
                                    }
                                } else {
                                    dangerousMarkets.put(movementRecord.mktCPH, movementRecord.date);
                                    System.out.println("Market " + movementRecord.mktCPH + " was at risk on " + movementRecord.date);
                                }
                            } else if (movementRecord.rcpCPH.equals(cphExam)) {
                                if (movementRecord.mktCPH.isEmpty()) {
                                    if (updateInvestigationQueue(movementRecord.srcCPH, DELAY_DIRECT_MOVEMENTS)) {// direct
										//										numOfDetectedMovements++;
                                        System.err.println("\t" + movementRecord.srcCPH + " traced (direct)!");
                                        infoText += "\t" + movementRecord.srcCPH + " traced (direct)! " + DELAY_DIRECT_MOVEMENTS + " days delay.\n";
                                    }
                                } else {
                                    dangerousMarkets.put(movementRecord.mktCPH, movementRecord.date);
                                    System.out.println("Market " + movementRecord.mktCPH + " was at risk on " + movementRecord.date);
                                }
                            }
                        }
                    }
					
                    // Now we consider dangerous markets
                    for (Iterator<String> i = movementsData.getMovementsIterator(); i.hasNext();) {
                        String key = (String) i.next();
                        MovementsDataRecord movementRecord = movementsData.getRecord(key);
						
                        Calendar tmpCal = Utils.fromStringToCalendar(movementRecord.date);
						
                        if (startCTCal.compareTo(tmpCal) >= 0 && pastCal.compareTo(tmpCal) <= 0) {
                            Iterator<Map.Entry<String, String>> dmIterator = dangerousMarkets.entrySet().iterator();
                            while (dmIterator.hasNext()) {
                                Map.Entry<String, String> entry = dmIterator.next();
                                String mktCPH = entry.getKey();
                                String date = entry.getValue();
								
                                if (movementRecord.mktCPH.equals(mktCPH) && movementRecord.date.equals(date)) {
                                    if (updateInvestigationQueue(movementRecord.rcpCPH, DELAY_MARKET_MOVEMENTS)) { // via markets
                                        //numOfDetectedMovements++;
                                        System.err.println("\t" + movementRecord.rcpCPH + " traced (MARKET)!");
                                        infoText += "\t" + movementRecord.rcpCPH + " traced (MARKET)! " + DELAY_MARKET_MOVEMENTS + " days delay.\n";
                                    }
									
									//									JOptionPane.showMessageDialog(null, movementRecord.rcpCPH + " traced (MARKET)! From " + 
									//										movementRecord.srcCPH + " on " + movementRecord.date);
                                    // SRC is already traced!!! Just before this cycle
									//									if (updateInvestigationQueue(movementRecord.srcCPH, DELAY_MARKET_MOVEMENTS)) // via markets
									//										//numOfDetectedMovements++;
									//										System.err.println("\t" + movementRecord.srcCPH + " traced (MARKET)!");
                                }
                            }
                        }
                    }
                }
				
                ////////////////////////////////////////////////////////////////
            }
			
            if (bLogDaily) {
                System.out.println("\tExamined: I (" + examinedI + "), E (" + examinedE + "), H (" + examinedH + "), S (" + examinedS + ").");
            }
			
            // DETECTION BY CLINICAL SIGNS (adding new premises for identification)
			//				ArrayList<String> detected = new ArrayList<String>();
            Iterator<Map.Entry<String, Integer>> infectiousIterator = infectiousPremises.entrySet().iterator();
            System.out.println("There are " + infectiousPremises.size() + " infectious premises to iterate over");
			
            while (infectiousIterator.hasNext()) {
                Map.Entry<String, Integer> entry = infectiousIterator.next();
                String cph = entry.getKey();
                System.out.println("CPH of infectious premises is " + cph);
                if (culledPremises.contains(cph)) {
                    System.out.println("This is a culled premises");
                    System.err.println(cph + " culled?! O_o");
                    continue;
                }
				
                if (clinicalSigns.get(cph) == null) {
                    clinicalSigns.put(cph, drawClinicalSignsPeriod());
                }
				
                System.out.println("About to check if farm was detected by clinical signs");
                if (step - entry.getValue() >= clinicalSigns.get(cph)) {
                    if (updateInvestigationQueue(cph, 0)) { //0 because it will be EXAMINED the next day.
                        numOfDetectedRandomly++;
                    }
                    if (bLogIndividual) {
                        System.out.println("\t" + cph + " (I) was detected by clinical signs!");
                    }
                    infoText += "\t" + cph + " (I) was detected by clinical signs!\n";
                }
            }
            System.out.println("There are " + restrictedPremises.size() + " restricted premises to iterate over");
            Iterator<Map.Entry<String, Integer>> restrictedIterator = restrictedPremises.entrySet().iterator();
            while (restrictedIterator.hasNext()) {
                Map.Entry<String, Integer> entry = restrictedIterator.next();
                String cph = entry.getKey();
				
                if (culledPremises.contains(cph)) {
                    System.err.println(cph + " culled?! O_o");
                    continue;
                }
				
                if (clinicalSigns.get(cph) == null) {
                    clinicalSigns.put(cph, drawClinicalSignsPeriod());
                }
				
                if (entry.getValue() < MOVEMENT_RESTRICTIONS - clinicalSigns.get(cph) - latentPeriods.get(cph)) {
                    if (updateInvestigationQueue(cph, 0)) { //0 because it will be EXAMINED the next day.
                        numOfDetectedRandomly++;
                    }
					//						detected.add(cph);
                    if (bLogIndividual) {
                        System.out.println("\t" + cph + " (H) was detected by clinical signs!");
                    }
                    infoText += "\t" + cph + " (H) was detected by clinical signs!\n";
                }
            }
            //System.out.println("Got this far 4 !!!!!!!");
			
			//				for (String cph : detected) {
			//					infectiousPremises.remove(cph);
			//					confirmedIPs.add(cph);
			//				}
            allExaminedS += examinedS;
            allExaminedH += examinedH;
            allExaminedE += examinedE;
            allExaminedI += examinedI;
			
            if (bOutputDaily) {
                rFile.getWriter().format("%d\t%d\t%d\t%d\t%d\t%d\t%d\t",
										 step,
										 susceptiblePremises.size(),
										 restrictedPremises.size(),
										 exposedPremises.size(),
										 infectiousPremises.size(),
										 confirmedIPs.size(),
										 culledPremises.size());
				
                rFile.getWriter().format("%d\t%d\t%d\t",
										 infLocal, // new E
										 infMov, // new H
										 newInf);	// new I
				
                rFile.getWriter().format("%d\t%d\t%d\t%d\t",
										 numOfConfirmedSusceptible,
										 numOfConfirmedRestricted,
										 numOfConfirmedExposed,
										 numOfConfirmedInfected);
				
                rFile.getWriter().format("%d\t%d\t%d\t%d\t",
										 examinedS,
										 examinedH,
										 examinedE,
										 examinedI);
				
                rFile.getWriter().format("%f\t%f\t%f\t%f\t",
										 localForceOfInfection,
										 forceOfInfection,
										 forceOfInfection / infectiousSize,
										 crudeR0);
				
                rFile.getWriter().format("%d\t%d\t%d\t%d\t",
										 numOfLocallyInfected,
										 numOfInfectedByMov,
										 newCulled,
										 allExaminedI);
				
                rFile.getWriter().format("%d\t",
										 numOfDetectedRandomly//,
										 //numOfDetectedMovements
										 );
				
                rFile.getWriter().format("\n");
            }
            
			
            step++;
            epidemicDuration = step;
            System.out.println("Step is "+step);
            System.out.println("infectiousPremises size: "+infectiousPremises.size());
			System.out.println("exposedPremises size: "+exposedPremises.size());
			System.out.println("restrictedPremises size: "+restrictedPremises.size());
			System.out.println("confirmedIPs size: "+confirmedIPs.size());
			System.out.println("investigationQueue size: "+investigationQueue.size());
			
        } // end of day
		
        if (bOutputDaily) {
            rFile.close();
        }
        
        if (bOutputCulledDaily) {
        	culledDailyFile.close();
        }
    }
	
    private boolean updateInvestigationQueue(String farm, int days) {
        Integer prev = investigationQueue.get(farm);
		
        if (culledPremises.contains(farm)) {
			//			System.err.println( farm + " should not be put to investigationQueue, it's already culled!");
            return false;
        } else if (confirmedIPs.contains(farm)) {
			//			System.err.println( farm + " should not be put to investigationQueue, it's already confirmed!");
            return false;
        }
		
        if (prev == null) {
            investigationQueue.put(farm, days);
            return true;
        } else {
            investigationQueue.put(farm, Math.min(prev, days)); // if already in the queue, choose minimal time
            return false;
        }
    }
	
    private void putToPending(HashMap<String, Double> farms, String cph, double prob) {
        if (prob == 0.0) {
            return;
        }
		
        if (!farms.containsKey(cph)) {
            farms.put(cph, prob);
        } else {
            double prev = farms.get(cph);
            farms.put(cph, 1 - (1 - prob) * (1 - prev));
        }
		
    }
	
    public ArrayList<String> getCulledPremises() {
        return culledPremises;
    }
	
    public ArrayList<String> getInfectiousPremises() {
        return new ArrayList<String>(infectiousPremises.keySet());
    }
	
    public int getEpidemicDuration() {
        return epidemicDuration;
    }
	
    public int getNumOfSusceptible() {
        return susceptiblePremises.size();
    }
	
    public int getNumOfInfectedBeforeNMB() {
        return numOfInfectedBeforeNMB;
    }
	
    public int getNumOfInfectedLocally() {
        return numOfLocallyInfected;
    }
	
    public int getNumOfInfectedViaMovements() {
        return numOfInfectedByMov;
    }
	
    public int getNumOfCulled() {
        return culledPremises.size();
    }
	
    public int getNumOfConfirmedSusceptible() {
        return numOfConfirmedSusceptible;
    }
	
    public int getNumOfConfirmedExposed() {
        return numOfConfirmedExposed;
    }
	
    public int getNumOfConfirmedRestricted() {
        return numOfConfirmedRestricted;
    }
	
    public int getNumOfConfirmedInfected() {
        return numOfConfirmedInfected;
    }
	
    public int getNumOfDetectedRandomly() {
        return numOfDetectedRandomly;
    }
	
	//	public int getNumOfDetectedByMovements() {
	//		return numOfDetectedMovements;
	//	}
	//	public int getNumOfDetectedLocally() {
	//		return numOfDetectedLocal;
	//	}
    public int getExaminationsS() {
        return allExaminedS;
    }
	
    public int getExaminationsH() {
        return allExaminedH;
    }
	
    public int getExaminationsE() {
        return allExaminedE;
    }
	
    public int getExaminationsI() {
        return allExaminedI;
    }
	
    private void saveCulledFarms(String fileName) {
        FileOutput resultsFile = new FileOutput(fileName);
        for (String cph : culledPremises) {
            resultsFile.getWriter().format("%s\n", cph);
        }
        resultsFile.close();
    }
	
    public int getExposed() {
        return exposedPremises.size();
    }
	
    public int getInfectious() {
        return infectiousPremises.size();
    }
	
    public int getIntermediateSusceptible() {
        return intermediateSusceptible;
    }
	
    public int getIntermediateInfectedLocally() {
        return intermediateInfectedLocally;
    }
	
    public int getIntermediateExposed() {
        return intermediateExposed;
    }
	
    public int getIntermediateInfectious() {
        return intermediateInfectious;
    }
	
    public int getIntermediateCulled() {
        return intermediateCulled;
    }
	
	
    private int drawLatentPeriod() {
		//		return LATENT_PERIOD;
        double weights[] = //{0, 0, 0, 0.5, 0.5, 0}; // weights for 1, 2, 3, 4...
		{0.115880750, 0.254691654, 0.314876795, 0.307582451, 0.264073610, 0.208944488, 0.156267233,
			0.112148957, 0.077990860, 0.052905657, 0.035174741, 0.023001251, 0.014832662, 0.009452178, 0.005962134, 1};
		
        double pick = Math.random();
        int i = 0;
        double sum = 0;
        for (; pick > sum; i++)
            sum += weights[i];
		
		//		System.err.println(i);
        return i;
    }
	
    private int drawClinicalSignsPeriod() {
        System.out.println("drawClinicalSignsPeriod!!!");
		//		return DETECTION_TIME;
        
        double weights[] = {
            0, 0.25, 0.25, 0.25, 0.1, 0.1, 0.05}; // weights for 1, 2, 3, 4...
		
        double pick = Math.random();
        System.out.println("Random number chosen is: " + pick);
        int i = 0;
        double sum = 0;
        for (; pick > sum; i++)
            sum += weights[i];
        
		//		System.err.println(i);
        return i;
    }
	
}
