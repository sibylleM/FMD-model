package gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.filechooser.FileNameExtensionFilter;

import data.InfectionsData;
import data.MarketsData;
import data.MovementsData;
import data.FarmsData;
import methods.DataProcessing;
import methods.EpidemicModelling;
import methods.SpatialProbabilityMatrix;

public class MainFrame extends JFrame {

// DEFAULT INPUT FILES
	
//	static final String inputFolder = "input/cs/";
//	static final String farmsFileName = "Ag_Census_2010_raw.csv";
//	static final String marketsFileName = "SAMU_Sheep_Market_2011.csv";
//	static final String individualMovementsFileName = "SAMU_2010_11.csv";
//	static final String initillyaInfectedFarmsFileName = "indexCases2011.csv";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static String inputFolder = "/Users/sibyllemohr/original_model/FMDModel/executables/input/dummy/";	
	static String farmsFileName = "premises.csv";
	static String marketsFileName = "markets.csv";
	static String individualMovementsFileName = "cattle_move11.csv";
	

//	static String marketsFileName = "../cs/SAMU_Sheep_Market_2011.csv";
//	static String individualMovementsFileName = "../dummy.csv";
	
	
	// DATA
	FarmsData farmsData;
	MarketsData marketsData;
	MovementsData movementsData;
	InfectionsData infectionsData;

	/**
	 * Reads input data
	 */
	@SuppressWarnings("deprecation")
	private void initData() {
		setCursor(Cursor.WAIT_CURSOR);
		
		//Aux.createDirectory("output/" + startTime);
		/////

		farmsData = new FarmsData(inputFolder + farmsFileName);		
		marketsData = new MarketsData(inputFolder + marketsFileName);
		movementsData = new MovementsData(inputFolder + individualMovementsFileName, farmsData, marketsData);
		
//		movementsData.update(inputFolder + FMFMovementsFileName, 0); // readNum: 0 � actual (100%), 1 � 80%, ... , 5 � 97%.
		
//		MovementsData movementsData = new MovementsData(inputFolder + FMFMovementsFileName);
		
		
		
		//////////////////////////////
//		FileOutput logFile = new FileOutput("output/dataClearing.log");
//		FileOutput logFile = new FileOutput("output/mktClearing.log");
//		DataProcessing.logFile = logFile;531941
//		DataProcessing.clearFarms(premisesData, 0, 5050000, 530000, 610000);
//		DataProcessing.clearFarms(premisesData, 280000, 350000, 530000, 580000);
//		DataProcessing.clearData(premisesData, movementsData); // CLEAR NOT IMPORTANT FARMS!
//		DataProcessing.clearFarmsScotland(premisesData, movementsData);
//		DataProcessing.clearMovements(premisesData, movementsData);
//		logFile.close();
//		DataProcessing.logFile = null;
//		DataProcessing.testData(premisesData, movementsData);
		
//		DataProcessing.checkMarkets(premisesData, movementsData);
		//////////////////////////////
		
//		FileOutput logFile = new FileOutput("output/indexCases.log");
//		DataProcessing.logFile = logFile;
//		DataProcessing.extractIndexCases(farmsData, movementsData);
//		logFile.close();
//		DataProcessing.logFile = null;
		
//		DataProcessing.generateLocations(20, 20, "output/new_premises.csv");
//		System.out.println("File with new locations was generated!");
		

//		
//		premisesData.writeToFile("output/premises.csv");
//		movementsData.writeToFile("output/movements.csv");
//		

//		DataProcessing.calculateMovsByMonth(premisesData, movementsData);
//		DataProcessing.premisesStats(premisesData);
		DataProcessing.movementStats(movementsData);

//		infectionsData = new InfectionsData(inputFolder + initillyaInfectedFarmsFileName);
		infectionsData = new InfectionsData(farmsData, movementsData);
		

//		DataProcessing.calculateMovsByPeriod(premisesData, movementsData, EpidemicModelling.MOVEMENT_BAN_DELAY);


//		DataProcessing.checkIndexCases(premisesData, infectionsData);
//Sibylle: uncommented here
		infectionsData.writeToFile("output/initiallyInfectedFarms.csv");
		
		/////////////////// 
//		GraphViz.drawJustMap(premisesData, "output/FMDmapCOUNTIES.gv");
		///////////////////
		
		
//		newPremisesData = new PremisesData("input/cs/Ag_Census_2010_Misha_raw.csv");
//		System.out.println(newPremisesData.getRecord("66/006/001"));
		

//		DataProcessing.comparePremisesData(premisesData, newPremisesData);

		DataProcessing.prepareData(farmsData, marketsData, movementsData);
		DataProcessing.checkIndexCases(farmsData, infectionsData);

//		movementsData.writeToFile("input/newMovs.csv");
//		System.exit(0);
		
		SpatialProbabilityMatrix.init();
		SpatialProbabilityMatrix.update(farmsData);
		
		setCursor(Cursor.DEFAULT_CURSOR);
	}
		
	
	/**
	 * Creates the main window
	 */
	public MainFrame() {
		setLayout(new FlowLayout());
		
        
        ////////////////// BUTTONS

        final JButton startButton = new JButton("Start simulations");
        startButton.setEnabled(false);
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {            	
            	StartThreadDialog dlg = new StartThreadDialog(farmsData, marketsData, movementsData, infectionsData);
            	dlg.setVisible(true);
            }
        });


        final JButton optionsButton = new JButton("Distance kernel");
    	optionsButton.setEnabled(false);
        optionsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {            	
            	final OptionsDialog dlg = new OptionsDialog();
            	dlg.setVisible(true);
            	 WindowListener exitListener = new WindowAdapter() {
                     @SuppressWarnings("deprecation")
					@Override
                     public void windowClosing(WindowEvent e) {
                         int confirm = JOptionPane.showOptionDialog(null, "Re-calculate spatial matrix?", "Confirm changes", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
                         if (confirm == 0) {
                        	setCursor(Cursor.WAIT_CURSOR);
                         	SpatialProbabilityMatrix.update(farmsData);
                         	dlg.dispose();
                         	setCursor(Cursor.DEFAULT_CURSOR);
                         } else if (confirm == 1)
                          	dlg.dispose();
                         // if -1, do nothing
                     }
                 };
                 
            	dlg.addWindowListener(exitListener);
            }
        });
        
        final JButton initButton = new JButton("Read data");
        initButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	initData();
            	startButton.setEnabled(true);
            	initButton.setEnabled(false);
            	optionsButton.setEnabled(true);
