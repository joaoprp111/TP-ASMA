package Classes;

import java.util.*;

import Interface.IStrategy;
import jade.core.AID;

public class AttackAndDefenseStrategy implements IStrategy {
	//1 jogador -> se só tiver inimigos no campo de visão, foge.
	// se o número de amigos + 1 for >= nº inimigos, ataca.

	@Override
	public Position attack(Map<AID,Map<AID, Position>> visionField, Map<AID,Position> teamPlayersPositions) {
		// TODO Auto-generated method stub
		return null;
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
