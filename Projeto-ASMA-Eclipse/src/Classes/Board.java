package Classes;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import jade.core.AID;

public class Board {
	private String[][] board;
	private HashMap<AID, Position> playersPositions;
	private static int visionFieldN = 7;
	
	public Board(int size, HashMap<String, HashSet<AID>> teams) {
		String[][] b = createEmptyBoard(size);
		HashSet<Position> takenPositions = new HashSet<Position>();
		HashMap<AID, Position> startingPositions = new HashMap<AID, Position>();
		
		//Percorrer cada equipa
		for(Entry<String, HashSet<AID>> entry: teams.entrySet()) {
			HashSet<AID> players = entry.getValue();
			Iterator<AID> it = players.iterator();
			
			//Percorrer a lista de jogadores da equipa
			while(it.hasNext()) {
				AID playerAID = it.next();
				String player = playerAID.getLocalName();
				int offset = "Player".length();
				int finalOffset = player.length();
				String playerId = player.substring(offset, finalOffset);
				Position p = generateRandomPos(size);
				
				//Gerar nova posição aleatória para o jogador
				while (takenPositions.contains(p))
					p = generateRandomPos(size);
				
				//Atualizar as estruturas
				takenPositions.add(p);
				int startingPosX = p.getPosX();
				int startingPosY = p.getPosY();
				b[startingPosX][startingPosY] = playerId;
				startingPositions.put(playerAID, p);
			}
		}
		
		this.board = b;
		this.playersPositions = startingPositions;
	}
	
	public Map<AID,Map<AID,Position>> getVisionFields(){
		Map<AID, Map<AID,Position>> visionFields = new HashMap<AID,Map<AID,Position>>();
		
		for(Entry<AID,Position> e: playersPositions.entrySet()) {
			Position p = e.getValue();
			AID a = e.getKey();
			
			//Recolher todas as posições incluídas no campo de visão
			int posX = p.getPosX();
			int posY = p.getPosY();
			
			Map<AID,Position> visionField = playersPositions.entrySet().stream()
				.filter(map -> (!map.getValue().equals(p)) &&
						(map.getValue().getPosX() >= posX - (visionFieldN/2)) &&
						(map.getValue().getPosX() <= posX + (visionFieldN/2)) &&
						(map.getValue().getPosY() >= posY - (visionFieldN/2)) &&
						(map.getValue().getPosY() <= posY + (visionFieldN/2)))
							.collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue()));
			
			visionFields.put(a,visionField);
		}
		
		return visionFields;
	}
	
	private String[][] createEmptyBoard(int size){
		String[][] b = new String[size][size];
		for(int i = 0; i < size; i++)
			for(int j = 0; j < size; j++)
				b[i][j] = "-";
		return b;		
	}
	
	private Position generateRandomPos(int range) {
		Random r = new Random();
		int[] randomNumbers = r.ints(2,0,35).toArray();
		return new Position(randomNumbers[0], randomNumbers[1]);
	}
	
	@Override
	public String toString() {
		String result = "";
		for(int i = 0; i < board.length; i++) {
			for(int j = 0; j < board.length; j++)
				result += " | " + board[i][j];
			result += " |\n";
		}
		return result;
	}
}
