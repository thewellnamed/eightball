package eightball;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eightball.enums.BallType;
import eightball.events.*;

/**
 * Manage Game state, including status messages and game information
 * Processes events from BilliardsTable
 */
public class Game {
	private BilliardsTable table;
	private Map<GameEventType, List<GameEventListener>> eventListeners;
	private boolean gameInProgress;
	private boolean haveWinner;
	private boolean singlePlayer;
	private boolean firstPlayersTurn;
	private boolean scratch;
	private boolean eightballCaptured;
	private BallType playerOneType;
	private BallType playerTwoType;
	private List<BilliardBall> capturedThisTurn;
	private Map<BallType, Set<BilliardBall>> captured;
	private int score;
	
	/**
	 * Constructor
	 */
	public Game() {
		eventListeners = new HashMap<GameEventType, List<GameEventListener>>();
		capturedThisTurn = new ArrayList<BilliardBall>();
		captured = new HashMap<BallType, Set<BilliardBall>>();
		
		table = new BilliardsTable();
		table.addEventListener(TableEventType.CUE_BALL_PLACEMENT_BEGIN, e -> onCueBallPlacementBegin(e));
		table.addEventListener(TableEventType.CUE_BALL_PLACEMENT_END, e -> onCueBallPlacementEnd(e));
		table.addEventListener(TableEventType.SHOT_BEGIN, e -> onShotBegin(e));
		table.addEventListener(TableEventType.BALL_CAPTURED, e -> onBallCaptured(e));
		table.addEventListener(TableEventType.SHOT_ENDED, e -> onShotEnd(e));
		table.addEventListener(TableEventType.REQUEST_PAUSE, e -> onRequestPause());
	}
		
	/**
	 * Get BilliardsTable for Game
	 * @return BilliardsTable
	 */
	public BilliardsTable getTable() {
		return table;
	}
	
	/**
	 * Game in progress?
	 * @return T/F
	 */
	public boolean inProgress() {
		return gameInProgress;
	}
	
	/**
	 * Has someone won this game?
	 * @return T/F
	 */
	public boolean haveWinner() {
		return haveWinner;
	}
	
	/**
	 * Reset existing game
	 */
	public void reset() {
		gameInProgress = false;
		haveWinner = false;
		table.reset();
	}
	
	/**
	 * Begin a new game
	 * @param players Number of players (1, 2)
	 */
	public void begin(int players) {
		gameInProgress = true;
		firstPlayersTurn = true;
		singlePlayer = (players == 1);
		playerOneType = BallType.NONE;
		playerTwoType = BallType.NONE;
		score = 0;
		captured.clear();
		scratch = false;
		eightballCaptured = false;
		
		updateStatusMessage("New Game! Place the cue ball to begin...");
		setGameInfo();
		table.begin();
	}
	
	/**
	 * Pause game
	 */
	public void pause() {
		table.pause();
	}
	
	/**
	 * Unpause game
	 */
	public void unpause() {
		table.unpause();
	}
	
	/**
	 * Attach GameEventListener
	 * @param type GameEventType
	 * @param listener GameEventListener
	 */
	public void addEventListener(GameEventType type, GameEventListener listener) {
		if (!eventListeners.containsKey(type)) {
			ArrayList<GameEventListener> listeners = new ArrayList<GameEventListener>();
			listeners.add(listener);
			eventListeners.put(type, listeners);
		} else {
			eventListeners.get(type).add(listener);
		}
	}
	
	/**
	 * Remove event listener
	 * @param type GameEventType
	 * @param listener GameEventListener
	 */
	public void removeEventListener(GameEventType type, GameEventListener listener) {
		if (eventListeners.containsKey(type)) {
			eventListeners.get(type).remove(listener);
		}
	}	
	
	// GameEvent helpers...
	private void updateStatusMessage(String message) {
		fireGameEvent(GameEventType.STATUS_MESSAGE, message);
	}
	
	private void updateGameInfo(String info) {
		fireGameEvent(GameEventType.INFO_MESSAGE, info);
	}
	
	private void fireGameEvent(GameEventType type) {
		fireGameEvent(type, null);
	}
	
	private void fireGameEvent(GameEventType type, String message) {
		for (GameEventListener listener : eventListeners.get(type)) {
			listener.fire(new GameEvent(type, message));
		}
	}
	
	// TableEvent callbacks
	private void onRequestPause() {
		fireGameEvent(GameEventType.REQUEST_PAUSE);
	}
	
	private void onCueBallPlacementBegin(TableEvent e) {
		updateStatusMessage("Scratch! Place the cueball to continue...");
	}
	
