package Agents;

import java.util.ArrayList;
import java.util.HashSet;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
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
		
		this.addBehaviour(new RegisterPlayers());
		/*this.addBehaviour(new ReceberConfirmacao(this));*/
	}

	protected void takeDown() {
		super.takeDown();
	}
	
	//Registering players as team members
	private class RegisterPlayers extends CyclicBehaviour{
		
		public void action() {
			ACLMessage msg = myAgent.receive();
			if (msg != null && msg.getPerformative() == ACLMessage.INFORM) {
				if (players.size() < maxTeamPlayers) {
					AID playerAID = msg.getSender();
					String player = playerAID.getLocalName();
					System.out.println("Eu, " + myAgent.getLocalName() + " , recebi mensagem do player " + player);
					players.add(playerAID);
				} else {
					System.out.println("Já tenho a minha equipa constituída, não posso aceitar mais jogadores!");
				}
			}
			block();
		}
	}
}
