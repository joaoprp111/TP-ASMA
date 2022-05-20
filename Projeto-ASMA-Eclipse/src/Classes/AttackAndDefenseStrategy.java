package Classes;

import java.util.*;
import java.util.Map.Entry;

import Interface.IStrategy;
import jade.core.AID;

public class AttackAndDefenseStrategy implements IStrategy {
	//1 jogador -> se só tiver inimigos no campo de visão, foge.
	// se o número de amigos + 1 for >= nº inimigos, ataca.

	@Override
	public Map<AID,Position> attack(Map<AID,Map<AID, Position>> visionFields, Map<AID,Position> teamPlayersPositions, Map<AID,String> states,
			Map<AID,Position> destinations) {
		// TODO Auto-generated method stub
		//Por default fica com o primeiro campo
		Map<AID,Position> dests = destinations;
		Map<AID,Position> selectedVf = new HashMap<AID, Position>();
		String currentTeam = teamPlayersPositions.keySet().iterator()
				.next().getLocalName().substring("Player".length(), "Player".length()+1);
		
		//Escolher o primeiro campo com estado de ataque
		for(Entry<AID,Map<AID,Position>> e: visionFields.entrySet()) {
			AID playerId = e.getKey();
			if(states.get(playerId) == "Attack") {
				selectedVf = e.getValue();
				//System.out.println("Campo do jogador  " + e.getKey().getLocalName() + " escolhido para atacar!");
				break;
			}
		}
		
		int enemyCount = 0;
		int friendCount = 0;
		int bestAdvantage = -100;
		
		//Percorrer cada campo
		for(Entry<AID,Map<AID,Position>> entry: visionFields.entrySet()) {
			enemyCount = 0;
			friendCount = 0;
			AID playerId = entry.getKey();
			String playerTeam = playerId.getLocalName().substring("Player".length(), "Player".length()+1);
			//Calcular critério de vantagem (nº de amigos - nº de inimigos)
			for(AID vfPlayer: entry.getValue().keySet()) {
				String vfPlayerTeam = vfPlayer.getLocalName().substring("Player".length(), "Player".length()+1);
				//Comparar as equipas
				if(!playerTeam.equals(vfPlayerTeam))
					enemyCount++;
				else
					friendCount++;
			}
			int advantage = (friendCount + 1) - enemyCount;
			//Escolher o melhor
			if(enemyCount > 0 && advantage > bestAdvantage) {
				bestAdvantage = advantage;
				selectedVf = entry.getValue();
				//System.out.println("Campo do jogador  " + entry.getKey().getLocalName() + " escolhido para atacar! (2)");
			}
		}
		
		//Mandar os amigos atacar um inimigo do campo escolhido (se tiverem o estado atacar ou ir para o centro)
		if(selectedVf.size() > 0) {
			//System.out.println("Tamanho do campo: " + selectedVf.size());
			Position target = null;
			
			for(Entry<AID,Position> e: selectedVf.entrySet()) {
				//Se é de uma equipa diferente
				if(!e.getKey().getLocalName().substring("Player".length(), "Player".length()+1).equals(currentTeam)) {
					target = e.getValue();
					break;
				}
			}
			//System.out.println("Posicao escolhida para atacar: " + target.toString());
			
			//Calcular os destinos de ataque dos jogadores da equipa
			for(Entry<AID,Position> entry: teamPlayersPositions.entrySet()) {
				AID playerId = entry.getKey();
				if(states.get(playerId) == "Attack" || states.get(playerId) == "GoCenter") {
					//System.out.println("Vou criar um destino!");
					Position playerPosition = entry.getValue();
					Position destination = calculateDestination(playerPosition,target,selectedVf.values());
					if(dests.containsKey(playerId))
						dests.replace(playerId,destination);
					else
						dests.put(playerId, destination);
				}
			}
		}
		
		return dests;
	}

