package Agents;

import java.util.*;

import Classes.Board;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

public class Manager extends Agent {
	private static int N = 35;
	private static int NUMTEAMS = 2;
	private boolean gameStarted = false;
	private int currentRound = -1;
	private int prevRound = -1;
	private String currentPlayingTeam;
	private Board board; //O tabuleiro vai apresentar os jogadores por A1, B1, etc; Onde não estiver ninguém a ocupar posição fica string "-"
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
			currentRound = 0;
			prevRound = currentRound;
			gameStarted = true;
			addBehaviour(new PrepareRoundBehaviour(myAgent, 1000));
			//System.out.print(board.toString());
		}
	}
	
	private class PrepareRoundBehaviour extends TickerBehaviour{
		public PrepareRoundBehaviour(Agent a, long period) {
			super(a, period);
		}
		
		public void onTick() {
			if((currentRound == 0) || (currentRound > prevRound)) {
				prevRound = currentRound;
				changePlayingTeam();
				sendVisionFields();
			}
		}
	}
	
	private void changePlayingTeam() {
		Set<String> teamIds = teams.keySet();
		if(currentRound == 0) {
			String[] arr = teamIds.toArray(new String[teamIds.size()]);
			Random r = new Random();
			int randNum = r.nextInt(teamIds.size());
			this.currentPlayingTeam = arr[randNum];
		} else {
			Iterator<String> it = teamIds.iterator();
			while(it.hasNext()) {
				String team = it.next();
				if (!team.equals(this.currentPlayingTeam)) {
					this.currentPlayingTeam = team;
					break;
				}
			}
		}
	}
	
	private void sendVisionFields() {
		System.out.println("Falta enviar os campos de cada jogador para os respetivos treinadores!");
	}
	
}