//            	SpatialProbabilityMatrix.printDistanceKernel(100, 10000);
//            	spatialProbabilityMatrix.printNumOfCloseFarms();
//            	spatialProbabilityMatrix.printProbabilities();
            }
        });

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	System.exit(0);
            }
        });
        
        
        JButton testButton = new JButton("Test");
        testButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
				JOptionPane.showMessageDialog(null, (String.format("%d,%d,%f,%f,%f,%f,%d,%d,%d,%d,%d,%d,%d,%d,%d",
					EpidemicModelling.NUMBER_OF_INDEX_CASES, EpidemicModelling.DURATION_OF_INITIAL_INFECTIONS, 
					EpidemicModelling.BETA, EpidemicModelling.DIRECT_MOVEMENT, 
					EpidemicModelling.OFF_MARKET_MOVEMENT, EpidemicModelling.LOCAL_SPREAD, 
					EpidemicModelling.LATENT_PERIOD,  EpidemicModelling.MOVEMENT_RESTRICTIONS, 
					EpidemicModelling.CONTROL_MEASURES_DELAY, EpidemicModelling.MOVEMENT_BAN_DELAY, EpidemicModelling.CONTACT_TRACING_DEPTH, 
					EpidemicModelling.DELAY_LOCAL_CONTACTS, EpidemicModelling.DELAY_DIRECT_MOVEMENTS, 
					EpidemicModelling.DELAY_MARKET_MOVEMENTS, EpidemicModelling.DETECTION_TIME)));
            }
        });
        

        final JTextField inputFileText = new JTextField(inputFolder + farmsFileName);
        inputFileText.setPreferredSize(new Dimension(500, 20));
        inputFileText.setEditable(false);
        final JButton chooseInputFileButton = new JButton("Choose file...");
        chooseInputFileButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent event) {
        		if (event.getSource() == chooseInputFileButton) {
        			JFileChooser chooser = new JFileChooser(new File(inputFolder));
        			FileNameExtensionFilter filter = new FileNameExtensionFilter(
        					"CSV files", "csv");
        			chooser.setFileFilter(filter);
        			chooser.setDialogTitle("Choose farms file...");
        			int returnVal = chooser.showOpenDialog(MainFrame.this);
        			if (returnVal == JFileChooser.APPROVE_OPTION) {
        				String chosenFile = chooser.getSelectedFile().getName();
        				farmsFileName = chosenFile;
        				inputFolder = chooser.getSelectedFile().getParent() + "/";
        				inputFileText.setText(inputFolder + farmsFileName);
        			}
        		}
        	}
        });
        
        final JTextField inputFileText2 = new JTextField(inputFolder + marketsFileName);
        inputFileText2.setPreferredSize(new Dimension(500, 20));
        inputFileText2.setEditable(false);
        final JButton chooseInputFileButton2 = new JButton("Choose file...");
        chooseInputFileButton2.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent event) {
        		if (event.getSource() == chooseInputFileButton2) {
        			JFileChooser chooser = new JFileChooser(new File(inputFolder));
        			FileNameExtensionFilter filter = new FileNameExtensionFilter(
        					"CSV files", "csv");
        			chooser.setFileFilter(filter);
        			chooser.setDialogTitle("Choose markets file...");
        			int returnVal = chooser.showOpenDialog(MainFrame.this);
        			if (returnVal == JFileChooser.APPROVE_OPTION) {
        				String chosenFile = chooser.getSelectedFile().getName();
        				marketsFileName = chosenFile;
        				inputFolder = chooser.getSelectedFile().getParent() + "/";
        				inputFileText2.setText(inputFolder + marketsFileName);
        			}
        		}
        	}
        });
        
        final JTextField inputFileText3 = new JTextField(inputFolder + individualMovementsFileName);
        inputFileText3.setPreferredSize(new Dimension(500, 20));
        inputFileText3.setEditable(false);
        final JButton chooseInputFileButton3 = new JButton("Choose file...");
        chooseInputFileButton3.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent event) {
        		if (event.getSource() == chooseInputFileButton3) {
        			JFileChooser chooser = new JFileChooser(new File(inputFolder));
        			FileNameExtensionFilter filter = new FileNameExtensionFilter(
        					"CSV files", "csv");
        			chooser.setFileFilter(filter);
        			chooser.setDialogTitle("Choose movements file...");
        			int returnVal = chooser.showOpenDialog(MainFrame.this);
        			if (returnVal == JFileChooser.APPROVE_OPTION) {
        				String chosenFile = chooser.getSelectedFile().getName();
        				individualMovementsFileName = chosenFile;
        				inputFolder = chooser.getSelectedFile().getParent() + "/";
        				inputFileText3.setText(inputFolder + individualMovementsFileName);
        			}
        		}
        	}
        });
        
        
        ////////////////// END OF BUTTONS
        
        

        JPanel firstPanel = new JPanel(new BorderLayout());
        
        
        
        JPanel buttonPanel = new JPanel(new FlowLayout());

        buttonPanel.add(initButton);
        buttonPanel.add(startButton);
        buttonPanel.add(optionsButton);
        
        

        JPanel inputFilesPanel = new JPanel();
        
        inputFilesPanel.setBorder(BorderFactory.createTitledBorder("Input"));
        SpringLayout layout = new SpringLayout();

