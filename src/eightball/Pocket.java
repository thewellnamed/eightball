package eightball;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.vecmath.Point2i;
import javax.vecmath.Tuple2i;

import canvas.Canvas;
import canvas.CanvasObject;

/**
 * Billiard table pockets
 * Definitions for placement and canvas hole data used by table
 */
public class Pocket extends CanvasObject 
{
	private Polygon poly;
	private int type;
	private Rectangle canvasBounds;
	private Map<Integer, Set<Tuple2i>> holes;
	
	public static String canvasObjectType = "Pocket";
	
	protected static final int TOP_LEFT = 0;
	protected static final int TOP_CENTER = 1;
	protected static final int TOP_RIGHT = 2;
	protected static final int BOTTOM_LEFT = 3;
	protected static final int BOTTOM_CENTER = 4;
	protected static final int BOTTOM_RIGHT = 5;
	protected static final int NUMBER_POCKETS = 6;
	
	// if true, adds additional rendering
	// useful for tweaking mechanics of balls sinking
	private static final boolean DEBUG = false; 
	
	/**
	 * Constructor
	 * @param pocketPosition Which Pocket is this? (TOP_LEFT ... BOTTOM_RIGHT)
	 * @param bounds Canvas rectangle
	 */
	public Pocket(int pocketPosition, Rectangle bounds) {
		super();
		
		int[] x;
		int[] y;
		type = pocketPosition;
		canvasBounds = bounds;
		setSize(new Dimension(38, 38));
		holes = new HashMap<Integer, Set<Tuple2i>>();
		canvasOrder = pocketPosition;
		
		HashSet<Tuple2i> north = new HashSet<Tuple2i>();
		HashSet<Tuple2i> east = new HashSet<Tuple2i>();
		HashSet<Tuple2i> south = new HashSet<Tuple2i>();
		HashSet<Tuple2i> west = new HashSet<Tuple2i>();
		
		switch(type) {
			case TOP_LEFT:
				setLocation(new Point2D.Double(canvasBounds.x - 17, canvasBounds.y - 18));
				
				north.add(new Point2i(canvasBounds.x - 100, canvasBounds.x + 27));
				holes.put(Canvas.WALL_NORTH, north);
				
				west.add(new Point2i(canvasBounds.y - 100, canvasBounds.y + 27));
				holes.put(Canvas.WALL_WEST, west);
				
				x = new int[] { 72, 100, 127, 101, 72 };
				y = new int[] { 85, 60, 90, 116, 85 }; 
				poly = new Polygon(x, y, 5);
				break;
				
			case TOP_CENTER:
				setLocation(new Point2D.Double(canvasBounds.x + 329, canvasBounds.y - 29));
				
				north.add(new Point2i(canvasBounds.x + 329, canvasBounds.x + 367));
				holes.put(Canvas.WALL_NORTH, north);
				
				x = new int[] { 427, 468, 468, 427 };
				y = new int[] { 60, 60, 90, 90 }; 
				poly = new Polygon(x, y, 4);
				break;
				
			case TOP_RIGHT:
				setLocation(new Point2D.Double(canvasBounds.x + canvasBounds.width - 22, canvasBounds.y - 18));
				
				north.add(new Point2i(canvasBounds.x + canvasBounds.width - 29, canvasBounds.x + canvasBounds.width + 100));
				holes.put(Canvas.WALL_NORTH, north);
				
				east.add(new Point2i(canvasBounds.y - 100, canvasBounds.y + 26));
				holes.put(Canvas.WALL_EAST, east);
				
				x = new int[] { 771, 799, 828, 799, 771 };
				y = new int[] { 90, 60, 89, 115, 90 }; 
				poly = new Polygon(x, y, 5);
				break;
				
			case BOTTOM_LEFT:
				setLocation(new Point2D.Double(canvasBounds.x - 18, canvasBounds.y + canvasBounds.height - 19));
				
				south.add(new Point2i(canvasBounds.x - 100, canvasBounds.x + 28));
				holes.put(Canvas.WALL_SOUTH, south);
				
				west.add(new Point2i(canvasBounds.y + canvasBounds.height - 25, canvasBounds.y + canvasBounds.height + 100));
				holes.put(Canvas.WALL_WEST, west);
				
				x = new int[] { 72, 101, 128, 101, 72 };
				y = new int[] { 440, 415, 440, 466, 440 }; 
				poly = new Polygon(x, y, 5);
				break;
				
			case BOTTOM_CENTER:
				setLocation(new Point2D.Double(canvasBounds.x + 329, canvasBounds.y + canvasBounds.height - 10));
				
				south.add(new Point2i(canvasBounds.x + 327, canvasBounds.x + 368));
				holes.put(Canvas.WALL_SOUTH, south);
				
				x = new int[] { 427, 468, 468, 427 };
				y = new int[] { 440, 440, 470, 470 }; 
				poly = new Polygon(x, y, 4);
				break;
				
			case BOTTOM_RIGHT:
				setLocation(new Point2D.Double(canvasBounds.x + canvasBounds.width - 22, canvasBounds.y + canvasBounds.height - 20));
				
				south.add(new Point2i(canvasBounds.x + canvasBounds.width - 29, canvasBounds.x + canvasBounds.width + 100));
				holes.put(Canvas.WALL_SOUTH, south);
				
				east.add(new Point2i(canvasBounds.y + canvasBounds.height - 25, canvasBounds.y + canvasBounds.height + 100));
				holes.put(Canvas.WALL_EAST, east);
				
				x = new int[] { 771, 799, 825, 799, 771 };
				y = new int[] { 440, 466, 444, 415, 440 }; 
				poly = new Polygon(x, y, 5);
				break;
				
			default:
				throw new IllegalArgumentException(String.format("Unknown pocket type: %d", type));
		}
	}
	
