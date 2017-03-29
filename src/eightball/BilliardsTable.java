package eightball;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import canvas.Canvas;
import canvas.physics.*;

@SuppressWarnings("serial")
public class BilliardsTable extends Canvas {
	private BufferedImage background;
	private static final Color canvasColor = new Color(0x0, 0xCC, 0x33);
	
	// configuration constants for physics processor
	private static final int MAX_COLLISION_PASSES = 5;
	private static final double COR_BALL_COLLISIONS = 0.965; // coefficient of restitution: ball<-->ball
	private static final double COR_WALL_COLLISIONS = 0.74; // coefficient of restitution: ball-->rail
	private static final double COEFFICIENT_BALL_FRICTION = 0.98; // coefficient of friction: rolling ball
	
	public BilliardsTable() {
		setSize(900, 525);
		setCanvasBounds(new Rectangle(100, 89, 700, 351));
		
		// BasicPhysics model
		BasicPhysicsModel model = new BasicPhysicsModel();
		model.setMaxCollisionPasses(MAX_COLLISION_PASSES);
		
		// Billiard Ball model
		// TODO: We will *not* bounce off pockets...
		CanvasObjectConfiguration ballConfig = new CanvasObjectConfiguration(COR_BALL_COLLISIONS, COEFFICIENT_BALL_FRICTION, CollisionType.Bounce);
		ballConfig.addCollisionConfig(BilliardBall.canvasObjectType, new CollisionTypeConfiguration(CollisionType.Bounce, COR_BALL_COLLISIONS));
		ballConfig.addCollisionConfig(Canvas.canvasObjectType, new CollisionTypeConfiguration(CollisionType.Bounce, COR_WALL_COLLISIONS));
		model.addTypeConfig(BilliardBall.canvasObjectType, ballConfig);
		
		// TODO: Pocket model...
		
		BasicPhysicsCanvasProcessor processor = new BasicPhysicsCanvasProcessor(model);
		processor.initialize(canvasBounds, new Dimension(25, 25), 16);
		setProcessor(processor);
		
		// load background
		try {
			background = ImageIO.read(new File("resources/table.png"));	
		} catch (IOException e) {
			System.out.println("Unable to load background...");
		}
	}
	
	@Override
	public void paintComponent(Graphics g) {
		// re-render background only if needed
		Rectangle clip = g.getClipBounds();
		if (clip == null || clip.x == 0 || clip.y == 0) {
			g.drawImage(background, 0,  0,  null);
		}
				
		// render CanvasObjects
		g.setColor(canvasColor);
		g.fillRect(canvasBounds.x, canvasBounds.y, canvasBounds.width, canvasBounds.height);
		super.paintComponent(g);
	}
	
	@Override
	protected void update() {
		// TODO
		// pass result of update() to Game to indicate end of a shot...
		
		processor.update(objects);
		repaint(canvasBounds);
	}
}
