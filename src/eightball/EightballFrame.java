package eightball;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import eightball.events.GameEvent;
import eightball.events.GameEventType;

/**
 * Main Eightball Game JFrame and UI
 */
@SuppressWarnings("serial")
public class EightballFrame extends JFrame 
{
	private Game game;
	private JLabel messageLabel;
	private JLabel infoLabel;
	private JLayeredPane layeredPane;
	private JPanel controlPanel;
	private JPanel statusPanel;
	private boolean showingControlPanel;
		
	/**
	 * Constructor
	 */
	public EightballFrame() {
		super("Eight Ball Blitz");
		setSize(900, 570);
		setResizable(false);
		setLayout(new BorderLayout());
		createStatusBar();
		
		layeredPane = new JLayeredPane();
		layeredPane.setPreferredSize(new Dimension(900, 525));
	
		game = new Game();
		game.addEventListener(GameEventType.REQUEST_PAUSE, e -> pause(false));
		game.addEventListener(GameEventType.STATUS_MESSAGE, e-> updateStatus(e));
		game.addEventListener(GameEventType.INFO_MESSAGE, e-> updateStatus(e));
		game.addEventListener(GameEventType.GAME_OVER, e -> gameOver(e));
		
		BilliardsTable table = game.getTable();
		Dimension tableSize = table.getPreferredSize();
		table.setBounds(0, 0, tableSize.width, tableSize.height);
		
		layeredPane.add(table, new Integer(1));
		
		add(layeredPane, BorderLayout.CENTER);		
		pause(false);
	}
	
	/*
	 * Create the status bar
	 */
	private void createStatusBar() {		
		statusPanel = new JPanel();
		statusPanel.setBorder(BorderFactory.createEmptyBorder(0,15,4,15));
		statusPanel.setBackground(new Color(14, 23, 46));
		statusPanel.setPreferredSize(new Dimension(getWidth(), 28));
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.LINE_AXIS));
		
		messageLabel = new JLabel("");
		messageLabel.setPreferredSize(new Dimension(600, 24));
		messageLabel.setHorizontalAlignment(SwingConstants.LEFT);
		messageLabel.setForeground(Color.WHITE);
		statusPanel.add(messageLabel);
		
		infoLabel = new JLabel("");
		infoLabel.setPreferredSize(new Dimension(280, 24));
		infoLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		infoLabel.setForeground(Color.WHITE);
		statusPanel.add(infoLabel);
		
		add(statusPanel, BorderLayout.SOUTH);
	}	
	
	/*
	 * Create a floating control panel with context-dependent options
	 */
	private JPanel createControlPanel() {
		JPanel panel = new JPanel();
		boolean inProgress = game.inProgress();
		boolean haveWinner = game.haveWinner();
		
		panel.setLayout(new GridLayout(4, 1));
		panel.setBounds(300, 150, 300, 160);
		
		JLabel label = new JLabel("Eight Ball Blitz!");
		label.setHorizontalAlignment(JLabel.CENTER);
		
		JButton exitButton = new JButton("Exit Game");
		exitButton.addActionListener(ae -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));
		
		if (haveWinner) {
			label.setText("Game Over!");
			
			JButton newButton = new JButton("New Game");
			newButton.addActionListener(ae -> reset());
			
			panel.setLayout(new GridLayout(2, 1));
			panel.setBounds(300,  150, 300, 120);
			
			panel.add(label);
			panel.add(newButton);			
		} else if (inProgress) {
			JButton continueButton = new JButton("Continue");
			continueButton.addActionListener(ae -> unpause());
			
			JButton newButton = new JButton("New Game");
			newButton.addActionListener(ae -> reset());
			
			panel.add(label);
			panel.add(continueButton);
			panel.add(newButton);
			panel.add(exitButton);
		} else {
			JButton onePlayerButton = new JButton("One Player");
			onePlayerButton.addActionListener(ae -> initializeWithPlayers(1));
			
			JButton twoPlayersButton = new JButton("Two Players");
			twoPlayersButton.addActionListener(ae -> initializeWithPlayers(2));
			
			panel.add(label);
			panel.add(onePlayerButton);
			panel.add(twoPlayersButton);
			panel.add(exitButton);
		}
		
		return panel;
	}
	
	/*
	 * Reset game
	 */
	private void reset() {
		game.reset();
		pause(true);		
	}
	
	/*
	 * Begin game with specified players
	 */
	private void initializeWithPlayers(int players) {
		game.begin(players);
		unpause();
	}
	
	/*
	 * GameEvent callback for status messages
	 */
	private void updateStatus(GameEvent e) {
		if (e.type == GameEventType.STATUS_MESSAGE) {
			messageLabel.setText(e.text);
		} else {
			infoLabel.setText(e.text);
		}
	}
	
	/*
	 * GameEvent callback for end of game
	 */
	private void gameOver(GameEvent e) {
		// force control panel update
		pause(true);
	}
	
	/*
	 * Pause game
	 * @param forceReset -- if true we will re-create control panel regardless of current state
	 */
	private void pause(boolean forceReset) {
		if (!showingControlPanel || forceReset) {
			if (forceReset && controlPanel != null) {
				layeredPane.remove(controlPanel);
			}
			
			game.pause();
			statusPanel.setBackground(new Color(9, 12, 22));
			controlPanel = createControlPanel();
			layeredPane.add(controlPanel, new Integer(2));
			showingControlPanel = true;
		}
	}
	
	/*
	 * Unpause game
	 */
	private void unpause() {
		if (showingControlPanel) {
			layeredPane.remove(controlPanel);
			statusPanel.setBackground(new Color(14, 23, 46));
			showingControlPanel = false;
			controlPanel = null;
			game.unpause();
		}
	}
}
