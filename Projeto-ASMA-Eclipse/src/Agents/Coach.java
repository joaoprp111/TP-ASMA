package Agents;

import jade.core.Agent;

public class Coach extends Agent{
	private String teamID;
	
	protected void setup() {
		super.setup();
		
		Object[] args = getArguments();
		for (Object a : args) {
			teamID = a.toString();
		}
		
		System.out.println("Coach da equipa " + teamID + " ativo!...");
		/*this.addBehaviour(new Comprarprodutos(this, 1000));
		this.addBehaviour(new ReceberConfirmacao(this));*/
	}

	protected void takeDown() {
		super.takeDown();
	}
}
