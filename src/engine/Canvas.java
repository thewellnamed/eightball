package engine;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.Timer;

/**
 * Canvas for rendering Billiard Table
 * @author Matthew Kauffman
 */
@SuppressWarnings("serial")
public class Canvas extends JComponent
{
	private ArrayList<CanvasObject> objects;
	private Timer animationTimer;
	private int animationDelay;
	private CanvasProcessor processor;
	
	public static final int WALL_NORTH = 1;
	public static final int WALL_EAST = 2;
	public static final int WALL_SOUTH = 3;
	public static final int WALL_WEST = 4;
	
	/**
	 * Constructor
	 */
	public Canvas() {
		objects = new ArrayList<CanvasObject>();
	}
		
	/**
	 * Objects currently in canvas
	 * @return CanvasObject collection
	 */
	public Collection<CanvasObject> getObjects() {
		return objects;
	}
	
	/**
	 * Add object to canvas
	 * @param o CanvasObject to add
	 */
	public void add(CanvasObject o) {
		objects.add(o);
	}
	
	/**
	 * Add multiple objects to canvas
	 * @param collection CanvasObject collection
	 */
	public void add(Collection<CanvasObject> collection) {
		objects.addAll(collection);
	}
	
	/**
	 * Removes all active CanvasObjects and halts animation
	 */
	public void clear() {
		if (animationTimer.isRunning())
			animationTimer.stop();
		
		objects.clear();
		repaint();
	}
	
	/**
	 * Set animation delay
	 * @param delay (ms)
	 */
	public void setAnimationDelay(int delay) {
		animationDelay = delay;
		
		if (animationTimer != null) {
			animationTimer.setDelay(animationDelay);
		} else {
			animationTimer = new Timer(animationDelay, ae -> updateSprites());
		}
	}
	
	/**
	 * Start animating
	 */
	public void start() {
		if (processor == null) {
			processor = new CanvasProcessor(this);
		}
		
		if (!animationTimer.isRunning())
			animationTimer.start();
	}
	
	/**
	 * Stop animating
	 */
	public void stop() {
		if (animationTimer.isRunning())
			animationTimer.stop();
	}
	
	/**
	 * @return Animation currently running (T/F)
	 */
	public boolean isRunning() {
		return animationTimer.isRunning();
	}
	
	/**
	 * @return largest sprite size
	 */
	public Dimension getMaxObjectSize() {
		Dimension d = new Dimension(0,0);
		
		for (CanvasObject o : objects) {
			Dimension size = o.getSize();
			if (size.width * size.height > d.width*d.height) {
				d.setSize(size);
			}
		}
		
		return d;
	}
		
	/**
	 * Draw canvas and render all objects
	 */
	@Override
	public void paintComponent(Graphics g) {
		objects.forEach(o -> o.draw((Graphics2D) g));
	}
	
	/**
	 * Main animation timer callback
	 */
	private void updateSprites() {
		processor.processCollisions();
		objects.forEach(o -> o.move());
		
		repaint();
	}
}
