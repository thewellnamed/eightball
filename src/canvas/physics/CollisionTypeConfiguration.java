package canvas.physics;

/**
 * Defines a type of collision and coefficient of restitution
 * Aggregated by CanvasObjectConfiguration
 */
public class CollisionTypeConfiguration {
	protected CollisionType collisionType;
	protected double coefficient;
	
	/**
	 * Constructor
	 * @param type Type of Collision
	 * @param coeff Coefficient of Restitution
	 */
	public CollisionTypeConfiguration(CollisionType type, double coeff) {
		collisionType = type;
		coefficient = coeff;
	}
}
