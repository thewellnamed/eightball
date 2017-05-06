package eightball;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
	private int suspendedRenderCount;
	
	public static Dimension ballSize = new Dimension(25, 25);
	public static String canvasObjectType = "BilliardBall";
		
	/**
	 * Default constructor
	 */
	public BilliardBall(BallDefinition b) {
		super();
		ball = b;
		suspendedRenderCount = 0;
		
		// basic ball characteristics
		setSize(ballSize);
	}

	/**
	 * Initialize from existing ball
	 * @param src BilliardBall
	 */
	public BilliardBall(BilliardBall src) {
		super(src);
	}
	
	/**
	 * Get Ball Definition
	 * @return BallDefinition
	 */
	public BallDefinition getDefinition() {
		return ball;
	}
	
	/**
	 * CanvasObject Type used in Physics processor
	 */
	@Override
	public String getType() {
		return canvasObjectType;
	}
	
	@Override
	public void setSuspended(boolean value) {
		suspended = value;	
		if (!suspended) suspendedRenderCount = 0;
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
		return String.format("BilliardBall(%s, %d)", ball.getType(), ball.getNumber());
	}
	
	/**
	 * Render
	 */
	@Override
	public void draw(Graphics2D g) {
		if (suspended) {
			suspendedRenderCount++;
			if (suspendedRenderCount > 2)
				return;
		}
		
		Rectangle2D bounds = getBounds();
		Ellipse2D outline = new Ellipse2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		switch (ball.getType()) {
			case CUE:
				g.setColor(ball.getColor());
				g.fill(outline);
				break;
				
			case EIGHTBALL:
			case SOLID:
				g.setColor(ball.getColor());
				g.fill(outline);
				
				g.setColor(Color.WHITE);
				g.fill(new Ellipse2D.Double(bounds.getX() + 6, bounds.getY() + 6, bounds.getWidth() - 12, bounds.getHeight() - 12));
				
				drawBallNumber(g);
				break;
				
			case STRIPE:
				g.setColor(Color.WHITE);
				g.fill(outline);
				
				Area stripe = new Area(outline);
				Rectangle2D band = new Rectangle2D.Double(bounds.getX(), bounds.getY() + 4, bounds.getWidth(), bounds.getHeight() - 8);
				stripe.intersect(new Area(band));
				g.setColor(ball.getColor());
				g.fill(stripe);
				
				g.setColor(Color.WHITE);
				g.fill(new Ellipse2D.Double(bounds.getX() + 6, bounds.getY() + 6, bounds.getWidth() - 12, bounds.getHeight() - 12));
				drawBallNumber(g);

				break;
		}
	}
	
	private void drawBallNumber(Graphics2D g) {
		Font currentFont = g.getFont();
		Font newFont = currentFont.deriveFont(10F);
		g.setFont(newFont);
		
		int x = ball.getNumber() > 9 ? 7 : 11;
		int y = 16;
		
		g.setColor(Color.BLACK);
		g.drawString(String.format("%d", ball.getNumber()), (int)(bounds.getX() + x), (int)(bounds.getY() + y));
	}
}
