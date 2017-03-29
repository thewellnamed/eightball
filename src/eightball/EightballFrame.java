package eightball;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Main Game UI
 */
@SuppressWarnings("serial")
public class EightballFrame extends JFrame 
{
	private JPanel controlPanel;
	private Game game;
		
	/**
	 * Constructor
	 */
	public EightballFrame() {
		setSize(900, 625);
		setResizable(false);
		setLayout(new BorderLayout());
		
		createControlPanel();
		
		game = new Game();
		add(game.getTable(), BorderLayout.CENTER);
	}
	
	/*
	 * Create control panel
	 */
	private void createControlPanel() {
		controlPanel = new JPanel();
		controlPanel.setLayout(new GridLayout(2, 1));

		JPanel buttonPanel = new JPanel();
		JButton startButton = new JButton("Start");
		startButton.addActionListener(ae -> game.start());
		
		JButton stopButton = new JButton("Stop");
		stopButton.addActionListener(ae -> game.stop());
		
		JButton clearButton = new JButton("Clear");
		clearButton.addActionListener(ae -> game.clear());
		
		buttonPanel.add(startButton);
		buttonPanel.add(stopButton);
		buttonPanel.add(clearButton);
		
		controlPanel.add(buttonPanel);
		add(controlPanel, BorderLayout.SOUTH);
	}
}