	@Override
	public Position runAway(AID playerId, Map<AID, Position> visionField, Position playerPosition) {
		// TODO Auto-generated method stub
		// Verificar se o jogador só tem inimigos no campo de visão
	
		//Tem de fugir
		List<Position> enemiesPositions = new ArrayList<Position>(visionField.values());
		List<Position> validPositions = calculateValidPositions(playerPosition, enemiesPositions);
		Position dest = selectMove(playerPosition, validPositions, enemiesPositions);
		
		return dest;
	}
	
	private Position selectMove(Position playerPosition, List<Position> validMoves, List<Position> enemiesPositions) {
		// Caso não se decida mover retorna a própria posição
		Position res = playerPosition;
		
		for(Position pos: validMoves) {
			if(!isInEnemyLineOrColumn(pos, enemiesPositions) && !isNearEnemy(pos, enemiesPositions)) {
				res = pos;
				break;
			}
		}
		
		return res;
	}
	
	private boolean isInEnemyLineOrColumn(Position validPos, List<Position> enemiesPos) {
		boolean res = false;
		
		for(Position pos: enemiesPos) {
			int validL = validPos.getPosX();
			int validC = validPos.getPosY();
			int enemyL = pos.getPosX();
			int enemyC = pos.getPosY();
			
			if(validL == enemyL || validC == enemyC) {
				res = true;
				break;
			}
		}
		
		return res;
	}
	
	private boolean isNearEnemy(Position validPos, List<Position> enemiesPos) {
		boolean res = false;
		
		for(Position enemyPos: enemiesPos) {
			if(dist(validPos, enemyPos) == 1) {
				res = true;
				break;
			}
		}
		
		return res;
	}
	
	private int dist(Position p1, Position p2) {
		int p1L = p1.getPosX();
		int p1C = p1.getPosY();
		int p2L = p2.getPosX();
		int p2C = p2.getPosY();
		
		return (int) Math.sqrt((p2C - p1C) * (p2C - p1C) + (p2L - p1L) * (p2L - p1L));
	}
	
	private Position calculateDestination(Position p, Position target,Collection<Position> playersPositions) {
		
		List<Position> pos = calculateValidPositionsAttack(p, playersPositions);
		Position destination = getClosestPosition(p,pos,target);
		
		return destination;
	}
	
	private Position getClosestPosition(Position playerPos,List<Position> pos, Position targetPos) {
		double dist = 1000;
		Position best = playerPos;
		for(Position p: pos) {
			int l = p.getPosX();
			int c = p.getPosY();
			int lDest = targetPos.getPosX();
			int cDest = targetPos.getPosY();
			
			double newDist = Math.sqrt((cDest - c) * (cDest - c) + (lDest - l) * (lDest - l));
			if(newDist > 0 && newDist < dist) {
				dist = newDist;
				best = p;
			}
		}
		
		return best;
	}
	
	private List<Position> calculateValidPositionsAttack(Position p, Collection<Position> playersPositions) {
		List<Position> list = new ArrayList<Position>();
		int l = p.getPosX();
		int c = p.getPosY();
		
		for(int i = l-1; i < l+2; i++) {
			for(int j = c-1; j < c+2; j++) {
				Position p1 = new Position(i,j);
				if(!p1.equals(p) && !playersPositions.contains(p1))
					list.add(p1);
			}
		}
		
		return list.stream().filter(position -> isValid(position)).toList();
	}
	
	private List<Position> calculateValidPositions(Position p, List<Position> enemiesPositions) {
		List<Position> list = new ArrayList<Position>();
		int l = p.getPosX();
		int c = p.getPosY();
		
		for(int i = l-1; i < l+2; i++) {
			for(int j = c-1; j < c+2; j++) {
				Position p1 = new Position(i,j);
				if(!p1.equals(p) && !enemiesPositions.contains(p1))
					list.add(p1);
			}
		}
		
		return list.stream().filter(position -> isValid(position)).toList();
	}
	
	private boolean isValid(Position p) {
		int l = p.getPosX();
		int c = p.getPosY();
		
		return l >= 0 && l <= 34 && c >= 0 && c <= 34;
	}

}
