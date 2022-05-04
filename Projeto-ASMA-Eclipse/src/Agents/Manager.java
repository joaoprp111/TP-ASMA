package Agents;

import jade.core.Agent;

public class Manager extends Agent {

	protected void setup() {
		super.setup();
		System.out.println("Manager ativo!...");
		/*this.addBehaviour(new Comprarprodutos(this, 1000));
		this.addBehaviour(new ReceberConfirmacao(this));*/
	}

	protected void takeDown() {
		super.takeDown();
	}
	
}