//        BoxLayout layout = new BoxLayout(inputFilesPanel, BoxLayout.Y_AXIS);
//        BoxLayout layout = new BoxLayout(inputFilesPanel, BoxLayout.Y_AXIS);
        inputFilesPanel.setLayout(layout);
        
        inputFilesPanel.add(inputFileText);
        inputFilesPanel.add(chooseInputFileButton);
        
        inputFilesPanel.add(inputFileText2);
        inputFilesPanel.add(chooseInputFileButton2);
        
        inputFilesPanel.add(inputFileText3);
        inputFilesPanel.add(chooseInputFileButton3);

        layout.putConstraint(SpringLayout.NORTH, inputFileText, 5, SpringLayout.NORTH, chooseInputFileButton);
        
        layout.putConstraint(SpringLayout.WEST, chooseInputFileButton, 5, SpringLayout.EAST, inputFileText);
        
        layout.putConstraint(SpringLayout.NORTH, inputFileText2, 15, SpringLayout.SOUTH, inputFileText);
        
        layout.putConstraint(SpringLayout.WEST, chooseInputFileButton2, 5, SpringLayout.EAST, inputFileText2);
        layout.putConstraint(SpringLayout.NORTH, chooseInputFileButton2, 10, SpringLayout.SOUTH, inputFileText);
        
        layout.putConstraint(SpringLayout.NORTH, inputFileText3, 15, SpringLayout.SOUTH, inputFileText2);
        
        layout.putConstraint(SpringLayout.WEST, chooseInputFileButton3, 5, SpringLayout.EAST, inputFileText3);
        layout.putConstraint(SpringLayout.NORTH, chooseInputFileButton3, 10, SpringLayout.SOUTH, inputFileText2);
        
        
        firstPanel.add(buttonPanel, BorderLayout.NORTH);
        firstPanel.add(inputFilesPanel, BorderLayout.CENTER);
        
        
        
        
	    
	    
	    
	    // OUTPUT

	    
        JPanel outputPanel = new JPanel(new GridLayout(0,2));
        
        outputPanel.setBorder(BorderFactory.createTitledBorder("Output"));
	    
	    JCheckBox graphvizChkBox = new JCheckBox("", false);
	    EpidemicModelling.bGraphviz = false;
	    graphvizChkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			    EpidemicModelling.bGraphviz = ((JCheckBox) e.getSource()).isSelected();		
			}
		});
	    outputPanel.add(new JLabel("Generate graphviz files:"));
	    outputPanel.add(graphvizChkBox);
	    
	    JCheckBox outputDailyChkBox = new JCheckBox("", false);
	    EpidemicModelling.bOutputDaily = false;
	    outputDailyChkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			    EpidemicModelling.bOutputDaily = ((JCheckBox) e.getSource()).isSelected();		
			}
		});
	    outputPanel.add(new JLabel("Generate daily output files:"));
	    outputPanel.add(outputDailyChkBox);
	    
	    
	    firstPanel.add(outputPanel, BorderLayout.SOUTH);
	    
