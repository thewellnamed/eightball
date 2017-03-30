package eightball.enums;

import java.awt.Color;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

public enum BallDefinition {	
	// Ball Definitions
	CUE(0, BallType.CUE, Color.WHITE),
	
	ONE(1, BallType.SOLID, Color.YELLOW), 
	TWO(2, BallType.SOLID, Color.BLUE), 
	THREE(3, BallType.SOLID, Color.RED), 
	FOUR(4, BallType.SOLID, new Color(0x80, 0x00, 0x80)), // purple
	FIVE(5, BallType.SOLID, Color.ORANGE), 
	SIX(6, BallType.SOLID, Color.GREEN), 
	SEVEN(7, BallType.SOLID, Color.MAGENTA), 
	
	EIGHT(8, BallType.EIGHTBALL, Color.BLACK), 
	
	NINE(9, BallType.STRIPE, Color.YELLOW), 
	TEN(10, BallType.STRIPE, Color.BLUE), 
	ELEVEN(11, BallType.STRIPE, Color.RED), 
	TWELVE(12, BallType.STRIPE, new Color(0x80, 0x00, 0x80)), 
	THIRTEEN(13, BallType.STRIPE, Color.ORANGE), 
	FOURTEEN(14, BallType.STRIPE, Color.GREEN), 
	FIFTEEN(15, BallType.STRIPE, Color.MAGENTA);
	
	private int id;
	private BallType type;
	private Color color;
	
	private static Map<Integer, BallDefinition> typeMap = new HashMap<Integer, BallDefinition>();
	
    static {
        for (BallDefinition b : BallDefinition.values()) {
            typeMap.put(b.id, b);
        }
    }
	
	private BallDefinition(int number, BallType t, Color c) {
		id = number;
		type = t;
		color = c;
	}
	
	public static BallDefinition valueOf(int id) {
		BallDefinition b = typeMap.get(id);
		
		if (b == null) {
			throw new InvalidParameterException("Requested unknown billiard ball");
		}
		
		return b;
	}
	
	public int getNumber() {
		return id;
	}
	
	public Color getColor() {
		return color;
	}
	
	public BallType getType() {
		return type;
	}
}
