package canvas.physics;

import java.util.HashMap;

public class BasicPhysicsModel {
	protected HashMap<String, CanvasTypeConfiguration> typeConfig;
	protected double wallCollisionCoefficient;
	protected int maxCollisionPasses;
	
	public BasicPhysicsModel() {
		typeConfig = new HashMap<String, CanvasTypeConfiguration>();
		wallCollisionCoefficient = 1;
		maxCollisionPasses = 1;
	}
	
	public void addTypeConfig(String type, CanvasTypeConfiguration config) {
		typeConfig.put(type,  config);
	}
	
	public CanvasTypeConfiguration getTypeConfig(String type) {
		return typeConfig.get(type);
	}
	
	public double getWallCollisionCoefficient() {
		return wallCollisionCoefficient;
	}
	
	public void setWallCollisionCoefficient(double value) {
		wallCollisionCoefficient = value;
	}
	
	public int getMaxCollisionPasses() {
		return maxCollisionPasses;
	}
	
	public void setMaxCollisionPasses(int value) {
		maxCollisionPasses = value;
	}
}
