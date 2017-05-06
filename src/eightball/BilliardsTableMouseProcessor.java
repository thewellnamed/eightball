package eightball;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.vecmath.Vector2d;

import eightball.events.TableEvent;
import eightball.events.TableEventType;

/**
 * Manages mouse events and rendering for scratch cueball placement, cuestick, and shot selection state model
 */
public class BilliardsTableMouseProcessor implements MouseMotionListener, MouseListener
{	
	private Point2D position;
	private int state;
	private BilliardsTable table;
	private BilliardBall cueBall;
	private Timer animationTimer;
	private int shotPower;
	private boolean inShot;
	
	protected static final int STATE_NONE = 0;
	protected static final int STATE_SELECT_CUE_STICK_ANGLE = 1;
	protected static final int STATE_SELECT_CUE_STICK_POWER = 2;
	protected static final int STATE_PLACE_CUE_BALL = 3;
	protected static final int MAX_SHOT_POWER = 40;
	protected static final int MAX_SCRATCH_X_COORD = 200;
	
	public BilliardsTableMouseProcessor(BilliardsTable tbl, BilliardBall cue) {
		table = tbl;
		cueBall = cue;
		
		state = STATE_NONE;
		position = new Point2D.Double(-1, -1);
		animationTimer = new Timer(30, ae -> updateShotPower());
		shotPower = 1;
		
		table.addMouseListener(this);
		table.addMouseMotionListener(this);
		table.addEventListener(TableEventType.SHOT_BEGIN, e -> handleShotEvent(e));
		table.addEventListener(TableEventType.SHOT_ENDED, e -> handleShotEvent(e));
	}
	
	public void beginCueballPlacement() {
		Point loc = MouseInfo.getPointerInfo().getLocation();
		Dimension size = cueBall.getSize();
		
		SwingUtilities.convertPointFromScreen(loc, table);
		int x = (int)(loc.x - ((float)size.width/2F));
		int y = (int)(loc.y- ((float)size.height/2F));
		cueBall.setLocation(new Point2D.Double(x, y));
		state = STATE_PLACE_CUE_BALL;
	}
		
	public void render(Graphics2D g) {
		if (state == STATE_SELECT_CUE_STICK_ANGLE || state == STATE_SELECT_CUE_STICK_POWER) {
			Point2D cueLocation = cueBall.getCenterPoint();
			Vector2d cueStickNormal = getShotNormalVector();
			
			Vector2d cueStick = new Vector2d(cueStickNormal);
			cueStick.scale(75);
			cueStickNormal.scale(20);
			
			g.setColor(Color.BLACK);
			g.setStroke(new BasicStroke(5));
			g.drawLine((int)(cueLocation.getX() + cueStickNormal.getX()), (int)(cueLocation.getY() + cueStickNormal.getY()), 
					   (int)(cueLocation.getX() + cueStick.getX()), (int)(cueLocation.getY() + cueStick.getY()));
			
			if (state == STATE_SELECT_CUE_STICK_POWER) {
				g.setStroke(new BasicStroke(1));
				g.drawRect((int)cueLocation.getX() - 60, (int)cueLocation.getY() + 20, 40, 20);
				g.setColor(Color.RED);
				g.fillRect((int)cueLocation.getX() - 60, (int)cueLocation.getY() + 20, shotPower, 20);
			}
		}
		
		else if (state == STATE_PLACE_CUE_BALL) {
			Rectangle bounds = table.getCanvasBounds();
			g.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));
			g.setColor(Color.WHITE);
			g.drawLine(bounds.x + MAX_SCRATCH_X_COORD, bounds.y, bounds.x + MAX_SCRATCH_X_COORD, bounds.y + bounds.height);
		}
	}
	
	private void handleShotEvent(TableEvent e) {
		inShot = (e.type == TableEventType.SHOT_BEGIN);
	}
	
	public void mouseMoved(MouseEvent e) {
		if (state == STATE_SELECT_CUE_STICK_ANGLE) {
			position.setLocation(e.getX(), e.getY());
			table.repaint();
		}
		
		else if (state == STATE_PLACE_CUE_BALL) {
			Rectangle bounds = table.getCanvasBounds();
			Dimension size = cueBall.getSize();
			int x = (int)(e.getX() - ((float)size.width/2F));
			int y = (int)(e.getY() - ((float)size.height/2F));
			
			cueBall.setLocation(new Point2D.Double(x, y));
			table.repaint();
		}
	}
	
	// MouseListener implementation		
	public void mouseExited(MouseEvent e) {
		table.repaint();
	}
	
	public void mouseClicked(MouseEvent e) {
		if (inShot) return;
		
		switch (state) {
			case STATE_NONE:
				state = STATE_SELECT_CUE_STICK_ANGLE;
				mouseMoved(e);
				break;
				
			case STATE_SELECT_CUE_STICK_ANGLE:
				state = STATE_SELECT_CUE_STICK_POWER;
				shotPower = 1;
				animationTimer.start();
				break;
				
			case STATE_SELECT_CUE_STICK_POWER:
				// perform shot
				state = STATE_NONE;
				animationTimer.stop();
				
				Vector2d shotVector = getShotNormalVector();
				shotVector.scale(-shotPower);
				cueBall.setMovementVector(shotVector);
				table.repaint();
				table.start();
				break;
				
			case STATE_PLACE_CUE_BALL:
				Rectangle bounds = table.getCanvasBounds();
				Dimension size = cueBall.getSize();
				Point2D location = cueBall.getLocation();
				
				if (bounds.contains(location) && location.getX() + size.width <= bounds.x + MAX_SCRATCH_X_COORD) {
					state = STATE_NONE;
				} else {
					JOptionPane.showMessageDialog(table, "You must place the cueball within the bounds indicated by the dotted line");
				}
				table.repaint();
				break;
		}
	}
	
	// unused mouse event handlers
	public void mouseEntered(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseDragged(MouseEvent e) {}
	
	
	private void updateShotPower() {
		shotPower++;
		if (shotPower > MAX_SHOT_POWER)
			shotPower = 1;
		table.repaint();
	}
	
	private Vector2d getShotNormalVector() {
		Point2D cueLocation = cueBall.getCenterPoint();
		Vector2d cueStickNormal = new Vector2d(position.getX() - cueLocation.getX(), position.getY() - cueLocation.getY());
		cueStickNormal.normalize();
		return cueStickNormal;
	}
}