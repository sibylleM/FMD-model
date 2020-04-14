package gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import methods.SpatialProbabilityMatrix;

public class OptionsDialog extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static double sDISTANCE_THRESHOLD = 10000.0;
	private static double sDELTA = 1471.0;
	private static double sB = 2.72;
	private static double sCLOSE_DISTANCE = 500;
	private static double sSURVEILLANCE_DISTANCE = 10000;
	
	private static int sKernel = 0;
	
	static JCheckBox autoDef = new JCheckBox("Auto", false);
	
	public OptionsDialog() {
		setLayout(new FlowLayout());
	    
	    String[] presetToChoose = { "Chis Ster et al.", "Tildesley et al.", "Green et al." };
	    final JComboBox<String> presetComboBox = new JComboBox<String>(presetToChoose);
	    presetComboBox.setSelectedIndex(sKernel);
	    // SELECT
	    SpatialProbabilityMatrix.DISTANCE_THRESHOLD = sDISTANCE_THRESHOLD;
		SpatialProbabilityMatrix.DELTA = sDELTA;
		SpatialProbabilityMatrix.B = sB;
		SpatialProbabilityMatrix.B = sB;
		SpatialProbabilityMatrix.CLOSE_DISTANCE = sCLOSE_DISTANCE;
		SpatialProbabilityMatrix.SURVEILLANCE_DISTANCE = sSURVEILLANCE_DISTANCE;
		final JLabel formulaLabel = new JLabel(getFormula(sKernel), JLabel.CENTER);
		

		final EpidemicCurvesPanel distanceKernel = new EpidemicCurvesPanel(400, 200, 50, 11, 8, 1);
	    distanceKernel.addVerticalLine((int) sCLOSE_DISTANCE);
	    distanceKernel.setScale(100, 0.001);
	    redraw(distanceKernel);
	    
		Double[] offsetChoose = { sDELTA };
	    final JComboBox<Double> offsetComboBox = new JComboBox<Double>(offsetChoose);
	    offsetComboBox.setSelectedIndex(0);
	    offsetComboBox.setEditable(true);
	    offsetComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox<Double> cb = (JComboBox<Double>) e.getSource();
				SpatialProbabilityMatrix.DELTA = sDELTA = (Double) cb.getSelectedItem();	
				redraw(distanceKernel);	
			}
		});
	    
