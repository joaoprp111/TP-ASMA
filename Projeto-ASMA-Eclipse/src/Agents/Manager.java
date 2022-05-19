package Agents;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import Classes.Board;
import Classes.Decision;
import Classes.Position;
import Classes.VisionField;
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
	private int teamPlayCount = 0;
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
					else if(p == ACLMessage.REQUEST) {
						teamPlayCount++;
						//Pedido para mover jogadores
						Decision d = (Decision) msg.getContentObject();
						Map<AID,Position> destinations = d.getDestinations();
						//Verificar posicoes e casos de morte
						verify(destinations);
						changePlayingTeam();
						sendVisionFields(myAgent, currentPlayingTeam);
						//System.out.println("Enviei campos de visao ao coach " + currentPlayingTeam);
						if(teamPlayCount == 2) {
							teamPlayCount = 0;
							currentRound++;
							System.out.println("Ronda " + currentRound);
							System.out.print(board.toString());
							try {
								Thread.sleep(4000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
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
			System.out.println("Ronda " + currentRound);
			System.out.print(board.toString());
			try {
				Thread.sleep(15000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			changePlayingTeam();
			sendVisionFields(myAgent, currentPlayingTeam);
			System.out.println("Enviei campos de visao ao coach " + currentPlayingTeam);
			//System.out.print(currentPlayingTeam);
			//addBehaviour(new PrepareRoundBehaviour(myAgent, 1000));
		}
	}
	
	private void verify(Map<AID,Position> destinations) {
		//Verificar posicoes validas
		for(Entry<AID,Position> entry: destinations.entrySet()) {
			Position dest = entry.getValue();
			Map<AID,Position> currentPositions = board.getAllPositions();
			
			if(!currentPositions.values().contains(dest)) {
				//Pode mover
				board.setPosition(entry.getKey(), dest);
			}
			else {
				System.out.println("Nao pode mover para " + dest.toString());
			}
		}
		//Verificar caso de morte de algum jogador
		Map<AID,Position> updatedBoard = board.getAllPositions();
		List<AID> deadPlayers = new ArrayList<AID>();
		for(Entry<AID,Position> entry: updatedBoard.entrySet()) {
			AID playerId = entry.getKey();
			String playerTeam = playerId.getLocalName().substring("Player".length(), "Player".length()+1);
			Position playerPosition = entry.getValue();
			if(isDead(playerTeam,playerPosition)) {
				System.out.println("O jogador " + playerId.getLocalName() + " morreu!");
				deadPlayers.add(playerId);
			}
		}
		for(AID dead: deadPlayers)
			board.removePlayer(dead);
	}
	
	private String getPlayerTeam(AID a) {
		return a.getLocalName().substring("Player".length(), "Player".length()+1);
	}
	
	private boolean isDead(String team,Position p) {
		boolean dead = false;
		
		//Caso do meio
		int l = p.getPosX();
		int c = p.getPosY();
		Position pU = new Position(l-1,c);Position pUR = new Position(l-1,c+1);
		Position pR = new Position(l,c+1);Position pUL = new Position(l-1,c-1);
		Position pD = new Position(l+1,c);Position pDR = new Position(l+1,c-1);
		Position pL = new Position(l,c-1);Position pDL = new Position(l+1,c-1);
		
		if((board.getKey(pU) != null && !team.equals(getPlayerTeam(board.getKey(pU))) && board.hasValue(pU)
			&& board.getKey(pR) != null && !team.equals(getPlayerTeam(board.getKey(pR))) && board.hasValue(pR)
			&& board.getKey(pD) != null && !team.equals(getPlayerTeam(board.getKey(pD))) && board.hasValue(pD)
			&& board.getKey(pL) != null && !team.equals(getPlayerTeam(board.getKey(pL))) && board.hasValue(pL)) || 
			(board.getKey(pUR) != null && !team.equals(getPlayerTeam(board.getKey(pUR))) && board.hasValue(pUR)
			&& board.getKey(pUL) != null && !team.equals(getPlayerTeam(board.getKey(pUL))) && board.hasValue(pUL)
			&& board.getKey(pDR) != null && !team.equals(getPlayerTeam(board.getKey(pDR))) && board.hasValue(pDR)
			&& board.getKey(pDL) != null && !team.equals(getPlayerTeam(board.getKey(pDL))) && board.hasValue(pDL)))
				return true;
		
		return false;
	}
	
	private void changePlayingTeam() {
		Set<String> teamIds = teams.keySet();
		if(!gameStarted) {
			String[] arr = teamIds.toArray(new String[teamIds.size()]);
			Random r = new Random();
			int randNum = r.nextInt(teamIds.size());
			this.currentPlayingTeam = arr[randNum];
			gameStarted = true;
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
	
	private void sendVisionFields(Agent a, String team) {
		//Enviar os campos de visão de cada jogador para o respetivo treinador
		Map<AID,Position> playersPosition = board.getPositions(team);
		Map<AID,Map<AID,Position>> visionFields = board.getVisionFields();
		int offset = ("Player" + team).length();
		
		//Filtrar pela equipa 
		Map<AID,Map<AID, Position>> visionFieldsTeam = visionFields.entrySet().stream()
				.filter(map -> map.getKey().getLocalName()
						.substring(0,offset).equals("Player" + team))
				.collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue()));
		
		VisionField vf = new VisionField(visionFieldsTeam, playersPosition);
		
		/*for(Entry<AID,Map<AID,Position>> e: visionFieldsTeamA.entrySet()) {
			System.out.println(e.getKey().getLocalName());
			for(Entry<AID,Position> positions: e.getValue().entrySet()) {
				System.out.println("Jogador: " + positions.getKey().getLocalName() + " | Posicao: " + positions.getValue().toString());
			}
		}*/
		
		//Enviar para o coach
		try {
			// CoachID
			AID coach = new AID("Coach" + team, AID.ISLOCALNAME);
			
			// Crate message to notify manager of its presence
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(coach);
			msg.setContentObject(vf);
			a.send(msg);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
}
