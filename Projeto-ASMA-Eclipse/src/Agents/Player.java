package Agents;

import Behaviours.NotifyCoach;
import jade.core.Agent;

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
		this.addBehaviour(new NotifyCoach(team));
		/*this.addBehaviour(new Comprarprodutos(this, 1000));
		this.addBehaviour(new ReceberConfirmacao(this));*/
	}

	protected void takeDown() {
		super.takeDown();
	}
	
}
