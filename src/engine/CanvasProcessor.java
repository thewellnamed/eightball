package engine;

import java.awt.Dimension;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import javax.vecmath.Vector2d;

/**
 * Movement and Collision Processing for CanvasObjects in Canvas
 */
@SuppressWarnings("serial")
public class CanvasProcessor 
{
	private int numRows;
	private int numCols;
	private int maxRegions;
	private double regionWidth;
	private double regionHeight;
	private Canvas canvas;
	private Dimension canvasSize;
	private Dimension maxObjectSize;
	private HashMap<Integer, CollisionNode> nodes;
	private HashMap<Integer, Integer> lastCollision;
	
	private final int MAX_COLLISION_PASSES = 5;
	
	/**
	 * Construct a new collision processor
	 * @param canvas Canvas for which to process collisions
	 */
	public CanvasProcessor(Canvas canvasForProcessing) {
		canvas = canvasForProcessing;
		canvasSize = canvas.getSize();
		maxObjectSize = canvas.getMaxObjectSize();

		// determine collision grid rows and columns
		initalizeCollisionGrid();
		
		nodes = new HashMap<Integer, CollisionNode>(maxRegions);
		lastCollision = new HashMap<Integer, Integer>(100);

		int nextRegion = 0;
		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numCols; j++) {
				double regionX = regionWidth * j;
				double regionY = regionHeight * i;	
		
				Rectangle2D rect = new Rectangle2D.Double(regionX, regionY, regionWidth, regionHeight);
				nodes.put(nextRegion++, new CollisionNode(rect));
			}
		}	
	}
	
	/*
	 * Determine grid for collision management
	 */
	private void initalizeCollisionGrid() {
		
		double canvasArea = canvasSize.height * canvasSize.width;
		double objectArea = maxObjectSize.height * maxObjectSize.width;
		int spriteCount = canvas.getObjects().size();
		int canvasDensityFactor = (int)Math.floor(Math.sqrt(canvasArea / (objectArea * spriteCount))) + 1;
		
		numRows = (int)Math.ceil(canvasSize.height / (maxObjectSize.height * canvasDensityFactor));
		numCols = (int)Math.ceil(canvasSize.width / (maxObjectSize.width * canvasDensityFactor));
		
		if (numRows < 1) numRows = 1;
		if (numCols < 1) numCols = 1;
		
		maxRegions = (numRows * numCols);
		regionWidth = canvasSize.getWidth() / numCols;
		regionHeight = canvasSize.getHeight() / numRows;
	}
	
	/**
	 * Main processing method -- process collisions and update sprite states
	 */
	public void processCollisions() {
		Collection<CanvasObject> objects = canvas.getObjects();
		int pass = 0;
		boolean haveCollision = false;		
		lastCollision.clear();
		HashSet<Integer> collisions = new HashSet<Integer>();
		
		do {
			clearCollisionNodes();
			collisions.clear();
			pass++;
			
			// Sprite collisions
			objects.forEach(o -> addObjectToCollisionGrid(o));

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
				
			// Check for wall collisions and move each sprite
			for (CanvasObject o : objects) {
				Point2D desired = o.getNextLocation();
				Dimension size = o.getSize();
				
				int maxWidth = canvasSize.width - 1;
				int maxHeight = canvasSize.height - 1;

				// check for wall collisions
				if (desired.getX() > maxWidth - size.width) {
					collide(o, Canvas.WALL_EAST);
					collisions.add(o.hashCode());
					lastCollision.put(o.hashCode(), Canvas.WALL_EAST);
					haveCollision = true;
				} else if (desired.getX() < 0) {
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
				} else if (desired.getY() < 0) {
					collide(o, Canvas.WALL_NORTH);
					collisions.add(o.hashCode());
					lastCollision.put(o.hashCode(), Canvas.WALL_NORTH);
					haveCollision = true;
				}
			}			
		} while (haveCollision && pass < MAX_COLLISION_PASSES);
	}	
	
	/*
	 * Add sprite to collision grid
	 * May add a single sprite to up to four grid locations if it spans multiple nodes
	 */
	private void addObjectToCollisionGrid(CanvasObject o) {
		Dimension objSize = o.getSize();
		Point2D objPosition = o.getNextLocation();
		
		// Todo: move this hack elsewhere?
		if (objPosition.getX() < 0) objPosition.setLocation(0, objPosition.getY());
		if (objPosition.getY() < 0) objPosition.setLocation(objPosition.getX(), 0);
		
		// calculate North, South, East, and West edges of Sprite, add to applicable collision regions
		Point2D north = new Point2D.Double(Math.min(objPosition.getX() + objSize.width/2, canvasSize.width - 1), 
										   Math.min(objPosition.getY(), canvasSize.height - 1));
		addCollisionPointToGrid(o, north);
		
		Point2D east = new Point2D.Double(Math.min(objPosition.getX() + objSize.width, canvasSize.width - 1), 
										  Math.min(objPosition.getY() + objSize.height/2, canvasSize.height - 1));
		addCollisionPointToGrid(o, east);
		
		Point2D south = new Point2D.Double(Math.min(objPosition.getX() + objSize.width/2, canvasSize.width - 1), 
										   Math.min(objPosition.getY() + objSize.width, canvasSize.height - 1));
		addCollisionPointToGrid(o, south);
		
		Point2D west = new Point2D.Double(Math.min(objPosition.getX(), canvasSize.width - 1), 
										 Math.min(objPosition.getY() + objSize.height/2, canvasSize.height - 1));
		addCollisionPointToGrid(o, west);
	}	
	
	/*
	 * Add sprite point (E, N, W, S) to grid node
	 */
	private void addCollisionPointToGrid(CanvasObject o, Point2D location) {
		int row = (int)(Math.floor((location.getY() / canvasSize.getHeight()) * numRows)); 
		int col = (int)(Math.floor((location.getX() / canvasSize.getWidth()) * numCols));
		
		// bit of a hack for boundary conditions when regionWidth or regionHeight don't cleanly divide the canvas...
		// there's probably a better way to manage this.
		if (col * regionWidth > location.getX()) col--;
		if (row * regionHeight > location.getY()) row--;
		
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
	 * Handle collision between two sprites
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
			// collision
			collide(a, b);

			lastCollision.put(aHash, bHash);
			lastCollision.put(bHash, aHash);
			haveCollision = true;
		}
		
		return haveCollision;
	}
	
	/*
	 * Check for a collision between objects at next position
	 */
	private boolean collisionPending(CanvasObject a, CanvasObject b) {
		Area intersection = a.getAreaForCollision();
		intersection.intersect(b.getAreaForCollision());

		return !intersection.isEmpty();
	}
	
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
		// CoR == 0.95
		
		Vector2d aNorm = new Vector2d(unitNormalVector);			
		aNorm.scale(((bMass * (bNormalScaleFactor - aNormalScaleFactor)) + 
				                (aMass * aNormalScaleFactor) + (bMass * bNormalScaleFactor)) / (aMass + bMass));
		newVectorForA.add(aNorm);
		a.setMovementVector(newVectorForA);
		
		Vector2d bNorm = new Vector2d(unitNormalVector);			
		bNorm.scale(((aMass * 0.97 * (aNormalScaleFactor - bNormalScaleFactor)) + 
				                (bMass * bNormalScaleFactor) + (aMass * aNormalScaleFactor)) / (aMass + bMass));
		newVectorForB.add(bNorm);
		b.setMovementVector(newVectorForB);
	}
	
	private void collide(CanvasObject o, int wall) {
		switch (wall) {
			case Canvas.WALL_EAST:
			case Canvas.WALL_WEST:
				o.getMovementVector().x *= -0.83;
				break;
				
			case Canvas.WALL_NORTH:
			case Canvas.WALL_SOUTH:
				o.getMovementVector().y *= -0.83;
				break;
				
			default:
				throw new IllegalArgumentException(String.format("Unknown wall type for bounce: %d", wall));
		}
	}
	
	private Vector2d getNormalizedCollisionVector(CanvasObject a, CanvasObject b) {
		Point2D aCenter = a.getNextCenterPoint();
		Point2D bCenter = b.getNextCenterPoint();
		
		Vector2d normalVector = new Vector2d(aCenter.getX() - bCenter.getX(), aCenter.getY() - bCenter.getY());
		normalVector.normalize();
		return normalVector;
	}
	
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
		nodes.values().forEach(sprites -> sprites.clear());
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