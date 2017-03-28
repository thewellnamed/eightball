package game;

import java.awt.Dimension;
import java.awt.geom.Point2D;

import javax.vecmath.Vector2d;

import engine.*;

/**
 * Track Eightball game state, manage canvas and player actions
 */
public class Game {
	private Canvas canvas;
	
	/**
	 * Create a new game
	 */
	public Game() {
		initializeCanvas();
	}
	
	/*
	 * Create Canvas
	 */
	private void initializeCanvas() {
		canvas = new Canvas();
		canvas.setSize(new Dimension(800, 400));
		canvas.setAnimationDelay(30);
		
		initializeTable();
	}
	
	/*
	 * Place billiards in initial states
	 */
	private void initializeTable() {
		int baseX = 560;
		int baseY = 150;
		int[] offsetX = { 0, 31, 31, 62, 62, 62, 93, 93, 93, 93, 124, 124, 124, 124, 124};
		int[] offsetY = { 60, 44, 75, 29, 60, 91, 14, 45, 76, 107, 0, 31, 62, 93, 124 };
		
		for (int i = 0; i < 15; i++) {
			CanvasObject s = new BilliardBall(i + 1);
			s.setLocation(new Point2D.Double(baseX + offsetX[i], baseY + offsetY[i]));
			canvas.add(s);
		}
		
		CanvasObject cue = new BilliardBall(0);
		cue.setLocation(new Point2D.Double(150, 205));
		cue.setMovementVector(new Vector2d(32, 0));
		canvas.add(cue);
	}
	
	/**
	 * Get Canvas for Game
	 * @return Canvas
	 */
	public Canvas getCanvas() {
		return canvas;
	}
	
	public void start() {
		canvas.start();
	}
	
	public void stop() {
		canvas.stop();
	}
	
	public void clear() {
		canvas.clear();
		initializeTable();
	}
}
