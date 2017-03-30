package eightball;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import canvas.CanvasObject;
import eightball.enums.*;


/**
 * Billiard Ball implementation 
 */
public class BilliardBall extends CanvasObject
{	
	private BallDefinition ball;
	
	public static String canvasObjectType = "BilliardBall";
		
	/**
	 * Default constructor
	 */
	public BilliardBall(BallDefinition b) {
		super();
		ball = b;
		
		// basic ball characteristics
		setSize(new Dimension(25, 25));
	}

	/**
	 * Initialize from existing ball
	 * @param src BilliardBall
	 */
	public BilliardBall(BilliardBall src) {
		super(src);
	}
	
	@Override
	public String getType() {
		return canvasObjectType;
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
		return String.format("BilliardBall(%d)", ball.getNumber());
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
		g.drawString(String.format("%d", ball.getNumber()), (int)(bounds.getX() + bounds.getWidth()/3), (int)(bounds.getY() + (bounds.getHeight()/2)));
	}
}