//	    prefPanel.setSize(450, 300);
//	    prefPanel.setBorder(BorderFactory.createLineBorder(Color.black));
	    
	    
	    
        
	    

        
        
        

		JTabbedPane tabbedPane = new JTabbedPane();
//		ImageIcon icon = createImageIcon("images/middle.gif");

		tabbedPane.addTab("Control panel", null, firstPanel,
                "Control panel");
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
		tabbedPane.addTab("Parameters", null, createParamPanel(),
                "Parameters");
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
//		tabbedPane.addTab("Output", null, outputPanel, 
//                "Output");
//		tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);
		add(tabbedPane);
        
        
//        EpidemicCurvesPanel epidemicCurves = new EpidemicCurvesPanel();
//		add(epidemicCurves);

        setTitle("FMD modelling");
//        setPreferredSize(new Dimension(450, 600));
        setSize(new Dimension(650, 600));
//        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	
	
	
	
	/**
	 * Creates JPanel with parameters
	 * @return 
	 */
	public JPanel createParamPanel() {
        JPanel paramPanel = new JPanel(); // the top-level panel that holds subpanels
        paramPanel.setLayout(new BoxLayout(paramPanel, BoxLayout.Y_AXIS));
        

        // Initial conditions
//        SpringLayout layout = new SpringLayout();
//        JPanel initialPanel = new JPanel(layout);
        JPanel initialPanel = new JPanel(new GridLayout(0,2));
        initialPanel.setBorder(BorderFactory.createTitledBorder("Initial conditions"));

	    Integer[] options = { 5 };
	    JComboBox<Integer> numberOfIndexCasesComboBox = new JComboBox<Integer>(options);
	    numberOfIndexCasesComboBox.setEditable(true);
	    numberOfIndexCasesComboBox.setSelectedIndex(0);
	    EpidemicModelling.NUMBER_OF_INDEX_CASES = options[0];
	    numberOfIndexCasesComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox<Integer> cb = (JComboBox<Integer>) e.getSource();
				EpidemicModelling.NUMBER_OF_INDEX_CASES = (Integer) cb.getSelectedItem();
			}
		});
	    JLabel numberOfIndexCasesLabel = new JLabel("Number of index cases:");
	    initialPanel.add(numberOfIndexCasesLabel);
	    initialPanel.add(numberOfIndexCasesComboBox);

