package Agents;

import java.util.*;

import Classes.Board;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class Manager extends Agent {
	private static int N = 35;
	private static int NUMTEAMS = 2;
	//O tabuleiro vai apresentar os jogadores por A1, B1, etc
	//Onde não estiver ninguém a ocupar posição fica string vazia
	private Board board;
	private boolean knowsAllPlayers = false;
	private HashMap<String,HashSet<AID>> teams = new HashMap<String,HashSet<AID>>();

	protected void setup() {
		super.setup();
		System.out.println("Manager ativo!...");
		this.addBehaviour(new ListeningBehaviour());
		/*this.addBehaviour(new Comprarprodutos(this, 1000));
		this.addBehaviour(new ReceberConfirmacao(this));*/
	}

	protected void takeDown() {
		super.takeDown();
	}
	
	private class ListeningBehaviour extends CyclicBehaviour {
		public void action() {
			try {
				ACLMessage msg = myAgent.receive();
				if (msg != null) {
					int p = msg.getPerformative();
					if (p == ACLMessage.INFORM && !knowsAllPlayers) {
						HashSet<AID> players = (HashSet<AID>) msg.getContentObject();
						Iterator<AID> i = players.iterator();
						if (i.hasNext()) {
							AID anyPlayerId = i.next();
							String playerName = anyPlayerId.getLocalName();
							int offset = "Player".length();
							String playerTeam = playerName.substring(offset, offset + 1);
							System.out.println("Manager: Recebi os players da equipa " + playerTeam + "!");
							teams.put(playerTeam, players);
							if (teams.size() == NUMTEAMS) {
								knowsAllPlayers = true;
								myAgent.addBehaviour(new InitializeGameBehaviour());
							}
						}
					}
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private class InitializeGameBehaviour extends OneShotBehaviour{
		
		// Criar o tabuleiro e inicializá-lo com posições aleatórias para cada jogador
		public void action() {
			board = new Board(N, teams);
			System.out.println("Manager: Os jogadores ja estao prontos a jogar!");
			//System.out.print(board.toString());
		}
	}
	
}
