package eightball.events;

import eightball.BilliardBall;

public class TableEvent {
	public TableEventType type;
	public BilliardBall ball;
	
	public TableEvent(TableEventType eventType, BilliardBall billiardBall) {
		type = eventType;
		ball = billiardBall;
	}
}
