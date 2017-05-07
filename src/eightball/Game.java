package eightball;

import eightball.events.*;

/**
 * Track Eightball game state, manage table state
 */
public class Game {
	private BilliardsTable table;
	
	/**
	 * Create a new game
	 */
	public Game() {
		table = new BilliardsTable();
		table.addEventListener(TableEventType.BALL_CAPTURED, e -> onBallCaptured(e));
		table.addEventListener(TableEventType.SHOT_BEGIN, e -> onShotBegin(e));
		table.addEventListener(TableEventType.SHOT_ENDED, e -> onShotEnd(e));
	}
	
	/**
	 * Get Canvas for Game
	 * @return Canvas
	 */
	public BilliardsTable getTable() {
		return table;
	}
	
	public void newGame(int players) {
		table.reset();
		table.initialize();
	}
	
	private void onShotBegin(TableEvent e) {
		System.out.println("Shot begin");
	}
	
	private void onShotEnd(TableEvent e) {
		System.out.println("Shot end");
	}
	
	private void onBallCaptured(TableEvent e) {
		System.out.printf("ball captured: %s\n", e.ball);
	}
}
