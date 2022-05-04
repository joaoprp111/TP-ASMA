package Agents;

import jade.core.Agent;
import Classes.Board;

public class Manager extends Agent {
	private Board board;
	private int teamPlayingID;
	
	protected void setup() {
		super.setup();

//		Object[] args = getArguments();
//		for (Object a : args) {
//			available_containers.add(a.toString());
//		}

		// this.addBehaviour(new updateState(this));
		System.out.println("Manager ativo...");
	}
	
	protected void takeDown() {
		super.takeDown();
	}
}
