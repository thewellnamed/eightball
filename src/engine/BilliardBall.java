package engine;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

/**
 * Billiard Ball implementation 
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

	/**
	 * Initialize from existing ball
	 * @param src BilliardBall
	 */
	public BilliardBall(BilliardBall src) {
		super(src);
	}
	
	/**
	 * Get Area object representing ball
	 */
	@Override
	public Area getArea() {
		Rectangle2D bounds = getBounds();
		return new Area(new Ellipse2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight()));
	}
	
	/**
	 * Get Area object at next position
	 */
	@Override
	public Area getAreaForCollision() {
		Rectangle2D bounds = getNextBounds();
		return new Area(new Ellipse2D.Double(bounds.getX()-1, bounds.getY()-1, bounds.getWidth()+2, bounds.getHeight()+2));
	}
	
	/** 
	 * Clone implementation
	 */
	@Override
	public CanvasObject clone() {
		return new BilliardBall(this);
	}
	
	/**
	 * String representation of BilliardBall
	 */
	@Override
	public String toString() {
		return String.format("BilliardBall(%d)", id);
	}
	
	/**
	 * Render
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
