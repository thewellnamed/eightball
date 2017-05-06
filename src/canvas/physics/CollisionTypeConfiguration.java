package canvas.physics;

/**
 * Defines a type of collision and coefficient of restitution
 * Aggregated by CanvasObjectConfiguration
 */
public class CollisionTypeConfiguration {
	protected CollisionType collisionType;
	protected double coefficient;
	protected CustomCollisionListener customAction;
	
	/**
	 * Constructor
	 * @param type Type of Collision
	 * @param coeff Coefficient of Restitution
	 */
	private CollisionTypeConfiguration(CollisionType type, double coeff, CustomCollisionListener actionListener) {
		collisionType = type;
		coefficient = coeff;
		customAction = actionListener;
	}
	
	public static CollisionTypeConfiguration bounce(double coeff) {
		return new CollisionTypeConfiguration(CollisionType.BOUNCE, coeff, null);
	}
	
	public static CollisionTypeConfiguration custom(CustomCollisionListener listener) {
		return new CollisionTypeConfiguration(CollisionType.CUSTOM, 0, listener);
	}
}
