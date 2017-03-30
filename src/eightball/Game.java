package eightball;

import java.awt.geom.Point2D;
import javax.vecmath.Vector2d;

import canvas.*;
import eightball.enums.*;

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
		initializeTable();
	}
	
	/*
	 * Place billiards in initial states
	 */
	private void initializeTable() {
		int baseX = 560;
		int baseY = 200;
		int[] offsetX = { 0, 0, 26, 26, 52, 52, 52, 78, 78, 78, 78, 104, 104, 104, 104, 104};
		int[] offsetY = { 0, 50, 38, 64, 25, 51, 76, 12, 38, 64, 90, 0, 26, 52, 78, 104 };
		
		for (int i = 1; i < 16; i++) {
			CanvasObject s = new BilliardBall(BallDefinition.valueOf(i));
			s.setLocation(new Point2D.Double(baseX + offsetX[i], baseY + offsetY[i]));
			table.add(s);
		}
		
		CanvasObject cue = new BilliardBall(BallDefinition.CUE);
		cue.setLocation(new Point2D.Double(180, 260));
		cue.setMovementVector(new Vector2d(32, -1));
		table.add(cue);
	}
	
	/**
	 * Get Canvas for Game
	 * @return Canvas
	 */
	public BilliardsTable getTable() {
		return table;
	}
	
	// These are going to go away...
	public void start() {
		table.start();
	}
	
	public void stop() {
		table.stop();
	}
	
	public void clear() {
		table.clear();
		initializeTable();
	}
}
