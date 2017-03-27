package engine;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.vecmath.Vector2d;

/**
 * Base class for objects renderable in Canvas
 * Manages the position, linear movement, and rendering of each canvas object
 * @author Matthew Kauffman
 */
public abstract class CanvasObject
{
	private Rectangle2D.Double bounds;
	private Vector2d movementVector;
	private double mass;
	private Color color;
	
	public CanvasObject() {
		bounds = new Rectangle2D.Double(0,  0,  0,  0);
		movementVector = new Vector2d(0, 0);
		mass = 1;
		color = Color.BLACK;
	}
	
	/**
	 * Copy an existing CanvasObject
	 * @param src CanvasObject to copy
	 */
	public CanvasObject(CanvasObject src) {
		bounds = src.getBounds();
		setMovementVector(src.getMovementVector());
		setColor(src.getColor());
		setMass(src.getMass());
	}
	
	/**
	 * Initialize sprite to specified position and vector
	 * @param initialPosition X,Y coordinates of initial position
	 * @param vector X,Y magnitudes of motion per move
	 */
	public void initialize(Point2D initialPosition, Vector2d vector) {
		bounds = new Rectangle2D.Double(initialPosition.getX(), initialPosition.getY(), bounds.getWidth(), bounds.getHeight());
		movementVector = vector;
	}
	
	/**
	 * @return Point containing x,y coordinates of sprite in canvas
	 */
	public Point2D getLocation() {
		return new Point2D.Double(bounds.x, bounds.y);
	}
	
	/**
	 * Set location
	 * @param p Point of new location
	 */
	public void setLocation(Point2D.Double p) {
		bounds.x = p.getX();
		bounds.y = p.getY();
	}
	
	/**
	 * @return Point of location after the next move
	 */
	public Point2D getNextLocation() {
		return new Point2D.Double(bounds.getX() + movementVector.getX(), bounds.getY() + movementVector.getY());
	}
		
	/**
	 * Get sprite size
	 * @return Dimension(width, height) of sprite
	 */
	public Dimension getSize() {
		return bounds.getBounds().getSize();
	}	
	
	/**
	 * Set sprite size
	 * @param newSize New Dimension(width, height) of sprite
	 */
	public void setSize(Dimension newSize) {
		bounds.width = newSize.getWidth();
		bounds.height = newSize.getHeight();
	}
	
	/**
	 * @return Mass
	 */
	public double getMass() {
		return mass;
	}
	
	/**
	 * Set mass
	 * @param value Double
	 */
	public void setMass(double value) {
		mass = value;
	}
		
	/**
	 * Get vector representing movement per unit time
	 * X=num pixels to move on x-axis per unit time
	 * Y=num pixels to move on y-axis per unit time
	 * @return Point containing movement vector
	 */
	public Vector2d getMovementVector() {
		return movementVector;
	}
	
	/**
	 * Set movement vector
	 * @param newVector (X,Y) pixels to move per unit time
	 */
	public void setMovementVector(Vector2d newVector) {
		movementVector = newVector;
	}

	/**
	 * Get Rectangle representing current bounds of sprite
	 * @return Rectangle coordinates (x,y,width,height)
	 */
	public Rectangle2D.Double getBounds() {		
		return bounds;
	}
		
	/**
	 * @return Bounding rectangle of next movement
	 */
	public Rectangle2D.Double getNextBounds() {
		return new Rectangle2D.Double(bounds.getX() + movementVector.getX(), bounds.getY() + movementVector.getY(), 
				               bounds.getWidth(), bounds.getHeight());
	}
	
	/**
	 * @return Point representing the center of mass
	 */
	public Point2D getCenterPoint() {
		return new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
	}
	
	/**
	 * @return Point representing the center of mass after next move
	 */
	public Point2D getNextCenterPoint() {
		Rectangle2D nextBounds = getNextBounds();
		return new Point2D.Double(nextBounds.getCenterX(), nextBounds.getCenterY());
	}
	
	/**
	 * Create area for current location
	 * Defaults to the bounding rectangle
	 * @return Area of Shape
	 */
	public Area getArea() {
		return new Area(getBounds());
	}
	
	/**
	 * Create an Area object to be used for Collision detection
	 * Defaults to the bounding rectangle
	 * @return Area of Shape
	 */
	public Area getAreaForCollision() {
		return new Area(getNextBounds());
	}
	
	/**
	 * @return Sprite Color
	 */
	public Color getColor() {
		return color;
	}
	
	/**
	 * Set sprite color
	 * @param c New color
	 */
	public void setColor(Color c) {
		color = c;
	}	
	
	/**
	 * Update sprite position using current position and movement vector
	 */
	public void move() {
		bounds = getNextBounds();
	}
	
	/**
	 * Check for intersection with other sprite in canvas
	 * Default uses bounding rectangle of both sprites
	 * @param s Sprite
	 * @return
	 */
	public Area getOverlapWith(CanvasObject o) {
		Area overlap = getArea();
		overlap.intersect(o.getArea());
		return overlap;
	}
		
	/**
	 * Clone a sprite
	 */
	public abstract CanvasObject clone();
	
	/**
	 * Abstract method to draw sprite
	 * @param g Graphics object on which to draw
	 */
	public abstract void draw(Graphics2D g);
	
	/**
	 * toString() override
	 */
	@Override
	public String toString() {
		return String.format("Sprite(%d)", hashCode());
	}
}
