package canvas.physics;

import java.util.HashMap;

/**
 * BasicPhysicsModel contains constants used by BasicPhysicsCanvasProcessor to process collisions
 *
 */
public class BasicPhysicsModel {
	protected HashMap<String, CanvasObjectConfiguration> typeConfig;
	protected int maxCollisionPasses;
	
	/**
	 * Constructor
	 */
	public BasicPhysicsModel() {
		typeConfig = new HashMap<String, CanvasObjectConfiguration>();
		maxCollisionPasses = 1;
	}
	
	/**
	 * Add a new CanvasObject type to model
	 * @param type CanvasObject type (CanvasObject.getType())
	 * @param config Configuration for CanvasObject type (see CanvasTypeConfiguration)
	 */
	public void addTypeConfig(String type, CanvasObjectConfiguration config) {
		typeConfig.put(type,  config);
	}
	
	/**
	 * Get Config for a given CanvasObject type
	 * @param type CanvasObject type
	 * @return CanvasTypeConfiguration
	 */
	public CanvasObjectConfiguration getTypeConfig(String type) {
		return typeConfig.get(type);
	}
	
	public int getMaxCollisionPasses() {
		return maxCollisionPasses;
	}
	
	public void setMaxCollisionPasses(int value) {
		maxCollisionPasses = value;
	}
}
