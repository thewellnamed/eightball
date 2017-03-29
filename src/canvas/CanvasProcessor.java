package canvas;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Collection;

public interface CanvasProcessor {
	
	/**
	 * Initialize the processor
	 * @param canvas Bounding rectangle for wall collisions
	 * @param objectSize average object size of CanvasObjects to be processed
	 * @param objectCount average number of CanvasObjects to be processed
	 * @return success or failure
	 */
	public boolean initialize(Rectangle bounds, Dimension objectSize, int objectCount);
	
	/**
	 * Update position and movement vectors of CanvasObjects in Canvas
	 * @param objects Collection of CanvasObjects to process
	 * @return true if no object is currently moving, else false
	 */
	public boolean update(Collection<CanvasObject> objects);
	
	// TODO - Standard events...?
}
