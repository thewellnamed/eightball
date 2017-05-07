package eightball;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

/**
 * Main Game UI
 */
@SuppressWarnings("serial")
public class EightballFrame extends JFrame 
{
	private Game game;
	private JLabel statusLabel;
	private JLayeredPane layeredPane;
	private JPanel controlPanel;
		
	/**
	 * Constructor
	 */
	public EightballFrame() {
		setSize(900, 570);
		setResizable(false);
		setLayout(new BorderLayout());
		
		layeredPane = new JLayeredPane();
		layeredPane.setPreferredSize(new Dimension(900, 525));
	
		game = new Game();
		BilliardsTable table = game.getTable();
		Dimension tableSize = table.getPreferredSize();
		table.setBounds(0, 0, tableSize.width, tableSize.height);
		
		controlPanel = createControlPanel();
		layeredPane.add(table, new Integer(1));
		layeredPane.add(controlPanel, new Integer(2));
		
		add(layeredPane, BorderLayout.CENTER);
		createStatusBar();
	}
	
	private JPanel createControlPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(3, 1));
		panel.setBounds(300, 180, 300, 120);
		
		JLabel label = new JLabel("Select number of players");
		label.setHorizontalAlignment(JLabel.CENTER);
		
		JButton onePlayerButton = new JButton("One Player");
		onePlayerButton.addActionListener(ae -> initializeWithPlayers(1));
		
		JButton twoPlayersButton = new JButton("Two Players");
		twoPlayersButton.addActionListener(ae -> initializeWithPlayers(2));
		
		panel.add(label);
		panel.add(onePlayerButton);
		panel.add(twoPlayersButton);
		
		return panel;
	}
	
	private void createStatusBar() {		
		JPanel statusPanel = new JPanel();
		statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		statusPanel.setPreferredSize(new Dimension(getWidth(), 24));
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
		
		statusLabel = new JLabel("");
		statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
		statusPanel.add(statusLabel);
		add(statusPanel, BorderLayout.SOUTH);
	}
	
	private void initializeWithPlayers(int players) {
		game.newGame(players);
		layeredPane.remove(controlPanel);
	}
}