	/**
	 * Add canvas holes for this pocket to a collection
	 * Used by BilliardsTable
	 */
	public void setCanvasHoles(Map<Integer, Set<Tuple2i>> canvasHoles) {		
		for (int i : holes.keySet()) {
			Set<Tuple2i> myHoles = holes.get(i);
			Set<Tuple2i> cHoles = canvasHoles.get(i);
			
			if (cHoles == null) {
				canvasHoles.put(i, myHoles);
			} else {
				for (Tuple2i t : myHoles) {
					cHoles.add(t);
				}
			}
		}
	}
		
	/**
	 * CanvasObject Type used in Physics processor
	 */
	@Override
	public String getType() {
		return canvasObjectType;
	}
	
	/**
	 * Since we don't move, getNextLocation is just getLocation...
	 */
	@Override
	public Point2D getNextLocation() {
		return getLocation();
	}
	
	/**
	 * Override collision area with custom pocket polygon
	 * Determines where a ball needs to get to in order to sink
	 */
	@Override
	public Area getAreaForCollision() {
		return new Area(poly);
	}
	
	/**
	 * Not implemented
	 */
	@Override
	public CanvasObject clone() {
		// not implemented
		return null;
	}

	/**
	 * Pockets don't move...
	 */
	@Override
	public void move() {
	}
	
	/**
	 * Render 
	 * Normally only fills in black circle which is partially overwritten by canvas Rectangle...
	 */
	@Override
	public void draw(Graphics2D g) {
		if (type != TOP_CENTER && type != BOTTOM_CENTER) {
			Point2D loc = getLocation();
			Dimension sz = getSize();
			g.setColor(getColor());
			g.fillOval((int)loc.getX(), (int)loc.getY(), sz.width, sz.height);
		}
		
		// rendering hints for canvas boundary holes and collision areas
		if (DEBUG) {
			g.setColor(Color.RED);
			g.draw(poly);
			
			g.setColor(Color.WHITE);
			for (int i : holes.keySet()) {
				Set<Tuple2i> h = holes.get(i);
				
				switch (i) {
					case Canvas.WALL_NORTH:
						for (Tuple2i point : h) {
							g.drawLine(point.x, canvasBounds.y, point.y, canvasBounds.y);
						}
						break;
						
					case Canvas.WALL_EAST:
						for (Tuple2i point : h) {
							g.drawLine(canvasBounds.x + canvasBounds.width, point.x, canvasBounds.x + canvasBounds.width, point.y);
						}
						break;
						
					case Canvas.WALL_SOUTH:
						for (Tuple2i point : h) {
							g.drawLine(point.x, canvasBounds.y + canvasBounds.height, point.y, canvasBounds.y + canvasBounds.height);
						}
						break;
						
					case Canvas.WALL_WEST:
						for (Tuple2i point : h) {
							g.drawLine(canvasBounds.x, point.x, canvasBounds.x, point.y);
						}
						break;
						
					default:
						break;
				}
			}
		}
	}	
}
