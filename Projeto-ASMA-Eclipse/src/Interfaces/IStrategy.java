package Interface;

import java.util.Map;

import Classes.Position;
import jade.core.AID;

public interface IStrategy {
	public Position attack(Map<AID,Map<AID, Position>> visionField, Map<AID,Position> teamPlayersPositions);
	public Position runAway(AID playerId, Map<AID, Position> visionField, Position playerPosition);
}
