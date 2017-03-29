package eightball;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import javax.vecmath.Vector2d;

import canvas.*;

/**
 * Movement and Collision Processing for CanvasObjects in Canvas
 */
@SuppressWarnings("serial")
public class BilliardsCanvasProcessor implements CanvasProcessor
{
	private int numRows;
	private int numCols;
	private int maxRegions;
	private double regionWidth;
	private double regionHeight;
	private Rectangle canvas;
	private Dimension objectSize;
	private int expectedObjectCount;
	private HashMap<Integer, CollisionNode> nodes;
	private HashMap<Integer, Integer> lastCollision;
	
	// collision processing
	private static final int MAX_COLLISION_PASSES = 5;
	private static final double COR_BALL_COLLISIONS = 0.965; // coefficient of restitution: ball<-->ball
	private static final double COR_WALL_COLLISIONS = 0.74;  // coefficient of restitution: ball-->rail
	private static final double COEFFICIENT_FRICTION = 0.98; // coefficient of friction: rolling ball
	
	public boolean initialize(Rectangle bounds, Dimension objSize, int objCount) {
		canvas = bounds;
		objectSize = objSize;
		expectedObjectCount = objCount;
		
		initializeCollisionGrid();
		
		nodes = new HashMap<Integer, CollisionNode>(maxRegions);
		lastCollision = new HashMap<Integer, Integer>(100);

		int nextRegion = 0;
		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numCols; j++) {
				double regionX = canvas.x + (regionWidth * j);
				double regionY = canvas.y + (regionHeight * i);	
		
				Rectangle2D rect = new Rectangle2D.Double(regionX, regionY, regionWidth, regionHeight);
				nodes.put(nextRegion++, new CollisionNode(rect));
			}
		}
		
		return true;
	}
	
	/*
	 * Determine grid for collision management
	 */
	private void initializeCollisionGrid() {
		double canvasArea = canvas.getHeight() * canvas.getWidth();
		double objectArea = objectSize.height * objectSize.width;
		int canvasDensityFactor = (int)Math.floor(Math.sqrt(canvasArea / (objectArea *expectedObjectCount))) + 1;
		
		numRows = (int)Math.ceil(canvas.getHeight() / (objectSize.height * canvasDensityFactor));
		numCols = (int)Math.ceil(canvas.getWidth() / (objectSize.width * canvasDensityFactor));
		
		if (numRows < 1) numRows = 1;
		if (numCols < 1) numCols = 1;
		
		maxRegions = (numRows * numCols);
		regionWidth = canvas.getWidth() / numCols;
		regionHeight = canvas.getHeight() / numRows;
	}
	
	/**
	 * Main processing method -- process collisions and update CanvasObject states
	 */
	public boolean update(Collection<CanvasObject> objects) {
		int pass = 0;
		boolean haveCollision = false;		
		lastCollision.clear();
		HashSet<Integer> collisions = new HashSet<Integer>();
		
		// iterate processor up to MAX_COLLISION_PASSES times as long
		// as the last pass found a collision.
		// This reduces overlap problems caused when multiple objects are all colliding
		do {
			clearCollisionNodes();
			collisions.clear();
			pass++;
			
			// CanvasObject collisions
			// first, add to collision grid...
			objects.forEach(o -> addObjectToCollisionGrid(o));

			// second, check collisions within each grid cell
			for (int i = 0; i < maxRegions; i++) {
				CollisionNode node = nodes.get(i);
				int size = node.size();
				
				if (size > 1) {
					CanvasObject[] objectArray = new CanvasObject[size];
					node.toArray(objectArray);
					
					for (int a = 0; a < size; a++) {
						for (int b = a+1; b < size; b++) {
							if (checkAndProcessCollision(objectArray[a], objectArray[b])) {
								collisions.add(objectArray[a].hashCode());
								collisions.add(objectArray[b].hashCode());
								haveCollision = true;
							};
						}
					}
				}
			}
				
			// Check for wall collisions
			for (CanvasObject o : objects) {
				Point2D desired = o.getNextLocation();
				Dimension size = o.getSize();
				
				int maxWidth = canvas.x + canvas.width - 1;
				int maxHeight = canvas.y + canvas.height - 1;

				// check for wall collisions
				if (desired.getX() > maxWidth - size.width) {
					collide(o, Canvas.WALL_EAST);
					collisions.add(o.hashCode());
					lastCollision.put(o.hashCode(), Canvas.WALL_EAST);
					haveCollision = true;
				} else if (desired.getX() < canvas.x) {
					collide(o, Canvas.WALL_WEST);
					collisions.add(o.hashCode());
					lastCollision.put(o.hashCode(), Canvas.WALL_WEST);
					haveCollision = true;
				}
				
				if (desired.getY() > maxHeight - size.height) {
					collide(o, Canvas.WALL_SOUTH);
					collisions.add(o.hashCode());
					lastCollision.put(o.hashCode(), Canvas.WALL_SOUTH);
					haveCollision = true;
				} else if (desired.getY() < canvas.y) {
					collide(o, Canvas.WALL_NORTH);
					collisions.add(o.hashCode());
					lastCollision.put(o.hashCode(), Canvas.WALL_NORTH);
					haveCollision = true;
				}
			}			
		} while (haveCollision && pass < MAX_COLLISION_PASSES);
		
		for (CanvasObject o : objects) {
			// move each object
			o.move();
			
			// apply friction
			Vector2d mv = o.getMovementVector();
			if (mv.length() > 0.25) {
				mv.scale(COEFFICIENT_FRICTION);
			} else {
				o.setMovementVector(new Vector2d(0, 0));
			}
		}
		
		return true;
	}	
	
	/*
	 * Add object to collision grid
	 * May add a single object to up to four grid locations if it spans multiple nodes
	 */
	private void addObjectToCollisionGrid(CanvasObject o) {
		Dimension objSize = o.getSize();
		Point2D objPosition = o.getNextLocation();
		
		// Todo: move this hack elsewhere?
		// This may not be needed any longer
		if (objPosition.getX() < canvas.x) objPosition.setLocation(canvas.x, objPosition.getY());
		if (objPosition.getY() < canvas.y) objPosition.setLocation(objPosition.getX(), canvas.y);
		
		int maxX = canvas.x + canvas.width - 1;
		int maxY = canvas.y + canvas.height - 1;
		
		// calculate North, South, East, and West edges of Sprite, add to applicable collision regions
		Point2D north = new Point2D.Double(Math.min(objPosition.getX() + objSize.width/2, maxX), 
										   Math.min(objPosition.getY(), maxY));
		addCollisionPointToGrid(o, north);
		
		Point2D east = new Point2D.Double(Math.min(objPosition.getX() + objSize.width, maxY), 
										  Math.min(objPosition.getY() + objSize.height/2, maxY));
		addCollisionPointToGrid(o, east);
		
		Point2D south = new Point2D.Double(Math.min(objPosition.getX() + objSize.width/2, maxX), 
										   Math.min(objPosition.getY() + objSize.width, maxY));
		addCollisionPointToGrid(o, south);
		
		Point2D west = new Point2D.Double(Math.min(objPosition.getX(), maxX), 
										 Math.min(objPosition.getY() + objSize.height/2, maxY));
		addCollisionPointToGrid(o, west);
	}	
	
	/*
	 * Add collision point (E, N, W, S) to grid node
	 */
	private void addCollisionPointToGrid(CanvasObject o, Point2D location) {
		int row = (int)(Math.floor((location.getY() / (canvas.x + canvas.getHeight())) * numRows)); 
		int col = (int)(Math.floor((location.getX() / (canvas.y + canvas.getWidth())) * numCols));
		
		// bit of a hack for rounding error when regionWidth or regionHeight don't cleanly divide the canvas...
		if (canvas.x + (col * regionWidth) > location.getX()) col--;
		if (canvas.y + (row * regionHeight) > location.getY()) row--;
		
		int region = (row * numCols) + col;
		
		CollisionNode node = nodes.get(region);
		
		// Sanity checks
		if (node == null) {
			throw new IllegalStateException(
					String.format("failed to find node for point at %s, row=%d, col=%d, region=%d", 
							location, row, col, region));
		}
		
		if (!node.getBounds().contains(location)) {
			throw new IllegalStateException(
					String.format("found node(%s) but does not contain point(%s), row=%d, col=%d, region=%d!\n", 
						node.getBounds(), location, row, col, region)); 
		}
		
		node.add(o);
	}
	
	/*
	 * Check for collision between two CanvasObjects
	 * Calls collide() if collision found
	 */
	private boolean checkAndProcessCollision(CanvasObject a, CanvasObject b) {
		boolean haveCollision = false;
		Vector2d aV = a.getMovementVector();
		Vector2d bV = b.getMovementVector();
		int aHash = a.hashCode();
		int bHash = b.hashCode();
		
		if (aV.getX() == 0 && aV.getY() == 0 && bV.getX() == 0 && bV.getY() == 0) {
			return false;
		}
		
		// deformation
		Area intersection = a.getOverlapWith(b);
		if (!intersection.isEmpty()) {
			deformCollision(a, b, intersection.getBounds2D());
		}
		
		// pending collision
		if (collisionPending(a, b) && (!lastCollision.containsKey(aHash) || lastCollision.get(aHash) != bHash)) {	
			collide(a, b);

			lastCollision.put(aHash, bHash);
			lastCollision.put(bHash, aHash);
			haveCollision = true;
		}
		
		return haveCollision;
	}
	
	/*
	 * Is a collision pending?
	 */
	private boolean collisionPending(CanvasObject a, CanvasObject b) {
		Area intersection = a.getAreaForCollision();
		intersection.intersect(b.getAreaForCollision());

		return !intersection.isEmpty();
	}
	
	/*
	 * Perform collision between two objects
	 */
	private void collide(CanvasObject a, CanvasObject b) {
		// logic can be found here: http://vobarian.com/collisions/2dcollisions2.pdf
		Vector2d unitNormalVector = getNormalizedCollisionVector(a, b);
		Vector2d unitTangentVector = new Vector2d(unitNormalVector.y, -unitNormalVector.x); 
		Vector2d aVector = a.getMovementVector();
		Vector2d bVector = b.getMovementVector();
				
		double aMass = a.getMass();
		double bMass = b.getMass();
		double aNormalScaleFactor = aVector.dot(unitNormalVector);
		double bNormalScaleFactor = bVector.dot(unitNormalVector);
		
		Vector2d newVectorForA = new Vector2d(unitTangentVector);
		Vector2d newVectorForB = new Vector2d(unitTangentVector);
		
		newVectorForA.scale(aVector.dot(unitTangentVector));
		newVectorForB.scale(bVector.dot(unitTangentVector));
		
		// scaling factor in the form for inelastic collisions: https://en.wikipedia.org/wiki/Inelastic_collision		
		Vector2d aNorm = new Vector2d(unitNormalVector);			
		aNorm.scale(((bMass * COR_BALL_COLLISIONS * (bNormalScaleFactor - aNormalScaleFactor)) + 
				                (aMass * aNormalScaleFactor) + (bMass * bNormalScaleFactor)) / (aMass + bMass));
		newVectorForA.add(aNorm);
		a.setMovementVector(newVectorForA);
		
		Vector2d bNorm = new Vector2d(unitNormalVector);			
		bNorm.scale(((aMass * COR_BALL_COLLISIONS * (aNormalScaleFactor - bNormalScaleFactor)) + 
				                (bMass * bNormalScaleFactor) + (aMass * aNormalScaleFactor)) / (aMass + bMass));
		newVectorForB.add(bNorm);
		b.setMovementVector(newVectorForB);
	}
	
	/*
	 * Perform collision between object and side-wall
	 */
	private void collide(CanvasObject o, int wall) {
		switch (wall) {
			case Canvas.WALL_EAST:
			case Canvas.WALL_WEST:
				o.getMovementVector().x *= -COR_WALL_COLLISIONS;
				break;
				
			case Canvas.WALL_NORTH:
			case Canvas.WALL_SOUTH:
				o.getMovementVector().y *= -COR_WALL_COLLISIONS;
				break;
				
			default:
				throw new IllegalArgumentException(String.format("Unknown wall type for bounce: %d", wall));
		}
	}
	
	/*
	 * Return unit normal vector to a collision between objects (using center-of-mass defined per object)
	 */
	private Vector2d getNormalizedCollisionVector(CanvasObject a, CanvasObject b) {
		Point2D aCenter = a.getNextCenterPoint();
		Point2D bCenter = b.getNextCenterPoint();
		
		Vector2d normalVector = new Vector2d(aCenter.getX() - bCenter.getX(), aCenter.getY() - bCenter.getY());
		normalVector.normalize();
		return normalVector;
	}
	
	/*
	 * Fix overlap between objects
	 */
	private void deformCollision(CanvasObject a, CanvasObject b, Rectangle2D intersection) {
		Rectangle2D aPos = a.getBounds();
		Rectangle2D bPos = b.getBounds();
		
		Vector2d aVector = getNormalizedCollisionVector(a, b);
		aVector.setX(aVector.getX() * intersection.getWidth());
		aVector.setY(aVector.getY() * intersection.getHeight());
		Vector2d bVector = new Vector2d(-aVector.getX(), -aVector.getY());	
		Point2D.Double aPosNew = new Point2D.Double(aPos.getX() + aVector.getX(), aPos.getY() + aVector.getY());
		Point2D.Double bPosNew = new Point2D.Double(bPos.getX() + bVector.getX(), bPos.getY() + bVector.getY());
		
		a.setLocation(aPosNew);
		b.setLocation(bPosNew);
	}

	/*
	 * Clear collision grid
	 */
	private void clearCollisionNodes() {
		nodes.values().forEach(node -> node.clear());
	}
	
	/*
	 * Helper class for managing collisions
	 */
	private class CollisionNode extends HashSet<CanvasObject> 
	{
		private Rectangle2D bounds;
		
		public CollisionNode(Rectangle2D nodeBoundary) {
			super();
			bounds = nodeBoundary;
		}
		
		public Rectangle2D getBounds() {
			return bounds;
		}
	}
}