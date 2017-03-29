package canvas.physics;

import java.util.HashMap;

/**
 * 
 *
 */
public class CanvasTypeConfiguration {
	protected double collisionCoefficient;
	protected double frictionCoefficient;
	protected HashMap<String, CollisionType> collisionConfig;
	protected CollisionType defaultCollisionType;
	
	public CanvasTypeConfiguration(double collision, double friction, CollisionType defCollisionType) {
		collisionCoefficient = collision;
		frictionCoefficient = friction;
		defaultCollisionType = defCollisionType;
		collisionConfig = new HashMap<String, CollisionType>();
	}
	
	public void addCollisionConfig(String objectType, CollisionType collisionType) {
		collisionConfig.put(objectType, collisionType);
	}
	
	public CollisionType getCollisionType(String objectType) {
		CollisionType type = collisionConfig.get(objectType);
		if (type == null) {
			return defaultCollisionType;
		}
		
		return type;
	}
}
