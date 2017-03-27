package game;

import java.awt.Dimension;
import java.awt.geom.Point2D;

import javax.vecmath.Vector2d;

import engine.*;

public class Game {
	private Canvas canvas;
	
	public Game() {
		initializeCanvas();
	}
	
	private void initializeCanvas() {
		canvas = new Canvas();
		canvas.setSize(new Dimension(800, 300));
		canvas.setAnimationDelay(30);
		
		initializeTable();
	}
	
	private void initializeTable() {
		int baseX = 640;
		int baseY = 125;
		int[] offsetX = { 0, 31, 31, 62, 62, 62, 93, 93, 93, 93, 124, 124, 124, 124, 124};
		int[] offsetY = { 60, 44, 75, 29, 60, 91, 14, 45, 76, 107, 0, 31, 62, 93, 124 };
		
		for (int i = 0; i < 15; i++) {
			CanvasObject s = new BilliardBall(i + 1);
			s.setLocation(new Point2D.Double(baseX + offsetX[i], baseY + offsetY[i]));
			canvas.add(s);
		}
		
		CanvasObject cue = new BilliardBall(0);
		cue.setLocation(new Point2D.Double(150, 185));
		cue.setMovementVector(new Vector2d(24, 0));
		canvas.add(cue);
	}
	
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
	}
}
