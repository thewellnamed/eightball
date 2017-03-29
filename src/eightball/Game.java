package eightball;

import java.awt.geom.Point2D;
import java.io.IOException;

import javax.vecmath.Vector2d;

import canvas.*;

/**
 * Track Eightball game state, manage canvas and player actions
 */
public class Game {
	private BilliardsTable table;
	
	/**
	 * Create a new game
	 * @throws IOException 
	 */
	public Game() {
		table = new BilliardsTable();
		table.setAnimationDelay(30);
		
		initializeTable();
	}
	
	/*
	 * Place billiards in initial states
	 */
	private void initializeTable() {
		int baseX = 560;
		int baseY = 200;
		int[] offsetX = { 0, 26, 26, 52, 52, 52, 78, 78, 78, 78, 104, 104, 104, 104, 104};
		int[] offsetY = { 50, 38, 64, 25, 51, 76, 12, 38, 64, 90, 0, 26, 52, 78, 104 };
		
		for (int i = 0; i < 15; i++) {
			CanvasObject s = new BilliardBall(i + 1);
			s.setLocation(new Point2D.Double(baseX + offsetX[i], baseY + offsetY[i]));
			table.add(s);
		}
		
		CanvasObject cue = new BilliardBall(0);
		cue.setLocation(new Point2D.Double(180, 260));
		cue.setMovementVector(new Vector2d(32, 0));
		table.add(cue);
	}
	
	/**
	 * Get Canvas for Game
	 * @return Canvas
	 */
	public BilliardsTable getTable() {
		return table;
	}
	
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
