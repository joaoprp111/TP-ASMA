package Agents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import Classes.AttackAndDefenseStrategy;
import Classes.Decision;
import Classes.Position;
import Classes.VisionField;
import Interface.IStrategy;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.core.AID;

public class Coach extends Agent{
	private String team;
	private int maxTeamPlayers;
	private HashSet<AID> players = new HashSet<AID>();
	private IStrategy s = new AttackAndDefenseStrategy();
	
	protected void setup() {
		super.setup();
		
		Object[] args = getArguments();
		this.team = args[0].toString();
		this.maxTeamPlayers = Integer.parseInt(args[1].toString());
		
		System.out.println("Coach da equipa " + team + " ativo!...");
		
		this.addBehaviour(new WaitForPlayersBehaviour());
		this.addBehaviour(new ListeningBehaviour());
	}

	protected void takeDown() {
		super.takeDown();
	}
	
	//Registering players as team members
	private class WaitForPlayersBehaviour extends SimpleBehaviour{
		
		public void action() {
			while(players.size() < maxTeamPlayers) {
				ACLMessage msg = myAgent.receive();
				if (msg != null && msg.getPerformative() == ACLMessage.INFORM) {
						AID playerAID = msg.getSender();
						String player = playerAID.getLocalName();
						System.out.println("Eu, " + myAgent.getLocalName() + " , recebi mensagem do player " + player);
						players.add(playerAID);
				}
				block();
			}
			System.out.println("Coach " + team + ": Ja tenho a minha equipa completa!");
			myAgent.addBehaviour(new SendPlayersToManagerBehaviour());
			done();
		}
		
		@Override
		public boolean done() {
			return true;
		}
	}
	
	//Só pode enviar quando tiver todos os jogadores armazenados no HashSet
	private class SendPlayersToManagerBehaviour extends OneShotBehaviour{
		
		public void action() {
			try {
				// Set the Manager ID
				AID manager = new AID("Manager", AID.ISLOCALNAME);
				
				// Crate message to notify manager of its presence
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(manager);
				msg.setContentObject(players);
				myAgent.send(msg);
				System.out.println("Coach " + team + " : Enviei os jogadores ao manager!");
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private class ListeningBehaviour extends CyclicBehaviour{
		
		public void action() {
			ACLMessage msg = receive();
			if (msg != null && msg.getPerformative() == ACLMessage.INFORM) {
				try {
					VisionField vf = (VisionField) msg.getContentObject();
					//System.out.println("Recebi os campos de visao");
					System.out.print(vf.toString());
					Map<AID,Position> playersDestinations = makeDecision(vf);
					//System.out.println("Destinos da equipa" + team + ": " + playersDestinations.size());
					Decision d = new Decision(playersDestinations);
					if(playersDestinations.size() > 0) {
						ACLMessage resp = msg.createReply();
						//System.out.print(d.toString());
						resp.setContentObject(d);
						resp.setPerformative(ACLMessage.REQUEST);
						myAgent.send(resp);
					}
				}
				catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
					
			}
		}
	}
	
	private Map<AID,Position> makeDecision(VisionField vf) {
		Map<AID,Map<AID, Position>> visionFields = vf.getVisionField();
		Map<AID,Position> teamPlayersPositions = vf.getPlayersPosition();
		Map<AID,String> playerStates = new HashMap<AID,String>();
		Map<AID,Position> destinations = new HashMap<AID,Position>();
		boolean noEnemies = true;
		int enemyCount = 0;
		int friendCount = 0;
		
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
			int disadvantage = enemyCount - (friendCount + 1);
			int advantage = (friendCount + 1) - enemyCount;
			//Escolher o melhor
			if(disadvantage > 0) {
				//Foge
				Position playerPos = teamPlayersPositions.get(playerId);
				Position destination = s.runAway(playerId, entry.getValue(), playerPos);
				destinations.put(playerId, destination);
				playerStates.put(playerId, "Defense");
			} else if(enemyCount > 0 && advantage >= 0) {
				//Atacar
				playerStates.put(playerId, "Attack");
			} else {
				//Ir para o centro
				Position p = teamPlayersPositions.get(playerId);
				Position destination = calculateDestinationNoEnemies(p);
				destinations.put(playerId, destination);
				playerStates.put(playerId, "GoCenter");
			}
		}
		
		//Preparar ataque se for preciso
		destinations = s.attack(visionFields, teamPlayersPositions, playerStates, destinations);
		
		return destinations;
	}
	
	private Position calculateDestinationNoEnemies(Position p) {
		
		List<Position> pos = calculateValidPositions(p);
		Position destination = getClosestPosition(pos);
		
		return destination;
	}
	
	private Position getClosestPosition(List<Position> pos) {
		double dist = 1000;
		Position best = new Position(-1,-1);
		for(Position p: pos) {
			int l = p.getPosX();
			int c = p.getPosY();
			int lCenter = 17;
			int cCenter = 17;
			
			double newDist = Math.sqrt((cCenter - c) * (cCenter - c) + (lCenter - l) * (lCenter - l));
			if(newDist < dist) {
				dist = newDist;
				best = p;
			}
		}
		
		return best;
	}
	
	private List<Position> calculateValidPositions(Position p) {
		List<Position> list = new ArrayList<Position>();
		int l = p.getPosX();
		int c = p.getPosY();
		
		for(int i = l-1; i < l+2; i++) {
			for(int j = c-1; j < c+2; j++) {
				Position p1 = new Position(i,j);
				if(!p1.equals(p))
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