//	    layout.putConstraint(SpringLayout.NORTH, numberOfIndexCasesLabel, 0, SpringLayout.NORTH, numberOfIndexCasesComboBox);
//	    layout.putConstraint(SpringLayout.EAST, numberOfIndexCasesLabel, 0, SpringLayout.WEST, numberOfIndexCasesComboBox);

	    options = new Integer[] { 3 };
	    JComboBox<Integer> durationOfInitialInfectionsComboBox = new JComboBox<Integer>(options);
	    durationOfInitialInfectionsComboBox.setEditable(true);
	    durationOfInitialInfectionsComboBox.setSelectedIndex(0);
	    EpidemicModelling.DURATION_OF_INITIAL_INFECTIONS = options[0];
	    durationOfInitialInfectionsComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox<Integer> cb = (JComboBox<Integer>) e.getSource();
				EpidemicModelling.DURATION_OF_INITIAL_INFECTIONS = (Integer) cb.getSelectedItem();
			}
		});
	    JLabel durationOfInitialInfectionsLabel = new JLabel("Duration of initial infections:");
	    initialPanel.add(durationOfInitialInfectionsLabel);
	    initialPanel.add(durationOfInitialInfectionsComboBox); 
	    
//	    layout.putConstraint(SpringLayout.NORTH, durationOfInitialInfectionsLabel, 0, SpringLayout.NORTH, durationOfInitialInfectionsComboBox);
//	    layout.putConstraint(SpringLayout.EAST, durationOfInitialInfectionsLabel, 0, SpringLayout.WEST, durationOfInitialInfectionsComboBox);
	    
	    paramPanel.add(initialPanel);
	    

	    
        // Disease-specific parameters
        
        JPanel diseaseSpecificPanel = new JPanel(new GridLayout(0,2));
        diseaseSpecificPanel.setBorder(BorderFactory.createTitledBorder("Disease-specific"));
	       	
	    options = new Integer[] { 5 };
	    JComboBox<Integer> latentPeriodComboBox = new JComboBox<Integer>(options);
	    latentPeriodComboBox.setEditable(true);
	    latentPeriodComboBox.setSelectedIndex(0);
	    EpidemicModelling.LATENT_PERIOD = options[0];
	    latentPeriodComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox<Integer> cb = (JComboBox<Integer>) e.getSource();
				EpidemicModelling.LATENT_PERIOD = (Integer) cb.getSelectedItem();
				
			}
		});
	    diseaseSpecificPanel.add(new JLabel("Incubation preiod:"));
	    diseaseSpecificPanel.add(latentPeriodComboBox);
	    
	    options = new Integer[] { 3 };
	    JComboBox<Integer> detectionTimeComboBox = new JComboBox<Integer>(options);
	    detectionTimeComboBox.setEditable(true);
	    detectionTimeComboBox.setSelectedIndex(0);
	    EpidemicModelling.DETECTION_TIME = options[0];
	    detectionTimeComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox<Integer> cb = (JComboBox<Integer>) e.getSource();
				EpidemicModelling.DETECTION_TIME = (Integer) cb.getSelectedItem();
			}
		});
	    diseaseSpecificPanel.add(new JLabel("Detection time (by clinical signs):"));
	    diseaseSpecificPanel.add(detectionTimeComboBox);
	    
	    paramPanel.add(diseaseSpecificPanel);
	    
	    
        // Transmission-specific parameters
        
	    JPanel transmissionSpecificPanel = new JPanel(new GridLayout(0,2));
	    transmissionSpecificPanel.setBorder(BorderFactory.createTitledBorder("Transmission-specific (per animal)"));
        
        Double[] dblOptions = { 0.065 };
	    JComboBox<Double> removalRateComboBox = new JComboBox<Double>(dblOptions);
	    removalRateComboBox.setEditable(true);
	    removalRateComboBox.setSelectedIndex(0);
	    EpidemicModelling.BETA = dblOptions[0];
	    removalRateComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox<Double> cb = (JComboBox<Double>) e.getSource();
				EpidemicModelling.BETA = (Double) cb.getSelectedItem();
			}
		});
	    transmissionSpecificPanel.add(new JLabel("Beta (probability of local infection):"));
	    transmissionSpecificPanel.add(removalRateComboBox);

	    dblOptions = new Double[] { 0.02 };
	    JComboBox<Double> directMovementComboBox = new JComboBox<Double>(dblOptions);
	    directMovementComboBox.setEditable(true);
	    directMovementComboBox.setSelectedIndex(0);
	    EpidemicModelling.DIRECT_MOVEMENT = dblOptions[0];
	    directMovementComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox<Double> cb = (JComboBox<Double>) e.getSource();
				EpidemicModelling.DIRECT_MOVEMENT = (Double) cb.getSelectedItem();
			}
		});
	    transmissionSpecificPanel.add(new JLabel("Probability of infection via direct movement:"));
	    transmissionSpecificPanel.add(directMovementComboBox); 
	    
	    dblOptions = new Double[] { 0.004 };
	    JComboBox<Double> offMarketMovementComboBox = new JComboBox<Double>(dblOptions);
	    offMarketMovementComboBox.setEditable(true);
	    offMarketMovementComboBox.setSelectedIndex(0);
	    EpidemicModelling.OFF_MARKET_MOVEMENT = dblOptions[0];
	    offMarketMovementComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox<Double> cb = (JComboBox<Double>) e.getSource();
				EpidemicModelling.OFF_MARKET_MOVEMENT = (Double) cb.getSelectedItem();
			}
		});
	    transmissionSpecificPanel.add(new JLabel("Probability of infection via off-market movement:"));
	    transmissionSpecificPanel.add(offMarketMovementComboBox);  
	    
