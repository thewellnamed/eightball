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

@SuppressWarnings("serial")
public class BilliardsTable extends Canvas {
	
	private BufferedImage background;
	private static final Color canvasColor = new Color(0x0, 0xCC, 0x33);
	
	public BilliardsTable() {
		super(new BilliardsCanvasProcessor());
		setSize(900, 525);
		
		try {
			background = ImageIO.read(new File("resources/table.png"));	
		} catch (IOException e) {
			System.out.println("Unable to load background...");
		}
		
		canvasBounds = new Rectangle(100, 89, 700, 351);
		processor.initialize(canvasBounds, new Dimension(30, 30), 16);
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
		processor.update(objects);
		repaint(canvasBounds);
	}
}
