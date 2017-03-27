package engine;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import javax.vecmath.Vector2d;

/**
 * Ball implementation of Sprite
 * @author Matthew Kauffman
 */
public class BilliardBall extends CanvasObject
{	
	private int id;
	
	/**
	 * Default constructor
	 */
	public BilliardBall(int number) {
		super();
		id = number;
		
		// basic ball characteristics
		setSize(new Dimension(30, 30));
	}

	public BilliardBall(BilliardBall src) {
		super(src);
	}
	
	@Override
	public Area getArea() {
		Rectangle2D bounds = getBounds();
		return new Area(new Ellipse2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight()));
	}
	
	/**
	 * Builds a more complex representation of the Area of the Sprite for collision detection
	 */
	@Override
	public Area getAreaForCollision() {
		Rectangle2D bounds = getNextBounds();
		return new Area(new Ellipse2D.Double(bounds.getX()-1, bounds.getY()-1, bounds.getWidth()+2, bounds.getHeight()+2));
	}
		
	/**
	 * Update ball position. Apply friction
	 */
	public void move() {
		Vector2d mv = getMovementVector();
		if (mv.length() > 0.25) {
			super.move();
			getMovementVector().scale(0.99);
		} else {
			setMovementVector(new Vector2d(0, 0));
		}
	}
	
	/** 
	 * Clone implementation
	 */
	@Override
	public CanvasObject clone() {
		return new BilliardBall(this);
	}
	
	@Override
	public String toString() {
		return String.format("Ball(%d)", id);
	}
	
	/**
	 * Draw a ball
	 */
	@Override
	public void draw(Graphics2D g) {
		Rectangle2D bounds = getBounds();
		g.setColor(getColor());
		g.fill(new Ellipse2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight()));
		
		g.setColor(Color.WHITE);
		g.drawString(String.format("%d", id), (int)(bounds.getX() + bounds.getWidth()/3), (int)(bounds.getY() + (bounds.getHeight()/2)));
	}
}
