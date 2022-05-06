package Classes;

import java.util.*;
import java.util.Map.Entry;

import jade.core.AID;

public class Board {
	private String[][] board;
	private HashMap<String, Position> playersPositions;
	
	public Board(int size, HashMap<String, HashSet<AID>> teams) {
		String[][] b = createEmptyBoard(size);
		HashSet<Position> takenPositions = new HashSet<Position>();
		HashMap<String, Position> startingPositions = new HashMap<String, Position>();
		
		//Percorrer cada equipa
		for(Entry<String, HashSet<AID>> entry: teams.entrySet()) {
			HashSet<AID> players = entry.getValue();
			Iterator<AID> it = players.iterator();
			
			//Percorrer a lista de jogadores da equipa
			while(it.hasNext()) {
				String player = it.next().getLocalName();
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
				startingPositions.put(playerId, p);
			}
		}
		
		this.board = b;
		this.playersPositions = startingPositions;
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
