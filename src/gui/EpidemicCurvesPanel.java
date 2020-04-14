package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;

import javax.swing.JPanel;

public class EpidemicCurvesPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	ArrayList<Integer> data[];
	private int numOfSeries;
	
	private Integer horisontal = null, vertical = null;
	
	private int WIDTH;
	private int HEIGHT;
	private int LEFT_MARGIN;

	private int V_OFFSET;
	private int H_OFFSET;
	
	private double V_SCALE = 1.0;
	private double H_SCALE = 1.0;
	
	Color[] colors = {Color.RED, Color.GREEN, Color.BLACK, Color.BLUE, Color.YELLOW};
	
	public EpidemicCurvesPanel(int width, int height, int margin, int vOffset, int hOffset, int numSer) {
		numOfSeries = numSer;
		data = (ArrayList<Integer>[]) new ArrayList[numOfSeries];
		
		for (int i = 0; i < numSer; i++) {
			data[i] = new ArrayList<Integer>();
//			data[i].add(1);
		}
		
		WIDTH = width;
		HEIGHT = height;
		LEFT_MARGIN = margin;
		V_OFFSET = vOffset;
		H_OFFSET = hOffset;

//		setSize(LEFT_MARGIN + WIDTH, HEIGHT);
		setPreferredSize(new Dimension(LEFT_MARGIN + WIDTH + 4 * H_OFFSET, HEIGHT + 2 * V_OFFSET));
	}
	
	@Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        
        g2d.setColor(Color.WHITE);        
        g2d.fillRect(LEFT_MARGIN, 0, WIDTH, HEIGHT);
        g2d.setColor(Color.BLACK);

    	int max = 1; // if all items in data are 0, we set max to 1.
        for (int series = 0; series < numOfSeries; series++) {    	
	    	for (Integer i : data[series]) // TODO: Exception in thread "AWT-EventQueue-0" java.util.ConcurrentModificationException
	    		max = Math.max(max, i);
        }
        max *= V_SCALE;
        
        g2d.drawLine(LEFT_MARGIN, 0, LEFT_MARGIN, HEIGHT); // y
        char[] chars = String.valueOf(max).toCharArray();
        g2d.drawChars(chars, 0, chars.length, LEFT_MARGIN - chars.length * H_OFFSET, V_OFFSET);
        chars = "0".toCharArray();
        g2d.drawChars(chars, 0, chars.length, LEFT_MARGIN - chars.length * H_OFFSET, HEIGHT + V_OFFSET);

        
        int step;
        if (data[0].size() <= 1)
        	step = WIDTH;
        else
        	step = WIDTH / (data[0].size() - 1);
        
        g2d.drawLine(LEFT_MARGIN, HEIGHT, LEFT_MARGIN + WIDTH, HEIGHT); // x    
        chars = String.valueOf((int) ((data[0].size() - 1) * H_SCALE)).toCharArray();
        g2d.drawChars(chars, 0, chars.length, LEFT_MARGIN - chars.length / 2 * H_OFFSET + (data[0].size() - 1) * step, HEIGHT + V_OFFSET);
    	
    	g2d.setColor(Color.BLUE);
    	if (vertical != null) { // line for day 14
	        chars = vertical.toString().toCharArray();
    		vertical = (int) (vertical / H_SCALE);
	        g2d.drawLine(LEFT_MARGIN + vertical * step, 0, LEFT_MARGIN + vertical * step, HEIGHT);
	        g2d.drawChars(chars, 0, chars.length, LEFT_MARGIN - chars.length / 2 * H_OFFSET + vertical * step, HEIGHT + V_OFFSET);
    	}
        
        if (horisontal != null) { // line for 100
	        chars = horisontal.toString().toCharArray();
	        horisontal = (int) (horisontal /  V_SCALE);
	        g2d.drawLine(LEFT_MARGIN, HEIGHT * (max - horisontal) / max, LEFT_MARGIN + WIDTH, HEIGHT * (max - horisontal) / max);
	        g2d.drawChars(chars, 0, chars.length, LEFT_MARGIN - chars.length * H_OFFSET, HEIGHT * (max - horisontal) / max + V_OFFSET / 2);
        }
        
        // SERIES
        for (int series = 0; series < numOfSeries; series++) {  	
	    	g2d.setColor(colors[series]);
	    	int prev = 0;
	    	if (data[series].size() > 0)
	    		prev = (int) (HEIGHT * data[series].get(0) * V_SCALE / max);
	        for (int i = 1; i < data[series].size(); i++) { // we start with 1 
	        	int cur = (int) (HEIGHT * data[series].get(i) * V_SCALE / max);
	        	g2d.drawLine(LEFT_MARGIN + (i-1) * step, HEIGHT - prev, LEFT_MARGIN + i * step, HEIGHT - cur);
	        	prev = cur;
	        	
	        	// TICK
	        	g2d.setColor(Color.BLACK);
	        	g2d.drawLine(LEFT_MARGIN + i * step, HEIGHT - V_OFFSET / 2, LEFT_MARGIN + i * step, HEIGHT);
	        	g2d.setColor(colors[series]);
	        }
        }    
//        setBorder(BorderFactory.createLineBorder(Color.black));
    }
	
	public void setScale(double h, double v) {
		H_SCALE = h;
		V_SCALE = v;
	}
	
	public void updateX(int n, int value) {
		data[n].add(value);
//		repaint();
	}
	
	public void addHorisontalLine(int x) {
		horisontal = x;
	}
	
	public void addVerticalLine(int x) {
		vertical = x;
	}

	public void erase(int i) {
		data[i].clear();
	}
}
