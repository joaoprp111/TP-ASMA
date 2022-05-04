package Behaviours;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class NotifyCoach extends OneShotBehaviour{
	private String team;
	
	public NotifyCoach(String team) {
		this.team = team; // A or B
	}
	
	public void action() {
			
		//Get player name
		String agentname = myAgent.getLocalName();
		
		// Set the coach ID
		AID coach = new AID("Coach" + this.team, AID.ISLOCALNAME);
		
		// Crate message to notify coach of its presence
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.addReceiver(coach);
		msg.setContent("Eu sou o player " + agentname);
		myAgent.send(msg);

	}
}
