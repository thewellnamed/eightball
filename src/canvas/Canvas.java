package canvas;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.JComponent;
import javax.swing.Timer;

/**
 * Base Canvas class
 * Provides basic implementation for rendering a set of CanvasObjects in a defined region
 * Depends on an implementation of CanvasProcessor to handle movement updates
 */
@SuppressWarnings("serial")
public class Canvas extends JComponent
{
	protected HashSet<CanvasObject> objects;
	protected CanvasProcessor processor;
	protected Rectangle canvasBounds;
	private Timer animationTimer;
	private int animationDelay;
	
	public static final int WALL_NORTH = 1;
	public static final int WALL_EAST = 2;
	public static final int WALL_SOUTH = 3;
	public static final int WALL_WEST = 4;
	
	/**
	 * Constructor
	 */
	public Canvas(CanvasProcessor proc) {
		objects = new HashSet<CanvasObject>();
		processor = proc;
		canvasBounds = getBounds();
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
		repaint(canvasBounds);
	}
	
	/**
	 * @return CanvasProcessor
	 */
	public CanvasProcessor getProcessor() {
		return processor;
	}
	
	/**
	 * Set CanvasProcessor for Canvas
	 * @param proc New Processor
	 */
	public void setProcessor(CanvasProcessor proc) {
		processor = proc;
	}
	
	/**
	 * @return Bounds of renderable part of canvas
	 */
	public Rectangle getCanvasBounds() {
		return canvasBounds;
	}
	
	/**
	 * Set bounds of managed part of canvas
	 * @param bounds New bounds
	 */
	public void setCanvasBounds(Rectangle bounds) {
		canvasBounds = bounds;
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
			animationTimer = new Timer(animationDelay, ae -> update());
		}
	}
	
	/**
	 * Start animating
	 */
	public void start() {
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
	 * Draw canvas and render all objects
	 */
	@Override
	public void paintComponent(Graphics g) {
		objects.forEach(o -> o.draw((Graphics2D) g));
	}
	
	/**
	 * Main animation timer callback
	 */
	protected void update() {
		processor.update(objects);
		repaint(canvasBounds);
	}
}