//	    JTextField offsetField = new JTextField("" + sDELTA);
//	    offsetField.setEditable(true);
//	    offsetField.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				JTextField textField = (JTextField) e.getSource();
//				try {
//					SpatialProbabilityMatrix.DELTA = sDELTA = Double.valueOf(textField.getText());
//				} catch (NumberFormatException ex) {
//					JOptionPane.showMessageDialog(null, "Invalid number!");
//				}	
//				redraw(distanceKernel);	
//			}
//			
//		});
	    
	    Double[] powerToChoose = { sB };
	    final JComboBox<Double> powerComboBox = new JComboBox<Double>(powerToChoose);
	    powerComboBox.setSelectedIndex(0);
	    powerComboBox.setEditable(true);
	    powerComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox<Double> cb = (JComboBox<Double>) e.getSource();
				SpatialProbabilityMatrix.B = sB = (Double) cb.getSelectedItem();
				redraw(distanceKernel);
			}
		});    
	    
	    Double[] thresholdChoose = { sDISTANCE_THRESHOLD };
	    final JComboBox<Double> thresholdComboBox = new JComboBox<Double>(thresholdChoose);
	    thresholdComboBox.setSelectedIndex(0);
	    thresholdComboBox.setEditable(true);
	    thresholdComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox<Double> cb = (JComboBox<Double>) e.getSource();
				SpatialProbabilityMatrix.DISTANCE_THRESHOLD = sDISTANCE_THRESHOLD = (Double) cb.getSelectedItem();
				redraw(distanceKernel);
			}
		});    
	    
	    Double[] closeDistanceChoose = { sCLOSE_DISTANCE };
	    final JComboBox<Double> closeComboBox = new JComboBox<Double>(closeDistanceChoose);
	    closeComboBox.setSelectedIndex(0);
	    closeComboBox.setEditable(true);
	    closeComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox<Double> cb = (JComboBox<Double>) e.getSource();
				SpatialProbabilityMatrix.CLOSE_DISTANCE = sCLOSE_DISTANCE = (Double) cb.getSelectedItem();
				redraw(distanceKernel);
			}
		}); 
	    
	    Double[] surveillanceDistanceChoose = { sSURVEILLANCE_DISTANCE };
	    final JComboBox<Double> surveillanceComboBox = new JComboBox<Double>(surveillanceDistanceChoose);
	    surveillanceComboBox.setSelectedIndex(0);
	    surveillanceComboBox.setEditable(true);
	    surveillanceComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox<Double> cb = (JComboBox<Double>) e.getSource();
				SpatialProbabilityMatrix.SURVEILLANCE_DISTANCE = sSURVEILLANCE_DISTANCE = (Double) cb.getSelectedItem();
				redraw(distanceKernel);
			}
		});
	    
	    
	    
	    final JButton setDefButton = new JButton("Set defaults");
	    setDefButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) { 
            	switch (sKernel) {
    			case 0:
    				offsetComboBox.setSelectedItem(1471.0); 
    				powerComboBox.setSelectedItem(2.72); 
                	thresholdComboBox.setSelectedItem(10000.0);
    				break;
    			case 1:
    				offsetComboBox.setSelectedItem(710.0);  
    				powerComboBox.setSelectedItem(1.66); 
                	thresholdComboBox.setSelectedItem(10000.0);
    				break;
    			case 2: 
    				powerComboBox.setSelectedItem(0.5);
                	thresholdComboBox.setSelectedItem(10000.0);
    				break;
    			default:
    				return;
    		}
            }
        });
	    

		
	    presetComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox<String> cb = (JComboBox<String>) e.getSource();
				SpatialProbabilityMatrix.KERNEL = sKernel = cb.getSelectedIndex();
				formulaLabel.setText(getFormula(sKernel));
				if (autoDef.isSelected())
					setDefButton.doClick(0);
				redraw(distanceKernel);
			}
		}); 
        
        

		JPanel paramsPanel = new JPanel();
		paramsPanel.setLayout(new GridLayout(0,2));
        
		paramsPanel.add(new JLabel("Presets:"));
		paramsPanel.add(presetComboBox);
		
		paramsPanel.add(new JLabel("Formula:"));
		paramsPanel.add(formulaLabel);

		
		autoDef.setText("Automatically");
		paramsPanel.add(autoDef);
		setDefButton.setPreferredSize(new Dimension(210, 10));
		paramsPanel.add(setDefButton); 

		
		paramsPanel.add(new JLabel("Offset (DELTA):"));
		paramsPanel.add(offsetComboBox);  
//		paramsPanel.add(offsetField);       

		paramsPanel.add(new JLabel("Power (B):"));
		paramsPanel. add(powerComboBox);

		paramsPanel.add(new JLabel("Threshold:"));
		paramsPanel.add(thresholdComboBox);

		paramsPanel.add(new JLabel("Close distance:"));
		paramsPanel.add(closeComboBox); 

		paramsPanel.add(new JLabel("Surveillance distance:"));
		paramsPanel.add(surveillanceComboBox); 
		
		add(paramsPanel);

        add(distanceKernel);
        
        setTitle("Options: distance kernel");
        setSize(500, 550);
        setLocationRelativeTo(null);
//        pack();
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        
        
       
	}
	
	private void redraw(EpidemicCurvesPanel graph) {
		graph.erase(0);
		for (Double val: SpatialProbabilityMatrix.listDistanceKernel(100, 10000)) {
        	graph.updateX(0, (int) (val * 1000));
        }
		graph.addVerticalLine((int) sCLOSE_DISTANCE);
		graph.repaint();
	}
	
	private String getFormula(int krnl) {
		switch (krnl) {
			case 0:
				return "K(d_ij) = (1 + d_ij / DELTA) ^ -B";
			case 1:
				return "K(d_ij) = (DELTA / d_ij) ^ B";
			case 2:
				return "K(d_ij) = exp(-B * d_ij / 1000)";
			default:
				return "---";
		}
	}
}
