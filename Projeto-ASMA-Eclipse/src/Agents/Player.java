package Agents;

import java.util.Map;

import Classes.Decision;
import Classes.Position;
import Classes.VisionField;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class Player extends Agent {
	private String id;
	private String team;

	protected void setup() {
		super.setup();
		
		Object[] args = getArguments();
		for (Object a : args) {
			this.id = a.toString();
			this.team = this.id.substring(0, 1);
		}
		
		System.out.println("Player " + id + " ativo!...");
		// Implementar um one shot behaviour para informar o seu coach de que é um jogador
		this.addBehaviour(new NotifyCoachBehaviour());
		/*this.addBehaviour(new Comprarprodutos(this, 1000));
		this.addBehaviour(new ReceberConfirmacao(this));*/
	}

	protected void takeDown() {
		super.takeDown();
	}
	
	private class NotifyCoachBehaviour extends OneShotBehaviour{

		
		public void action() {
				
			//Get player name
			String agentname = myAgent.getLocalName();
			
			// Set the coach ID
			AID coach = new AID("Coach" + team, AID.ISLOCALNAME);
			
			// Crate message to notify coach of its presence
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(coach);
			msg.setContent("Eu sou o player " + agentname);
			myAgent.send(msg);

		}
	}
	
	private class ListeningBehaviour extends CyclicBehaviour{
		
		public void action() {
			ACLMessage msg = receive();
			if (msg != null && msg.getPerformative() == ACLMessage.INFORM) {
				try {
					String s = msg.getContent();
					if (s == "You are dead") {
						takeDown();
					}
				}
				catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
					
			}
		}
	}
	
}
