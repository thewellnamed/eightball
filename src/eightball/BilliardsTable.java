package eightball;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.vecmath.Vector2d;

import canvas.Canvas;
import canvas.CanvasObject;
import canvas.physics.*;
import eightball.enums.*;
import eightball.events.*;

/**
 * BilliardsTable 
 * Implements Canvas with a BasicPhysics model for collisions
 */
@SuppressWarnings("serial")
public class BilliardsTable extends Canvas 
{
	private BufferedImage background;
	private BilliardBall cueBall;
	private BilliardsTableUIProcessor uiProcessor;
	private boolean shotInProgress;
	private List<BilliardBall> captured;
	private Map<TableEventType, List<TableEventListener>> eventListeners;
	private boolean paused;
	
	private static final Color canvasColor = new Color(0x0, 0xCC, 0x33);
	
	// configuration constants for physics processor
	private static final int MAX_COLLISION_PASSES = 5;
	private static final double COR_BALL_COLLISIONS = 0.965; // coefficient of restitution: ball<-->ball
	private static final double COR_WALL_COLLISIONS = 0.74; // coefficient of restitution: ball-->rail
	private static final double COEFFICIENT_BALL_FRICTION = 0.98; // coefficient of friction: rolling ball
	
	/**
	 * Constructor
	 */
	public BilliardsTable() {
		setPreferredSize(new Dimension(900, 525));
		setCanvasBounds(new Rectangle(100, 89, 700, 351)); // 100,89 to 800,440
		setAnimationDelay(30); // 30 ms
		eventListeners = new HashMap<TableEventType, List<TableEventListener>>();
		
		initializeCanvasObjects();
		createPhysicsModel();
		
		// mouse and keyboard events
		// cue stick and scratch handling via mouse
		uiProcessor = new BilliardsTableUIProcessor(this);
						
		// load background
		try {
			// image adapted from source here:
			// http://www.bezzmedia.com/swfspot/tutorials/flash8/8_Ball_Pool
			background = ImageIO.read(new File("resources/table.png"));	
		} catch (IOException e) {
			System.out.println("Unable to load background...");
		}
	}
		
	/**
	 * Begin play
	 */
	public void begin() {
		unpause();
		uiProcessor.beginCueballPlacement();
		repaint();
	}
	
	/**
	 * Reset table
	 */
	public void reset() {
		stop();
		clear();
		initializeCanvasObjects();
		uiProcessor.reset();
		uiProcessor.setCueBall(cueBall);
		shotInProgress = false;
		repaint();
	}
	
	/**
	 * Request pause
	 */
	public void requestPause() {
		fireTableEvent(TableEventType.REQUEST_PAUSE);
	}
	
	/**
	 * Execute pause
	 */
	public void pause() {
		paused = true;
		stop();
		repaint();
	}
	
	/**
	 * Unpause
	 */
	public void unpause() {
		paused = false;
		
		if (shotInProgress) {
			start();
		} else {
			repaint();
		}
	}
	
	/**
	 * Are we paused?
	 * @return boolean
	 */
	public boolean isPaused() {
		return paused;
	}
	
	/**
	 * Attach a TableEvent listener
	 * @param type TableEventType
	 * @param listener TableEventListener
	 */
	public void addEventListener(TableEventType type, TableEventListener listener) {
		if (!eventListeners.containsKey(type)) {
			ArrayList<TableEventListener> listeners = new ArrayList<TableEventListener>();
			listeners.add(listener);
			eventListeners.put(type, listeners);
		} else {
			eventListeners.get(type).add(listener);
		}
	}
	
	/**
	 * Remove a TableEvent listener
	 * @param type TableEvent type
	 * @param listener TableEventListener
	 */
	public void removeEventListener(TableEventType type, TableEventListener listener) {
		if (eventListeners.containsKey(type)) {
			eventListeners.get(type).remove(listener);
		}
	}
	
	/**
	 * Get the cue ball
	 * @return BilliardBall (BallType.CUE)
	 */
	public BilliardBall getCueBall() {
		return cueBall;
	}
	
	/**
	 * Render table, balls, cue stick
	 */
	@Override
	public void paintComponent(Graphics g2d) {
		// re-render background only if needed
		Graphics2D g = (Graphics2D) g2d;
		
		Rectangle clip = g.getClipBounds();
		if (clip == null || clip.x == 0 || clip.y == 0) {
			g.drawImage(background, 0,  0,  null);
		}
				
		// render CanvasObjects
		g.setColor(canvasColor);
		g.fillRect(canvasBounds.x, canvasBounds.y, canvasBounds.width, canvasBounds.height);
		super.paintComponent(g);
		
		if (!paused && !shotInProgress && uiProcessor != null) {
			uiProcessor.render(g);
		}
		
		if (paused) {
			g.setColor(new Color(0, 0, 0, 0.4F));
			g.fillRect(0, 0, getWidth(), getHeight());
		}
	}
	
