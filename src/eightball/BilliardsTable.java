package eightball;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.vecmath.Vector2d;

import canvas.Canvas;
import canvas.physics.*;
import eightball.enums.BallType;

/**
 * BilliardsTable 
 * Implements Canvas with a BasicPhysics model for collisions
 * Manages UI for player shot selection
 */
@SuppressWarnings("serial")
public class BilliardsTable extends Canvas {
	private BufferedImage background;
	private MouseMotionListener mmListener;
	private Point2D cursorPosition;
	private BilliardBall cueBall;
	
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
		setSize(900, 525);
		setCanvasBounds(new Rectangle(100, 89, 700, 351));
		setAnimationDelay(30); // 30 ms
		
		// BasicPhysics model
		BasicPhysicsModel model = new BasicPhysicsModel();
		model.setMaxCollisionPasses(MAX_COLLISION_PASSES);
		
		// Billiard Ball model
		// TODO: We will *not* bounce off pockets...
		CanvasObjectConfiguration ballConfig = new CanvasObjectConfiguration(COR_BALL_COLLISIONS, COEFFICIENT_BALL_FRICTION, CollisionType.BOUNCE);
		ballConfig.addCollisionConfig(BilliardBall.canvasObjectType, new CollisionTypeConfiguration(CollisionType.BOUNCE, COR_BALL_COLLISIONS));
		ballConfig.addCollisionConfig(Canvas.canvasObjectType, new CollisionTypeConfiguration(CollisionType.BOUNCE, COR_WALL_COLLISIONS));
		model.addTypeConfig(BilliardBall.canvasObjectType, ballConfig);
		
		// TODO: Pocket model...
		
		BasicPhysicsCanvasProcessor processor = new BasicPhysicsCanvasProcessor(model);
		processor.initialize(canvasBounds, BilliardBall.ballSize, 16);
		setProcessor(processor);
		
		// Mouse Motion Listener
		cursorPosition = new Point2D.Double(-1, -1);
		mmListener = new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent e) {
				cursorPosition.setLocation(e.getX(), e.getY());
				repaint();
			}
			
			public void mouseDragged(MouseEvent e) {}
		};
		addMouseMotionListener(mmListener);
		
		// load background
		try {
			background = ImageIO.read(new File("resources/table.png"));	
		} catch (IOException e) {
			System.out.println("Unable to load background...");
		}
	}
	
	public void add(BilliardBall b) {
		if (b.getDefinition().getType() == BallType.CUE) {
			cueBall = b;
		}
		
		super.add(b);
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
		
		if (cueBall != null && cursorPosition.getX() >= 0) {
			Point2D cueLocation = cueBall.getCenterPoint();
			Vector2d cueStickNormal = new Vector2d(cursorPosition.getX() - cueLocation.getX(), cursorPosition.getY() - cueLocation.getY());
			cueStickNormal.normalize();
			
			Vector2d cueStick = new Vector2d(cueStickNormal);
			cueStick.scale(75);
			cueStickNormal.scale(20);
			
			g.setColor(Color.BLACK);
			g.setStroke(new BasicStroke(5));
			g.drawLine((int)(cueLocation.getX() + cueStickNormal.getX()), (int)(cueLocation.getY() + cueStickNormal.getY()), 
					   (int)(cueLocation.getX() + cueStick.getX()), (int)(cueLocation.getY() + cueStick.getY()));
		}
	}
	
	/**
	 * Process ball movement
	 */
	@Override
	protected void update() {
		// TODO
		// pass result of update() to Game to indicate end of a shot...
		
		processor.update(objects);
		repaint(canvasBounds);
	}
}
