package canvas.physics;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.vecmath.Tuple2i;
import javax.vecmath.Vector2d;

import canvas.*;

/**
 * Movement and Collision Processing using basic physics engine for CanvasObjects in Canvas
 */
@SuppressWarnings("serial")
public class BasicPhysicsCanvasProcessor implements CanvasProcessor
{
	private int numRows;
	private int numCols;
	private int maxRegions;
	private double regionWidth;
	private double regionHeight;
	private Rectangle canvas;
	private Map<Integer, Set<Tuple2i>> canvasHoles;
	private Dimension objectSize;
	private int expectedObjectCount;
	private CollisionNode[] nodes;
	private HashMap<CanvasObject, Integer> lastCollision;
	private BasicPhysicsModel model;
	
	/**
	 * Constructor
	 * @param physicsModel Physics model to use in processing
	 */
	public BasicPhysicsCanvasProcessor(BasicPhysicsModel physicsModel) {
		model = physicsModel;
	}
	
	/**
	 * Initialize processor
	 * See CanvasProcessor
	 */
	public boolean initialize(Rectangle bounds, Map<Integer, Set<Tuple2i>> holes, Dimension objSize, int objCount) {
		canvas = bounds;
		canvasHoles = holes;
		objectSize = objSize;
		expectedObjectCount = objCount;
		
		initializeCollisionGrid();		
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
		
		nodes = new CollisionNode[maxRegions];
		lastCollision = new HashMap<CanvasObject, Integer>(100);

		int nextRegion = 0;
		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numCols; j++) {
				double regionX = canvas.x + (regionWidth * j);
				double regionY = canvas.y + (regionHeight * i);	
		
				Rectangle2D rect = new Rectangle2D.Double(regionX, regionY, regionWidth, regionHeight);
				nodes[nextRegion++] = new CollisionNode(rect);
			}
		}
	}
	
	/**
	 * Main processing method -- process collisions and update CanvasObject states
	 * See CanvasProcessor
	 */
	public boolean update(Collection<CanvasObject> objects) {
		int pass = 0;
		boolean inMotion = false;
		boolean haveCollision = false;		
		lastCollision.clear();
		
		// iterate processor based on PhysicsModel
		// as the last pass found a collision.
		// This reduces overlap problems caused when multiple objects are all colliding
		do {
			clearCollisionNodes();
			pass++;
			
			// CanvasObject collisions
			// first, add to collision grid...
			objects.forEach(o -> addObjectToCollisionGrid(o));

			// second, check collisions within each grid cell
			for (int i = 0; i < maxRegions; i++) {
				CollisionNode node = nodes[i];
				int size = node.size();
				
				if (size > 1) {
					CanvasObject[] objectArray = new CanvasObject[size];
					node.toArray(objectArray);
					
					for (int a = 0; a < size; a++) {
						for (int b = a+1; b < size; b++) {
							if (checkAndProcessCollision(objectArray[a], objectArray[b])) {
								haveCollision = true;
							};
						}
					}
				}
			}
				
			// Check for wall collisions
			for (CanvasObject o : objects) {
				// Only collide if type allows for it and this object is not suspended
				CanvasObjectConfiguration config = model.getTypeConfig(o.getType());
				if (o.getSuspended() || config.getCollisionType(Canvas.canvasObjectType) != CollisionType.BOUNCE) {
					continue;
				}
				
				Point2D desired = o.getNextLocation();
				Dimension size = o.getSize();
				
				int maxWidth = canvas.x + canvas.width - 1;
				int maxHeight = canvas.y + canvas.height - 1;

				// check for wall collisions
				if (desired.getX() > maxWidth - size.width && !movingThroughCanvasHole(Canvas.WALL_EAST, desired, size)) {
					collide(o, Canvas.WALL_EAST);
					lastCollision.put(o, Canvas.WALL_EAST);
					haveCollision = true;
				} else if (desired.getX() < canvas.x && !movingThroughCanvasHole(Canvas.WALL_WEST, desired, size)) {
					collide(o, Canvas.WALL_WEST);
					lastCollision.put(o, Canvas.WALL_WEST);
					haveCollision = true;
				}
				
				if (desired.getY() > maxHeight - size.height && !movingThroughCanvasHole(Canvas.WALL_SOUTH, desired, size)) {
					collide(o, Canvas.WALL_SOUTH);
					lastCollision.put(o, Canvas.WALL_SOUTH);
					haveCollision = true;
				} else if (desired.getY() < canvas.y && !movingThroughCanvasHole(Canvas.WALL_NORTH, desired, size)) {
					collide(o, Canvas.WALL_NORTH);
					lastCollision.put(o, Canvas.WALL_NORTH);
					haveCollision = true;
				}
			}			
		} while (haveCollision && pass < model.maxCollisionPasses);
		
		for (CanvasObject o : objects) {
			// move each object
			o.move();
			
			// apply friction
			Vector2d mv = o.getMovementVector();
			if (!o.getSuspended() && mv.length() > 0.4) {
				mv.scale(model.getTypeConfig(o.getType()).frictionCoefficient);
				inMotion = true;
			} else {
				o.setMovementVector(new Vector2d(0, 0));
			}
		}
		
		return inMotion;
	}	
	

	
	/*
	 * Add object to collision grid
	 * May add a single object to up to four grid locations if it spans multiple nodes
	 */
	private void addObjectToCollisionGrid(CanvasObject o) {
		if (o.getSuspended())
			return;
		
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
		
		if (region < 0 || region >= maxRegions) {
			throw new IllegalStateException(
					String.format("failed to find node for point at %s, row=%d, col=%d, region=%d", 
							location, row, col, region));
		}
		
		CollisionNode node = nodes[region];
		
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
		// don't collide suspended objects
		if (a.getSuspended() || b.getSuspended()) {
			return false;
		}
		
		// check model to see if we can collide
		CanvasObjectConfiguration config = model.getTypeConfig(a.getType());
		if (config == null)
			return false;
		
		CollisionType collisionType = config.getCollisionType(b.getType());
		if (collisionType == CollisionType.BOUNCE) {
			return checkBounce(a, b);
		} else if (collisionType == CollisionType.CUSTOM) {
			CustomCollisionListener listener = config.getCustomListener(b.getType());
			if (listener != null) {
				return listener.checkCollision(a, b);
			}
		}
		
		return false;
	}
	
	private boolean checkBounce(CanvasObject a, CanvasObject b) {		
		Vector2d aV = a.getMovementVector();
		Vector2d bV = b.getMovementVector();
		int aHash = a.hashCode();
		int bHash = b.hashCode();
				
		// deformation
		Area intersection = a.getOverlapWith(b);
		if (!intersection.isEmpty()) {
			deformCollision(a, b, intersection.getBounds2D());
		}
		
		// can't collide if we're not moving
		if (aV.getX() == 0 && aV.getY() == 0 && bV.getX() == 0 && bV.getY() == 0) {
			return false;
		}
		
		// pending collision
		if (collisionPending(a, b) && (!lastCollision.containsKey(a) || lastCollision.get(a) != bHash)) {	
			collide(a, b);

			lastCollision.put(a, bHash);
			lastCollision.put(b, aHash);
			return true;
		}
		
		return false;
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
		double restitution = model.getTypeConfig(a.getType()).getCollisionCoefficient(b.getType());		
		
		Vector2d newVectorForA = new Vector2d(unitTangentVector);
		Vector2d newVectorForB = new Vector2d(unitTangentVector);
		
		newVectorForA.scale(aVector.dot(unitTangentVector));
		newVectorForB.scale(bVector.dot(unitTangentVector));
		
		// scaling factor in the form for inelastic collisions: https://en.wikipedia.org/wiki/Inelastic_collision		
		Vector2d aNorm = new Vector2d(unitNormalVector);			
		aNorm.scale(((bMass * restitution * (bNormalScaleFactor - aNormalScaleFactor)) + 
				                (aMass * aNormalScaleFactor) + (bMass * bNormalScaleFactor)) / (aMass + bMass));
		newVectorForA.add(aNorm);
		a.setMovementVector(newVectorForA);
		
		Vector2d bNorm = new Vector2d(unitNormalVector);			
		bNorm.scale(((aMass * restitution * (aNormalScaleFactor - bNormalScaleFactor)) + 
				                (bMass * bNormalScaleFactor) + (aMass * aNormalScaleFactor)) / (aMass + bMass));
		newVectorForB.add(bNorm);
		b.setMovementVector(newVectorForB);
	}
	
	/*
	 * Perform collision between object and side-wall
	 */
	private void collide(CanvasObject o, int wall) {
		double wallCoefficient = model.getTypeConfig(o.getType()).getCollisionCoefficient(Canvas.canvasObjectType);
		
		switch (wall) {
			case Canvas.WALL_EAST:
			case Canvas.WALL_WEST:
				o.getMovementVector().x *= -wallCoefficient;
				break;
				
			case Canvas.WALL_NORTH:
			case Canvas.WALL_SOUTH:
				o.getMovementVector().y *= -wallCoefficient;
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
	 * Check to see if we're moving through a hole in the canvas
	 */
	private boolean movingThroughCanvasHole(int wall, Point2D desired, Dimension size) {
		boolean inHole = false;
		Set<Tuple2i> holes = canvasHoles.get(wall);

		for (Tuple2i hole : holes) {
			int objectLocation = getCanvasObjectLocationForHole(wall, desired);
			
			if (objectLocation >= hole.x && objectLocation + getCanvasObjectSizeForHole(wall, size) <= hole.y) {
				inHole = true;
				break;
			}
		}
		
		return inHole;
	}
	
	/*
	 * Depending on which hole we are moving through, get either X or Y coordinate
	 */
	private int getCanvasObjectLocationForHole(int wall, Point2D desired) {
		double result;
		
		switch (wall) {
			case Canvas.WALL_NORTH:
			case Canvas.WALL_SOUTH:
				result = desired.getX();
				break;
				
			case Canvas.WALL_EAST:
			case Canvas.WALL_WEST:
				result = desired.getY();
				break;
				
			default:
				throw new IllegalArgumentException(String.format("Unknown wall type: %d", wall));
		}
		
		return (int)Math.round(result);
	}
	
	/*
	 * Depending on which hole we are moving through, get size in eitehr X or Y dimension
	 */
	private int getCanvasObjectSizeForHole(int wall, Dimension size) {
		int result;
		
		switch (wall) {
		case Canvas.WALL_NORTH:
		case Canvas.WALL_SOUTH:
			result = size.width;
			break;
			
		case Canvas.WALL_EAST:
		case Canvas.WALL_WEST:
			result = size.height;
			break;
			
		default:
			throw new IllegalArgumentException(String.format("Unknown wall type: %d", wall));
		}
		
		return result;
	}

	/*
	 * Clear collision grid
	 */
	private void clearCollisionNodes() {
		for (int i = 0; i < maxRegions; i++) {
			nodes[i].clear();
		}
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