//	    dblOptions = new Double[] { 1.0, 0.065 };
//	    JComboBox localSpreadComboBox = new JComboBox(dblOptions);
//	    localSpreadComboBox.setEditable(true);
//	    localSpreadComboBox.setSelectedIndex(0);
//	    EpidemicModelling.LOCAL_SPREAD = dblOptions[0];
//	    localSpreadComboBox.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				JComboBox cb = (JComboBox) e.getSource();
//				EpidemicModelling.LOCAL_SPREAD = (Double) cb.getSelectedItem();
//			}
//		});
//	    transmissionSpecificPanel.add(new JLabel("Local spread:"));
//	    transmissionSpecificPanel.add(localSpreadComboBox);  
	    
	    paramPanel.add(transmissionSpecificPanel);
	    
	    
        // Control-specific parameters
        
	    JPanel controlSpecificPanel = new JPanel(new GridLayout(0,2));
	    controlSpecificPanel.setBorder(BorderFactory.createTitledBorder("Control-specific"));

	    options = new Integer[] { 0 };
	    JComboBox<Integer> controlMeasuresDelayComboBox = new JComboBox<Integer>(options);
	    controlMeasuresDelayComboBox.setEditable(true);
	    controlMeasuresDelayComboBox.setSelectedIndex(0);
	    EpidemicModelling.CONTROL_MEASURES_DELAY = options[0];
	    controlMeasuresDelayComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox<Integer> cb = (JComboBox<Integer>) e.getSource();
				EpidemicModelling.CONTROL_MEASURES_DELAY = (Integer) cb.getSelectedItem();
			}
		});
	    controlSpecificPanel.add(new JLabel("Control measures delay:"));
	    controlSpecificPanel.add(controlMeasuresDelayComboBox);
	    
	    options = new Integer[] { 20 };
	    JComboBox<Integer> movementBanDelayComboBox = new JComboBox<Integer>(options);
	    movementBanDelayComboBox.setEditable(true);
	    movementBanDelayComboBox.setSelectedIndex(0);
	    EpidemicModelling.MOVEMENT_BAN_DELAY = options[0];
	    movementBanDelayComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox<Integer> cb = (JComboBox<Integer>) e.getSource();
				EpidemicModelling.MOVEMENT_BAN_DELAY = (Integer) cb.getSelectedItem();
			}
		});
	    controlSpecificPanel.add(new JLabel("Movement ban delay:"));
	    controlSpecificPanel.add(movementBanDelayComboBox);
	    

	    options = new Integer[] { 13 }; // Sibylle: no standstill
	    JComboBox<Integer> movementRestrictionsComboBox = new JComboBox<Integer>(options);
	    movementRestrictionsComboBox.setEditable(true);
	    movementRestrictionsComboBox.setSelectedIndex(0);
	    EpidemicModelling.MOVEMENT_RESTRICTIONS = options[0];
	    movementRestrictionsComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox<Integer> cb = (JComboBox<Integer>) e.getSource();
				EpidemicModelling.MOVEMENT_RESTRICTIONS = (Integer) cb.getSelectedItem();
			}
		});
	    controlSpecificPanel.add(new JLabel("Standstill period:"));
	    controlSpecificPanel.add(movementRestrictionsComboBox);
	    
	    options = new Integer[] { 21 };
	    JComboBox<Integer> contactTracingDepthComboBox = new JComboBox<Integer>(options);
	    contactTracingDepthComboBox.setEditable(true);
	    contactTracingDepthComboBox.setSelectedIndex(0);
	    EpidemicModelling.CONTACT_TRACING_DEPTH = options[0];
	    contactTracingDepthComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox<Integer> cb = (JComboBox<Integer>) e.getSource();
				EpidemicModelling.CONTACT_TRACING_DEPTH = (Integer) cb.getSelectedItem();
			}
		});
	    controlSpecificPanel.add(new JLabel("Contact tracing depth:"));
	    controlSpecificPanel.add(contactTracingDepthComboBox);
	    
	    options = new Integer[] { 1 };
	    JComboBox<Integer> delayLocalMovementsComboBox = new JComboBox<Integer>(options);
	    delayLocalMovementsComboBox.setEditable(true);
	    delayLocalMovementsComboBox.setSelectedIndex(0);
	    EpidemicModelling.DELAY_LOCAL_CONTACTS = options[0];
	    delayLocalMovementsComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox<Integer> cb = (JComboBox<Integer>) e.getSource();
				EpidemicModelling.DELAY_LOCAL_CONTACTS = (Integer) cb.getSelectedItem();
			}
		});
	    controlSpecificPanel.add(new JLabel("Tracing delay for local contacts:"));
	    controlSpecificPanel.add(delayLocalMovementsComboBox);
	    
	    options = new Integer[] { 2 };
	    JComboBox<Integer> delayDirectMovementsComboBox = new JComboBox<Integer>(options);
	    delayDirectMovementsComboBox.setEditable(true);
	    delayDirectMovementsComboBox.setSelectedIndex(0);
	    EpidemicModelling.DELAY_DIRECT_MOVEMENTS = options[0];
	    delayDirectMovementsComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox<Integer> cb = (JComboBox<Integer>) e.getSource();
				EpidemicModelling.DELAY_DIRECT_MOVEMENTS = (Integer) cb.getSelectedItem();
			}
		});
	    controlSpecificPanel.add(new JLabel("Tracing delay for direct movements:"));
	    controlSpecificPanel.add(delayDirectMovementsComboBox);
	    
	    options = new Integer[] { 4 };
	    JComboBox<Integer> delayMarketMovementsComboBox = new JComboBox<Integer>(options);
	    delayMarketMovementsComboBox.setEditable(true);
	    delayMarketMovementsComboBox.setSelectedIndex(0);
	    EpidemicModelling.DELAY_MARKET_MOVEMENTS = options[0];
	    delayMarketMovementsComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox<Integer> cb = (JComboBox<Integer>) e.getSource();
				EpidemicModelling.DELAY_MARKET_MOVEMENTS = (Integer) cb.getSelectedItem();
			}
		});
	    controlSpecificPanel.add(new JLabel("Tracing delay for movements via markets:"));
	    controlSpecificPanel.add(delayMarketMovementsComboBox);
	    

	    paramPanel.add(controlSpecificPanel);
	    
	    //
	    
		return paramPanel;
	}
	
	@Deprecated
	public JPanel createTablePanel() {
        // TABLE
        String[][] tableContent = {{"1", "2", "3"}, {"4", "5", "6"}};
        String[] rowNames = {"first", "second", "third"};
        JTable table = new JTable(tableContent, rowNames);
        

        JPanel tablePanel = new JPanel( new GridLayout(0,1));
        tablePanel.add(new JLabel("Results:"));
        tablePanel.add(table);
        
        return tablePanel;
	}
}
