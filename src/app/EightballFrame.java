package app;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.geom.Point2D;

import javax.vecmath.Vector2d;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import engine.*;

/**
 * Frame containing a SpriteCanvas
 * @author Matthew Kauffman
 */
@SuppressWarnings("serial")
public class EightballFrame extends JFrame 
{
	private Canvas canvas;
	private JPanel controlPanel;
		
	/**
	 * Constructor
	 */
	public EightballFrame() {
		setSize(800, 400);
		setResizable(false);
		setLayout(new BorderLayout());
		
		createCanvas();
		createControlPanel();
		populateTable();
	}
	
	/*
	 * Create Canvas
	 */
	private void createCanvas() {
		canvas = new Canvas();
		canvas.setSize(new Dimension(800, 300));
		canvas.setAnimationDelay(30);
		add(canvas, BorderLayout.CENTER);
	}
	
	/*
	 * Create control panel
	 */
	private void createControlPanel() {
		controlPanel = new JPanel();
		controlPanel.setLayout(new GridLayout(2, 1));

		JPanel buttonPanel = new JPanel();
		JButton startButton = new JButton("Start");
		startButton.addActionListener(ae -> start());
		
		JButton stopButton = new JButton("Stop");
		stopButton.addActionListener(ae -> stop());
		
		JButton clearButton = new JButton("Clear");
		clearButton.addActionListener(ae -> clear());
		
		buttonPanel.add(startButton);
		buttonPanel.add(stopButton);
		buttonPanel.add(clearButton);
		
		controlPanel.add(buttonPanel);
		add(controlPanel, BorderLayout.SOUTH);
	}
	
	private void populateTable() {
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
	
	/*
	 * Start animation
	 */
	private void start() {
		canvas.clear();
		populateTable();
		canvas.start();
	}
	
	/*
	 * Stop animation
	 */
	private void stop() {
		canvas.stop();
	}
	
	/*
	 * Clear canvas
	 */
	private void clear() {
		canvas.clear();
	}
}
