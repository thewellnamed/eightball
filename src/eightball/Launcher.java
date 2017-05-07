package eightball;

import javax.swing.JFrame;

/**
 * Eightball launcher
 */
public class Launcher {
	/**
	 * Launch application
	 * @param args Unused
	 */
	public static void main(String[] args) {	
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
			   launch();
			}
		});
	}
	
	private static void launch() {
		JFrame frame = new EightballFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
