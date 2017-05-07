package eightball.events;

public class GameEvent {
	public GameEventType type;
	public String text;
	
	public GameEvent(GameEventType eventType, String message) {
		type = eventType;
		text = message;
	}
}
