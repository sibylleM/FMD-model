package gui;

import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import utils.Utils;
import utils.FileOutput;
import data.InfectionsData;
import data.MarketsData;
import data.MovementsData;
import data.FarmsData;
import java.util.HashMap;
import methods.EpidemicModellingThread;

public class StartThreadDialog extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int repeats = 1;
	int numOfThreads = 1;
	boolean bNew = true;
	boolean bLocal = true;
	boolean bInfoWindows = false;
	boolean bDrawIndexCases = false;
	String startDate = "01/01/2011";
	//String startDate = "01/10/2010";
	int readRate = 100;
	
	
	String outputFile;
	
	static FileOutput summaryFileOutput;
	boolean bSummaryExists = false;
	
	EpidemicModellingThread threads[];
	
	//static final String[] startDates = { 
	// "01/01/2010", "15/01/2010",
	//"01/02/2010", "15/02/2010", 
	//"01/03/2010", "15/03/2010", 
	//"01/04/2010", "17/04/2010", 
	//"01/05/2010", "15/05/2010", 
	//"01/06/2010", "15/06/2010", 
	//"01/07/2010", "15/07/2010", 
	//"01/08/2010", "15/08/2010", 
	//"01/09/2010", "15/09/2010", 
	//"01/10/2010", "15/10/2010", 
	//"01/11/2010", "15/11/2010", 
	//"01/12/2010", "15/12/2010",
	//"01/01/2011", "15/01/2011",
	//"01/02/2011", "15/02/2011", 
	//"01/03/2011", "15/03/2011", 
	//"01/04/2011", "17/04/2011", 
	//"01/05/2011", "15/05/2011", 
	//"01/06/2011", "15/06/2011", 
	//"01/07/2011", "15/07/2011", 
	//"01/08/2011", "15/08/2011", 
	//"01/09/2011", "15/09/2011", 
	//"01/10/2011", "15/10/2011", 
	//"01/11/2011", "15/11/2011", 
	//"01/12/2011", "15/12/2011"};
	
	//static final String[] startDates = { 
	//            "01/10/2010", 
	//            "02/10/2010",
	//            "03/10/2010",
	//            "04/10/2010",
	//            "05/10/2010",
	//            "06/10/2010",
	//            "07/10/2010",
	//            "08/10/2010",
	//            "09/10/2010",
	//            "10/10/2010",
	//            "11/10/2010",
	//            "12/10/2010",
	//            "13/10/2010",
	//            "14/10/2010",
	//            "15/10/2010",
	//            "16/10/2010",
	//            "17/10/2010",
	//            "18/10/2010",
	//            "19/10/2010",
	//            "20/10/2010",
	//            "21/10/2010",
	//            "22/10/2010",
	//            "23/10/2010",
	//            "24/10/2010",
	//            "25/10/2010",
	//            "26/10/2010",
	//            "27/10/2010",
	//            "28/10/2010",
	//            "29/10/2010",
	//            "30/10/2010",
	//            "31/10/2010"};
	
	static final String[] startDates = { 
/*	"01/2010", 
	"02/2010",
	"03/2010",
	"04/2010",
	"05/2010",
	"06/2010",
	"07/2010",
	"08/2010",
	"09/2010",
	"10/2010",
	"11/2010",
	"12/2010", */
	"01/2011", 
	"02/2011",
	"03/2011",
	"04/2011",
	"05/2011",
	"06/2011",
	"07/2011",
	"08/2011",
	"09/2011",
	"10/2011",
	"11/2011",
	"12/2011",
	
	};
	
	static final String[] peakStartDates = {
	//"01/10/2011", "15/10/2011"
	
	// "01/08/2010", "15/08/2010", 
	//	"01/09/2010", "15/09/2010", 
	//	"01/10/2010",
	//	"01/08/2011", "15/08/2011", 
	//	"01/09/2011", "15/09/2011", 
	//	"01/10/2011"
	};
	
	static final String[] pairStartDates = {
	//"01/10/2011", "15/10/2011"
	
	//       "15/09/2010", 
	//	"01/10/2010",
	//	"15/09/2011", 
	//	"01/10/2011"
	};
	//
	//	{ "01/08/2011", "01/09/2011", "01/10/2011", "01/11/2011", "01/12/2011", "01/01/2012", "01/02/2012", "01/03/2012", "01/04/2012", "01/05/2012", "01/06/2012", "01/07/2012", "01/08/2012" };
	
	public StartThreadDialog(final FarmsData premisesData, final MarketsData marketsData, final MovementsData movementsData, final InfectionsData infectionsData) {
		
		setLayout(new GridLayout(0,2));
		
		// TODO: set initial values for TIMES, etc.
		
		Integer[] threadsToChoose = { 1, 2, 4, 6, 8, 10 };
	    JComboBox<Integer> threadsComboBox = new JComboBox<Integer>(threadsToChoose);
	    threadsComboBox.setSelectedIndex(0);
	    threadsComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox<Integer> cb = (JComboBox<Integer>) e.getSource();
				numOfThreads = (Integer) cb.getSelectedItem();		
			}
		});
	    
	    Integer[] repeatsToChoose = { 1, 2, 3, 4, 5, 10, 20, 100, 500, 1000};
	    JComboBox<Integer> repeatsComboBox = new JComboBox<Integer>(repeatsToChoose);
	    repeatsComboBox.setSelectedIndex(0);
	    repeatsComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox<Integer> cb = (JComboBox<Integer>) e.getSource();
			    repeats = (Integer) cb.getSelectedItem();		
			}
		});
	    
	    JCheckBox bNewChkBox = new JCheckBox("", true);
        bNewChkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			    bNew = ((JCheckBox) e.getSource()).isSelected();		
			}
		});
		
        JCheckBox bLocalChkBox = new JCheckBox("", true);
        bLocalChkBox.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				bLocal = ((JCheckBox) e.getSource()).isSelected();
			}
		});  
        
		JCheckBox bInfoWindowsBox = new JCheckBox("", false);
		bInfoWindowsBox.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				bInfoWindows = ((JCheckBox) e.getSource()).isSelected();
			}
		});        
		
		
		JCheckBox bDrawIndexCasesBox = new JCheckBox("", false);
		bDrawIndexCasesBox.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				bDrawIndexCases = ((JCheckBox) e.getSource()).isSelected();
			}
		});        
		
	    ArrayList<String> startDateToChoose = new ArrayList<String>();
	    for (int i = 0; i < startDates.length; i++)
	    	startDateToChoose.add(startDates[i]);
		//	    startDateToChoose.add("PAIR");
			    startDateToChoose.add("PEAK");
	    startDateToChoose.add("ALL");
	    @SuppressWarnings({ "unchecked", "rawtypes" })
		JComboBox<String> startDateComboBox = new JComboBox(startDateToChoose.toArray());
	    startDateComboBox.setSelectedIndex(0);
	    startDateComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox<String> cb = (JComboBox<String>) e.getSource();
				startDate = (String) cb.getSelectedItem();		
			}
		});    
	    
	    Integer[] readRateToChoose = { 0, 80, 90, 95, 100 };
	    JComboBox<Integer> readRateComboBox = new JComboBox<Integer>(readRateToChoose);
	    readRateComboBox.setSelectedIndex(4);
	    readRateComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox<Integer> cb = (JComboBox<Integer>) e.getSource();
				readRate = (Integer) cb.getSelectedItem();		
			}
		});
	    
	    
	    String[] strOptions = { "output/tmp.csv", "output/results.csv", "output/resultsX.csv" };
	    JComboBox<String> outputFileComboBox = new JComboBox<String>(strOptions);
	    outputFileComboBox.setEditable(true);
	    outputFileComboBox.setSelectedIndex(0);
	    outputFile = strOptions[0];
	    outputFileComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox<String> cb = (JComboBox<String>) e.getSource();
				outputFile = (String) cb.getSelectedItem();
			}
		});
		
		//		final JButton indexButton = new JButton("Choose ONE index case");
		//        indexButton.addActionListener(new ActionListener() {
		//            public void actionPerformed(ActionEvent event) {
		//            	while (infectionsData.getInfectedOnDatePremises(Aux.fromStringToCalendar(startDate)).size() > 1) // TODO: BRUTAL...
		//            		infectionsData.getData().remove(0);
		//            			
		//            }
		//        });
		
		final JButton startButton = new JButton("Start threads");
        startButton.addActionListener(new ActionListener() {
            @SuppressWarnings("deprecation")
			public void actionPerformed(ActionEvent event) {
            	setCursor(Cursor.WAIT_CURSOR);        		
				
				//        		long startTime = System.currentTimeMillis();
				//        		System.out.println("Starting...");
            	
            	//String[] actualStartDates = new String[]{};
                ArrayList<String> actualStartDates = new ArrayList();
            	
				/*	if (startDate.equals("ALL")) 
				 actualStartDates = startDates;
				 else if (startDate.equals("PEAK"))
				 actualStartDates = peakStartDates;
				 else if (startDate.equals("PAIR"))
				 actualStartDates = pairStartDates;
				 else {*/
            	
				//            	System.out.print("Start dates: ");
				//            	for (String str: actualStartDates)
				//            		System.out.println(str);
				//            	System.out.println();
                
                HashMap<String,Integer> daysInMonth = new HashMap<>();
                daysInMonth.put("01", 31);
                daysInMonth.put("02", 28);
                daysInMonth.put("03", 31);
                daysInMonth.put("04", 30);
                daysInMonth.put("05", 31);
                daysInMonth.put("06", 30);
                daysInMonth.put("07", 31);
                daysInMonth.put("08", 31);
                daysInMonth.put("09", 30);
                daysInMonth.put("10", 31);
                daysInMonth.put("11", 30);
                daysInMonth.put("12", 31);
                
                System.out.println("Date chosen on interface is: "+startDate);
                String month = startDate.substring(0, 2);
                System.out.println("Month chosen is: "+month);
                String year = startDate.substring(3, 7);
                System.out.println("Year chosen is: "+year);
				
                
                int numberRandom = 20;
                int max = daysInMonth.get(month);
                int min = 1;
                //int randomDay = min + (int) (Math.random() * ((max - min) + 1));
                //System.out.println("Random day chosen is: "+randomDay);
                // randomDayString = Integer.toString(randomDay);
                //if(randomDay<10)
                //    randomDayString = "0"+randomDayString;
				
                
                //String fullDate = ""+randomDayString+"/"+month+"/"+year;
                //System.out.println("fullDate chosen is: "+fullDate);
                String fullDate = "";
                for(int i=0;i<=numberRandom;i++){
					int randomDay = min + (int) (Math.random() * ((max - min) + 1));
					fullDate = ""+randomDay+"/"+month+"/"+year;
					System.out.println("fullDate chosen is: "+fullDate);
					//actualStartDates = new String[] {fullDate};
					actualStartDates.add(fullDate);
                }
				
                
				//  }
				
            	
            	// ADD HEADER TO THE FILE
            	boolean bHeader = !Utils.bFileExists(outputFile);
            	FileOutput resultsFile = new FileOutput(outputFile, true);
                if (bHeader) {		
        			resultsFile.getWriter().format("%s,%s, %s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,",
												   "Date", "Time",
												   "Start date",
												   "NUMBER_OF_INDEX_CASES", "DURATION_OF_INITIAL_INFECTIONS", 
												   "BETA", "DIRECT_MOVEMENT", "OFF_MARKET_MOVEMENT", 
												   "LOCAL_SPREAD", "LATENT_PERIOD", "MOVEMENT_RESTRICTIONS", 
												   "CONTROL_MEASURES_DELAY", "MOVEMENT_BAN_DELAY", "CONTACT_TRACING_DEPTH", 
												   "DELAY_LOCAL_CONTACTS", "DELAY_DIRECT_MOVEMENTS", 
												   "DELAY_MARKET_MOVEMENTS", "DETECTION_TIME",
												   "CLOSE_DISTANCE");
        			
        			resultsFile.getWriter().format("%s,%s, %s,%s,%s,%s,%s,%s, %s,%s,%s, %s,%s,%s,%s, %s, %s,%s,%s,%s, %s,%s,%s,%s,%s\n", "Run ID", "Repeat",
												   "Index case",
												   "Epidemic duration",
												   "Susceptible (final)",
												   "Infected locally (final)", 
												   "Infected through movements",
												   "Infected before NMB",
												   
												   "Exposed (final)",
												   "Infectious (final)",
												   "Culled (final)",
												   
												   "Confirmed susceptible",
												   "Confirmed restricted",
												   "Confirmed exposed",
												   "Confirmed infected",
												   
												   "Detected randomly",
												   
												   "Examined susceptible",
												   "Examined restricted",
												   "Examined exposed",
												   "Examined infected",
												   
												   "Susceptible (im)",
												   "Infected locally (im)",
												   "Exposed (im)",
												   "Infectious (im)",
												   "Culled (im)");
        		}
                resultsFile.close();
                
                
                if (!bSummaryExists) {
                	summaryFileOutput = new FileOutput("output/summary.csv");
                	bSummaryExists = true;
                } else
                	summaryFileOutput = new FileOutput("output/summary.csv", true);
				
            	for (String sDate : actualStartDates) {            		
	            	threads = new EpidemicModellingThread[numOfThreads];
// Altered by Kajetan
	            	ArrayList<Integer> usedSIDs = new ArrayList<Integer>(numOfThreads);
	          
	        		for (int i=0; i < threads.length; i++) {
		        		int SID;
		        		do {
		        			SID = (int) (Math.random() * 1000); // session ID
		        		} while (usedSIDs.contains(SID));
		        		usedSIDs.add(SID);
		        		threads[i]= new EpidemicModellingThread(premisesData, marketsData, movementsData, infectionsData, repeats, bNew, bLocal, sDate, readRate, SID, bInfoWindows, bDrawIndexCases, 
															 outputFile, summaryFileOutput);
		        		EpidemicModellingThread thread = threads[i];
// END Altered by Kajetan
		        		String sNew = bNew ? "new" : "old";
		        		String sLocal = bLocal ? "local" : "mov";
		        		
						
		    			final JProgressBar progressBar = new JProgressBar(0,100);
		        		if (bInfoWindows) {
			    			JFrame frame = new JFrame("Running simulations... " + thread.repeats + " repeats, " + sNew + ", " + sLocal + ", " + readRate + "% read rate. SID: " + SID);
							
			    			progressBar.setStringPainted(true);
			    			progressBar.setVisible(true);
			    			progressBar.setValue(0);
			    			
			    			
			    			frame.setLayout(new FlowLayout());
							//    			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			    			frame.add(new JLabel(sDate));
			    			frame.add(progressBar);
			    			frame.setSize(500, 20);
			    			frame.setLocationByPlatform(true);
			        		frame.pack();
			    			frame.setVisible(true);
		        		}
		    			
		    			thread.addPropertyChangeListener(
														 new PropertyChangeListener() {
							public  void propertyChange(PropertyChangeEvent evt) {
								if ("progress".equals(evt.getPropertyName())) {
									if (bInfoWindows)
										progressBar.setValue((Integer) evt.getNewValue());
									//        			            	 progressBar.repaint();
								}
							}
						});
		    			
		    			thread.execute();
						
						//    			boolean bContinue = false;
						//        		while (!bContinue) {
						//    				if (thread.isDone()) {
						//    					bContinue = false;
						//    					frame.dispose();
						//    					break;
						//    				} else
						//    					bContinue = true;
						//    			}
		        		
						//        		long endTime = System.currentTimeMillis();		
						//        		System.out.println("The simulation finished in " + (endTime - startTime) + "ms.");
		        		
		        		
		        		setCursor(Cursor.DEFAULT_CURSOR);
		        		
	        		}
	        		
	            	for (EpidemicModellingThread thread: threads) {
	            		//if (!thread.isDone())
	            		//	;
	            		while(!thread.isDone()) {}   // Added by Kajetan
	            	}
        		}
            	
            	summaryFileOutput.close();
            }
        });
		
        
		
        add(new JLabel("Number of threads:"));
        add(threadsComboBox);
        
        add(new JLabel("Repeats (by thread):"));
        add(repeatsComboBox);
		
        add(new JLabel("New:"));
        add(bNewChkBox);        
		
        add(new JLabel("Local:"));
        add(bLocalChkBox);        
		
        add(new JLabel("Start date:"));
        add(startDateComboBox);
        
        add(new JLabel("Read rate:"));
        add(readRateComboBox); 
        
		
		
        add(new JLabel("Info windows:"));
        add(bInfoWindowsBox);
		
        add(new JLabel("Draw index cases on every step:"));
        add(bDrawIndexCasesBox);
        
		
	    add(new JLabel("Output file:"));
	    add(outputFileComboBox);
        
		
		//        add(indexButton);
        add(startButton);
        
        setTitle("Starting simulations...");
        setSize(300, 300);
        setLocationRelativeTo(null);
		//        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
		//        WindowListener exitListener = new WindowAdapter() {
		//            @Override
		//            public void windowClosing(WindowEvent e) {
		////                int confirm = JOptionPane.showOptionDialog(null, "Re-calculate spatial matrix?", "Confirm changes", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
		////                if (confirm == 0) {
		////               	setCursor(Cursor.WAIT_CURSOR);
		////                	spatialProbabilityMatrix.update(premisesData);
		////                	dlg.dispose();
		////                	setCursor(Cursor.DEFAULT_CURSOR);
		////                } else if (confirm == 1)
		////                 	dlg.dispose();
		////                // if -1, do nothing
		//            	for (EpidemicModellingThread thread : threads)
		//            		thread.
		//            }
		//        };
		//        addWindowListener(exitListener);
	}
}
