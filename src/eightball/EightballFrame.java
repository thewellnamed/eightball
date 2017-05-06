package eightball;

import java.awt.BorderLayout;
import javax.swing.JFrame;

/**
 * Main Game UI
 */
@SuppressWarnings("serial")
public class EightballFrame extends JFrame 
{
	private Game game;
		
	/**
	 * Constructor
	 */
	public EightballFrame() {
		setSize(900, 545);
		setResizable(false);
		setLayout(new BorderLayout());
		
		game = new Game();
		add(game.getTable(), BorderLayout.CENTER);
	}
}
