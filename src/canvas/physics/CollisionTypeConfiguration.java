package canvas.physics;

public class CollisionTypeConfiguration {
	protected CollisionType collisionType;
	protected double coefficient;
	
	public CollisionTypeConfiguration(CollisionType type, double coeff) {
		collisionType = type;
		coefficient = coeff;
	}
}
