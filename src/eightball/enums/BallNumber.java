package eightball.enums;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

public enum BallNumber {	
	// Ball Definitions
	CUE(0, BallType.CUE),
	
	ONE(1, BallType.SOLID), 
	TWO(2, BallType.SOLID), 
	THREE(3, BallType.SOLID), 
	FOUR(4, BallType.SOLID), 
	FIVE(5, BallType.SOLID), 
	SIX(6, BallType.SOLID), 
	SEVEN(7, BallType.SOLID), 
	
	EIGHT(8, BallType.EIGHTBALL), 
	
	NINE(9, BallType.STRIPE), 
	TEN(10, BallType.STRIPE), 
	ELEVEN(11, BallType.STRIPE), 
	TWELVE(12, BallType.STRIPE), 
	THIRTEEN(13, BallType.STRIPE), 
	FOURTEEN(14, BallType.STRIPE), 
	FIFTEEN(15, BallType.STRIPE);
	
	private int id;
	private BallType type;
	private static Map<Integer, BallNumber> typeMap = new HashMap<Integer, BallNumber>();
	
    static {
        for (BallNumber b : BallNumber.values()) {
            typeMap.put(b.id, b);
        }
    }
	
	private BallNumber(int number, BallType t) {
		id = number;
		type = t;
	}
	
	public static BallNumber valueOf(int id) {
		BallNumber b = typeMap.get(id);
		
		if (b == null) {
			throw new InvalidParameterException("Requested unknown billiard ball");
		}
		
		return b;
	}
	
	public int getNumber() {
		return id;
	}
	
	public BallType getType() {
		return type;
	}
}