	/**
	 * Process ball movement
	 */
	@Override
	protected void update() {
		shotInProgress = processor.update(objects);
		if (!shotInProgress) {
			stop();

			if (captured.size() > 0) {
				for (BilliardBall ball : captured) {
					fireTableEvent(TableEventType.BALL_CAPTURED, ball);
					
					if (ball.getDefinition().getType() == BallType.CUE) {
						cueBall.setMovementVector(new Vector2d(0, 0));
						cueBall.setSuspended(false);
						uiProcessor.beginCueballPlacement();
						fireTableEvent(TableEventType.CUE_BALL_PLACEMENT_BEGIN, cueBall);
					} else {
						remove(ball);
					}
				}
			}
			
			captured.clear();
			fireTableEvent(TableEventType.SHOT_ENDED);
		}
		
		repaint(canvasBounds);
	}	
	
	/**
	 * Start animation timer
	 */
	@Override
	public void start() {
		super.start();
		fireTableEvent(TableEventType.SHOT_BEGIN);
	}
	
	/*
	 * Create the BasicPhysicsModel
	 * and create a BasicPhysicsCanvasProcessor for the table
	 */
	private void createPhysicsModel() {
		BasicPhysicsModel model = new BasicPhysicsModel();
		model.setMaxCollisionPasses(MAX_COLLISION_PASSES);
		
		// Billiard Ball model
		CanvasObjectConfiguration ballConfig = new CanvasObjectConfiguration(COR_BALL_COLLISIONS, COEFFICIENT_BALL_FRICTION, CollisionType.BOUNCE);
		ballConfig.addCollisionConfig(BilliardBall.canvasObjectType, CollisionTypeConfiguration.bounce(COR_BALL_COLLISIONS));
		ballConfig.addCollisionConfig(Canvas.canvasObjectType, CollisionTypeConfiguration.bounce(COR_WALL_COLLISIONS));
		ballConfig.addCollisionConfig(Pocket.canvasObjectType, CollisionTypeConfiguration.custom((a,b) -> checkAndProcessPocketCollision(a, b)));
		model.addTypeConfig(BilliardBall.canvasObjectType, ballConfig);
		
		// Pocket model
		CanvasObjectConfiguration pocketConfig = new CanvasObjectConfiguration(0, 0, CollisionType.NONE);
		pocketConfig.addCollisionConfig(BilliardBall.canvasObjectType, CollisionTypeConfiguration.custom((a,b) -> checkAndProcessPocketCollision(a, b)));
		model.addTypeConfig(Pocket.canvasObjectType, pocketConfig);
		
		BasicPhysicsCanvasProcessor processor = new BasicPhysicsCanvasProcessor(model);
		processor.initialize(canvasBounds, canvasHoles, BilliardBall.ballSize, 16);
		setProcessor(processor);
	}
	
	/*
	 * Creates the pockets and billiard balls at initial positions
	 */
	private void initializeCanvasObjects() {
		// Pockets
		for (int i = 0; i < Pocket.NUMBER_POCKETS; i++) {
			Pocket p = new Pocket(i, canvasBounds);
			p.setCanvasHoles(canvasHoles);
			add(p);
		}
		
		// Cue ball
		BilliardBall cue = new BilliardBall(BallDefinition.CUE);
		cue.setLocation(new Point2D.Double(180, 260));
		cue.setMovementVector(new Vector2d(32, -1));
		cueBall = cue;
		add(cue);
		
		// Standard Balls
		int baseX = 560;
		int baseY = 200;
		int[] offsetX = { 0, 0, 26, 26, 52, 52, 52, 78, 78, 78, 78, 104, 104, 104, 104, 104};
		int[] offsetY = { 0, 50, 38, 64, 25, 51, 76, 12, 38, 64, 90, 0, 26, 52, 78, 104 };
		
		for (int i = 1; i < 16; i++) {
			BilliardBall b = new BilliardBall(BallDefinition.valueOf(i));
			b.setLocation(new Point2D.Double(baseX + offsetX[i], baseY + offsetY[i]));
			add(b);
		}
		
		// Initialize collection for balls captured during shot
		captured = new ArrayList<BilliardBall>();
	}
	
	/*
	 * Used to process collisions between a Pocket and a ball, i.e to sink a ball
	 */
	private boolean checkAndProcessPocketCollision(CanvasObject a, CanvasObject b) {
		BilliardBall ball;
		Pocket pocket;
		
		if (a instanceof BilliardBall && b instanceof Pocket) {
			ball = (BilliardBall) a;
			pocket = (Pocket) b;
		} else if (a instanceof Pocket && b instanceof BilliardBall) {
			ball = (BilliardBall) b;
			pocket = (Pocket) a;
		} else {
			return false;
		}
		
		Point2D ballCenter = ball.getCenterPoint();
		if (pocket.getArea().contains(ballCenter)) {
			Point2D pocketLocation = pocket.getLocation();
			
			ball.setLocation(new Point2D.Double(pocketLocation.getX(), pocketLocation.getY()));
			ball.setSuspended(true);
			captured.add(ball);
		} 
		
		return false;
	}
	
	/*
	 * Fire table event
	 */
	protected void fireTableEvent(TableEventType type) {
		fireTableEvent(type, null);
	}
	
	/*
	 * Fire table event
	 */
	protected void fireTableEvent(TableEventType type, BilliardBall b) {
		for (TableEventListener listener : eventListeners.get(type)) {
			listener.fire(new TableEvent(type, b));
		}
	}
}
