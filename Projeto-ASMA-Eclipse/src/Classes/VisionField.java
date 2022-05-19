package Classes;

import java.util.Map;
import java.util.Map.Entry;

import jade.core.AID;
import jade.util.leap.Serializable;

public class VisionField implements Serializable {
	private Map<AID,Map<AID, Position>> visionField;
	private Map<AID,Position> playersPosition;
	
	public VisionField(Map<AID,Map<AID, Position>> visionField, Map<AID, Position> playersPosition) {
		this.visionField = visionField;
		this.playersPosition = playersPosition;
	}
	
	public Map<AID,Map<AID, Position>> getVisionField(){
		return this.visionField;
	}
	
	public Map<AID,Position> getPlayersPosition(){
		return this.playersPosition;
	}
	
	@Override
	public String toString() {
		String result = "";
		for(Entry<AID,Map<AID,Position>> e: visionField.entrySet()) {
			result += "Jogador: " + e.getKey().getLocalName() + " | Campo de visão: ";
			for(Entry<AID,Position> positions: e.getValue().entrySet()) {
				result += positions.getKey().getLocalName() + " - Posicao: " + positions.getValue().toString() + " | ";
			}
			result += "\n";
		}
		return result;
	}
}
