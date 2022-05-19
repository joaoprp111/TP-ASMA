package Interface;

import java.util.Map;

import Classes.Decision;
import Classes.Position;
import jade.core.AID;

public interface IStrategy {
	public Map<AID,Position> attack(Map<AID,Map<AID, Position>> visionFields, Map<AID,Position> teamPlayersPositions, Map<AID,String> states,
			Map<AID,Position> destinations);
	public Position runAway(AID playerId, Map<AID, Position> visionField, Position playerPosition);
}
