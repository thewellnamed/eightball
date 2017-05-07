package eightball.events;

/**
 * Defines an event related to Game processing
 * See GameEventType
 */
public class GameEvent {
	public GameEventType type;
	public String text;
	
	/**
	 * Constructor
	 * @param eventType GameEventType of event
	 * @param message Event info
	 */
	public GameEvent(GameEventType eventType, String message) {
		type = eventType;
		text = message;
	}
}