	private void onCueBallPlacementEnd(TableEvent e) {
		updateStatusMessage("Click the table to shoot...");
	}
	
	private void onShotBegin(TableEvent e) {
		updateStatusMessage("Shot in progress...");
		capturedThisTurn.clear();
		scratch = false;
		eightballCaptured = false;
	}
	
	private void onBallCaptured(TableEvent e) {
		BallType type = e.ball.getDefinition().getType();
		
		switch (type) {
			case CUE:
				scratch = true;
				break;
				
			case SOLID:
			case STRIPE:
				capturedThisTurn.add(e.ball);
				break;
				
			case EIGHTBALL:
				if (singlePlayer) {
					capturedThisTurn.add(e.ball);
				} else {
					eightballCaptured = true;
				}
				break;
				
			default:
				break;
		}
	}	
	
	private void onShotEnd(TableEvent e) {
		if (!singlePlayer) {			
			// determine which player owns which balls..
			if (playerOneType == BallType.NONE && capturedThisTurn.size() > 0) {
				BilliardBall firstCaptured = capturedThisTurn.get(0);
				
				if (firstPlayersTurn) {
					playerOneType = firstCaptured.getDefinition().getType();
					playerTwoType = (playerOneType == BallType.SOLID ? BallType.STRIPE : BallType.SOLID);
				} else {
					playerTwoType = firstCaptured.getDefinition().getType();
					playerOneType = (playerTwoType == BallType.SOLID ? BallType.STRIPE : BallType.SOLID);	
				}
			}
			
			// process shot
			boolean success = false;	
			ArrayList<String> myCaptures = new ArrayList<String>();
			BallType myType = firstPlayersTurn ? playerOneType : playerTwoType;
			
			if (capturedThisTurn.size() > 0) {
				for (BilliardBall b : capturedThisTurn) {
					BallType t = b.getDefinition().getType();
					if (t == myType) {
						myCaptures.add(String.format("(%d)", b.getDefinition().getNumber()));
						if (!scratch) {
							success = true;
						}
					}
					capture(b);
				}
			}
			
			// if eightball captured, the acting player wins as long as all of their other balls are sunk
			// at the end of the same turn
			if (eightballCaptured) {
				haveWinner = true;
				
				if (!captured.containsKey(myType) || captured.get(myType).size() != 7) {
					updateStatusMessage(String.format("Eightball sunk too early! %s wins!",
							(firstPlayersTurn ? "Player Two" : "Player One")));
				} else {
					updateStatusMessage(String.format("%s wins!",
							(firstPlayersTurn ? "Player One" : "Player Two")));
				}
				fireGameEvent(GameEventType.GAME_OVER);
			} else {			
				if (success) {	
					updateStatusMessage(String.format("%s captured: %s! Continue shooting...", 
							(firstPlayersTurn ? "P1" : "P2"), String.join(",", myCaptures)));
				} else {
					firstPlayersTurn = !firstPlayersTurn;
					if (!scratch) {
						updateStatusMessage(String.format("Now Player %s turn...", (firstPlayersTurn ? "One's" : "Two's")));
					}
				}
			}
		} else {
			ArrayList<String> myCaptures = new ArrayList<String>();
			
			if (capturedThisTurn.size() > 0) {
				for (BilliardBall b : capturedThisTurn) {
					myCaptures.add(String.format("(%d)", b.getDefinition().getNumber()));
				}
				
				updateStatusMessage(String.format("captured: %s", String.join(",", myCaptures)));
			} else {
				updateStatusMessage("Click table to shoot...");
			}
			
			score += 100 * capturedThisTurn.size();
		}
		
		// update game info
		setGameInfo();
	}
	
	private void capture(BilliardBall b) {
		BallType type = b.getDefinition().getType();
		
		if (!captured.containsKey(type)) {
			HashSet<BilliardBall> set = new HashSet<BilliardBall>();
			set.add(b);
			captured.put(type, set);
		} else {
			captured.get(type).add(b);
		}
	}
	
	private void setGameInfo() {
		if (singlePlayer) {
			updateGameInfo("Score: " + score);
		} else {
			String ballTypes = "";
			String pOneType = "n/a";
			String pTwoType = "n/a";
			
			if (playerOneType != BallType.NONE) {
				if (playerOneType == BallType.SOLID) {
					pOneType = "Solids";
					pTwoType = "Stripes";
				} else {
					pOneType = "Stripes";
					pTwoType = "Solids";
				}
				
				ballTypes = String.format("P1: %s, P2: %s. ", pOneType, pTwoType);
			}
			
			updateGameInfo(String.format("%sCurrently Playing: %s",
					ballTypes, (firstPlayersTurn ? "P1" : "P2")));
		}
	}
}
