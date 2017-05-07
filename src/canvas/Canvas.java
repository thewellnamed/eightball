package canvas;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JComponent;
import javax.swing.Timer;
import javax.vecmath.Tuple2i;

/**
 * Base Canvas class
 * Provides basic implementation for rendering a set of CanvasObjects in a defined region
 * Depends on an implementation of CanvasProcessor to handle movement updates
 */
@SuppressWarnings("serial")
public class Canvas extends JComponent
{
	protected TreeSet<CanvasObject> objects;
	protected CanvasProcessor processor;
	protected Rectangle canvasBounds;
	protected Map<Integer, Set<Tuple2i>> canvasHoles;
	private Timer animationTimer;
	private int animationDelay;
	
	// Used for wall collisions
	public final static String canvasObjectType = "Wall";
	public static final int WALL_NORTH = 1;
	public static final int WALL_EAST = 2;
	public static final int WALL_SOUTH = 3;
	public static final int WALL_WEST = 4;
	
	/**
	 * Default constructor
	 */
	public Canvas() {
		this(null);
	}
	
	/**
	 * Constructor
	 */
	public Canvas(CanvasProcessor proc) {
		objects = new TreeSet<CanvasObject>();
		processor = proc;
		canvasBounds = getBounds();
		canvasHoles = new HashMap<Integer, Set<Tuple2i>>();
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
	 * Remove object from Canvas
	 * @param o Object to remove
	 */
	public void remove(CanvasObject o) {
		objects.remove(o);
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
	 * Returns definition of holes to punch in canvas bounds, defined per side (NORTH, EAST, SOUTH, WEST)
	 */
	public Map<Integer, Set<Tuple2i>> getCanvasHoles() {
		return canvasHoles;
	}
	
	/**
	 * Set canvas hole definition
	 * @param holes Holes in canvas
	 */
	public void setCanvasHoles(Map<Integer, Set<Tuple2i>> holes) {
		canvasHoles = holes;
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
