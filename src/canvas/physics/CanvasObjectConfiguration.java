package canvas.physics;

import java.util.HashMap;

/**
 * Physics model for a specific CanvasObject implementation
 * Model defines how to collide with other CanvasObject types (including the same type)
 */
public class CanvasObjectConfiguration {
	protected double frictionCoefficient;
	protected HashMap<String, CollisionTypeConfiguration> collisionConfig;
	protected CollisionType defaultCollisionType;
	protected double defaultCollisionCoefficient;
	
	/**
	 * Constructor
	 * @param collision Coefficient of Restitution for object type
	 * @param friction Coefficient of Friction for object type
	 * @param defCollisionType Default Collision Type, used when not overridden for a specific type.
	 */
	public CanvasObjectConfiguration(double collision, double friction, CollisionType defCollisionType) {
		defaultCollisionCoefficient = collision;
		defaultCollisionType = defCollisionType;
		frictionCoefficient = friction;
		collisionConfig = new HashMap<String, CollisionTypeConfiguration>();
	}
	
	/**
	 * Add a CollisionType for a specific CanvasObject class
	 * @param objectType Type of CanvasObject (CanvasObject.getType())
	 * @param collisionType (Collision Type)
	 */
	public void addCollisionConfig(String objectType, CollisionTypeConfiguration config) {
		collisionConfig.put(objectType, config);
	}
	
	/**
	 * Get Collision Type for a specific CanvasObject type
	 * @param objectType CanvasObject type
	 * @return CollisionType 
	 */
	public CollisionType getCollisionType(String objectType) {
		CollisionTypeConfiguration config = collisionConfig.get(objectType);
		if (config == null) {
			return defaultCollisionType;
		}
		
		return config.collisionType;
	}
	
	/**
	 * Coefficient of Restitution for collisions with a specified type of CanvasObject
	 * @param objectType Type of CanvasObject colliding with
	 * @return Coefficient of Restitution for collisions with objectType
	 */
	public double getCollisionCoefficient(String objectType) {
		CollisionTypeConfiguration config = collisionConfig.get(objectType);
		if (config == null) {
			return defaultCollisionCoefficient;
		}
		
		return config.coefficient;
	}
	
	/**
	 * Gets the custom action handler for collisions with the specified CanvasObject type
	 * @param objectType Type of CanvasObject colliding with
	 * @return CustomCollisionListener instance
	 */
	public CustomCollisionListener getCustomListener(String objectType) {
		CollisionTypeConfiguration config = collisionConfig.get(objectType);
		if (config == null) {
			return null;
		}
		
		return config.customAction;
	}
}
