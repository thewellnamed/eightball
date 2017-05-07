package eightball.events;

/**
 * Functional interface for GameEvent listeners
 */
public interface GameEventListener {
	public void fire(GameEvent e);
}
