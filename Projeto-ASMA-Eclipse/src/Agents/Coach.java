package Agents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import Classes.Position;
import Classes.VisionField;
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
					System.out.println("Recebi os campos de visao");
					System.out.print(vf.toString());
					Map<AID,Position> playersDestinations = makeDecision(vf);
					if(playersDestinations.keySet().size() == 0) {
						System.out.print("Nao tem nada");
					}
					for(Entry<AID,Position> entry: playersDestinations.entrySet()) {
						System.out.print("Jogador " + entry.getKey().getLocalName() + " | Destino: (" +
					entry.getValue().getPosX() + "," + entry.getValue().getPosY() + ")");
					}
				} catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
					
			}
		}
	}
	
	private Map<AID,Position> makeDecision(VisionField vf) {
		Map<AID,Map<AID, Position>> visionField = vf.getVisionField();
		Map<AID,Position> teamPlayersPositions = vf.getPlayersPosition();
		Map<AID,Position> destinations = new HashMap<AID,Position>();
		boolean noEnemies = true;
		
		//Verificar se nenhum player tem visão de um inimigo
		for(AID player: visionField.keySet()) {
			String playerTeam = player.getLocalName().substring("Player".length(), "Player".length()+1);
			if (visionField.get(player) != null) {
				Map<AID,Position> m = visionField.get(player);
				for(AID a: m.keySet()) {
					if(!a.getLocalName().substring("Player".length(),"Player".length()+1).equals(playerTeam)) {
						noEnemies = false;
						break;
					}
				}
			}
		}
		
		if (noEnemies) {
			System.out.print("No enemies");
			//Mover todos os players para o centro
			for(Entry<AID, Position> entry: teamPlayersPositions.entrySet()) {
				Position p = entry.getValue();
				Position destination = calculateDestinationNoEnemies(p);
				destinations.put(entry.getKey(), destination);
			}
		}
		
		return destinations;
	}
	
	private Position calculateDestinationNoEnemies(Position p) {
		Position dest;
		int l = p.getPosX();
		int c = p.getPosY();
		
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
