package canvas.physics;

import canvas.CanvasObject;

/**
 * Defines an event handler for a custom collision between to CanvasObjects
 */
public interface CustomCollisionListener {
	public boolean checkCollision(CanvasObject a, CanvasObject b);
}
