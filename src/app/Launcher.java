package app;

import javax.swing.JFrame;

public class Launcher {
	/**
	 * Launch application
	 * @param args Unused
	 */
	public static void main(String[] args) {		
		JFrame frame = new EightballFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}
