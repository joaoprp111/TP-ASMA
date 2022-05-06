package Agents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
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
		
		this.addBehaviour(new WaitForPlayersBehaviour());
		/*this.addBehaviour(new ReceberConfirmacao(this));*/
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
}
