package eightball.events;

import eightball.BilliardBall;

/**
 * Events related to BilliardsTable processing
 */
public class TableEvent {
	public TableEventType type;
	public BilliardBall ball;
	
	/**
	 * Constructor
	 * @param eventType TableEvent type
	 * @param billiardBall ball (may be null)
	 */
	public TableEvent(TableEventType eventType, BilliardBall billiardBall) {
		type = eventType;
		ball = billiardBall;
	}
}
