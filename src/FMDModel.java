import gui.MainFrame;

import javax.swing.SwingUtilities;

public class FMDModel {
	public static void main(final String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);
            }
        });
	}
}