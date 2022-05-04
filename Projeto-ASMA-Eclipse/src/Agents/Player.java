package Agents;

import jade.core.Agent;

public class Player extends Agent {
	private String id;

	protected void setup() {
		super.setup();
		
		Object[] args = getArguments();
		for (Object a : args) {
			id = a.toString();
		}
		
		System.out.println("Player " + id + " ativo!...");
		/*this.addBehaviour(new Comprarprodutos(this, 1000));
		this.addBehaviour(new ReceberConfirmacao(this));*/
	}

	protected void takeDown() {
		super.takeDown();
	}
	
}
