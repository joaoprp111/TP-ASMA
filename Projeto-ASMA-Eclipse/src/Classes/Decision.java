package Classes;

import java.util.Map;
import java.util.Map.Entry;

import jade.core.AID;
import jade.util.leap.Serializable;

public class Decision implements Serializable {
	Map<AID,Position> destinations;
	
	public Decision(Map<AID,Position> destinations) {
		this.destinations = destinations;
	}
	
	public Map<AID,Position> getDestinations(){
		return this.destinations;
	}
	
	@Override
	public String toString() {
		String result = "";
		for(Entry<AID,Position> entry: destinations.entrySet()) {
			System.out.print("Jogador " + entry.getKey().getLocalName() + " | Destino: (" +
					entry.getValue().getPosX() + "," + entry.getValue().getPosY() + ")");
		}
		return result;
	}
}
