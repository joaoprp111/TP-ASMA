package Agents;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import Classes.Board;
import Classes.Decision;
import Classes.Position;
import Classes.Report;
import Classes.VisionField;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.ShutdownPlatform;
import jade.lang.acl.ACLMessage;
import jade.wrapper.ControllerException;

public class Manager extends Agent {
	private static int N = 35;
	private static int MAXROUND = 150;
	private static int NUMTEAMS = 2;
	private boolean gameStarted = false;
	private int currentRound = -1;
	private int teamPlayCount = 0;
	private String currentPlayingTeam;
	private String startingTeam;
	int end = 0;
	private String winner = "";
	private String loser = "";
	private int numPlayersWinner = 0;
	private int numPlayersLoser = 0;
	private Board board; //O tabuleiro vai apresentar os jogadores por A1, B1, etc; Onde não estiver ninguém a ocupar posição fica string "-"
	private boolean knowsAllPlayers = false;
	private HashMap<String,HashSet<AID>> teams = new HashMap<String,HashSet<AID>>();
	private Report r;

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
			if(end == 1) { //acabou
				System.out.println("O jogo terminou...");
				r = new Report(winner, loser, numPlayersWinner, numPlayersLoser,currentRound);
				System.out.println("Equipa que comecou a jogar: " + startingTeam);
				System.out.print(r.toString());
				end = 2;
			} else if(end == 0) { //Não acabou
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
							List<AID> deadPlayers = verify(destinations);
							//Remover os mortos do sistema
							for(AID dead: deadPlayers) {
								String playerTeam = dead.getLocalName().substring("Player".length(), "Player".length()+1);
								board.removePlayer(dead);
								HashSet<AID> teamPlayers = teams.get(playerTeam);
								teamPlayers.remove(dead);
								teams.replace(playerTeam,teamPlayers);
								
								// Avisar o agente player de que morreu
								ACLMessage resp = new ACLMessage(ACLMessage.INFORM);
								msg.addReceiver(dead);
								resp.setContent("You are dead");
								myAgent.send(resp);
							}
							
							checkEndOfGame();
							if(end != 1) {
								changePlayingTeam();
								sendVisionFields(myAgent, currentPlayingTeam);
								//System.out.println("Enviei campos de visao ao coach " + currentPlayingTeam);
								if(teamPlayCount == 2) {
									teamPlayCount = 0;
									currentRound++;
									System.out.println("Ronda " + currentRound);
									System.out.print(board.toString());
									/*try {
										Thread.sleep(4000);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}*/
								}
							}
						}
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
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
			startingTeam = currentPlayingTeam;
			sendVisionFields(myAgent, currentPlayingTeam);
			System.out.println("Enviei campos de visao ao coach " + currentPlayingTeam);
			//System.out.print(currentPlayingTeam);
			//addBehaviour(new PrepareRoundBehaviour(myAgent, 1000));
		}
	}
	
	private String getWinner() {
		String winner = "";
		int maxPlayers = -1;
		for(Entry<String,HashSet<AID>> entry: teams.entrySet()) {
			if(entry.getValue().size() > maxPlayers) {
				maxPlayers = entry.getValue().size();
				winner = entry.getKey();
			}
		}
		
		numPlayersWinner = maxPlayers;
		
		return winner;
	}
	
	private String getLoser() {
		String loser = "";
		int minPlayers = 1000;
		for(Entry<String,HashSet<AID>> entry: teams.entrySet()) {
			if(entry.getValue().size() < minPlayers) {
				minPlayers = entry.getValue().size();
				loser = entry.getKey();
			}
		}
		
		numPlayersLoser = minPlayers;
		
		return loser;
	}
	
	private void checkEndOfGame() {
		boolean end = false;
		
		for(Entry<String,HashSet<AID>> entry: teams.entrySet()) {
			if(entry.getValue().size() == 0) {
				end = true;
				break;
			}
		}
		
		if(end || currentRound == MAXROUND) {
			//Acabou o jogo
			//System.out.println("O jogo terminou...");
			winner = getWinner();
			loser = getLoser();
			this.end = 1;
			//System.out.println("O vencedor é: " + winner);
		}
	}
	
	private List<AID> verify(Map<AID,Position> destinations) {
		//Verificar posicoes validas
		for(Entry<AID,Position> entry: destinations.entrySet()) {
			Position dest = entry.getValue();
			Map<AID,Position> currentPositions = board.getAllPositions();
			
			if(!currentPositions.values().contains(dest)) {
				//Pode mover
				board.setPosition(entry.getKey(), dest);
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
		
		return deadPlayers;
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
		
		// Cantos
		//Canto (0,0)
		Position p01 = new Position(0,1);
		Position p10 = new Position(1,0);
		Position p11 = new Position(1,1);
		//Canto (0,34)
		Position p033 = new Position(0,33);
		Position p134 = new Position(1,34);
		Position p133 = new Position(1,33);
		//Canto (34,34)
		Position p3334 = new Position(33,34);
		Position p3433 = new Position(34,33);
		Position p3333 = new Position(33,33);
		//Canto (34,0)
		Position p330 = new Position(33,0);
		Position p341 = new Position(34,1);
		Position p331 = new Position(33,1);
		
		//Bordas
		//Borda de cima
		Position pSupBoardLeft = new Position(0,c-1);
		Position pSupBoardRight = new Position(0,c+1);
		Position pSupBoardDown = new Position(l+1,c);
		Position pSupBoardDiagLeft = new Position(l+1,c-1);
		Position pSupBoardDiagRight = new Position(l+1,c+1);
		//Borda da direita
		Position pRightBoardSup = new Position(l-1,34);
		Position pRightBoardDown = new Position(l+1,34);
		Position pRightBoardLeft = new Position(l,c-1);
		Position pRightBoardDiagSup = new Position(l-1,c-1);
		Position pRightBoardDiagDown = new Position(l+1,c-1);
		//Borda de baixo
		Position pDownBoardRight = new Position(34,c+1);
		Position pDownBoardLeft = new Position(34,c-1);
		Position pDownBoardSup = new Position(l-1,c);
		Position pDownBoardDiagLeft = new Position(l-1,c-1);
		Position pDownBoardDiagRight = new Position(l-1,c+1);
		//Borda da esquerda
		Position pLeftBoardSup = new Position(l-1,0);
		Position pLeftBoardDown = new Position(l+1,0);
		Position pLeftBoardRight = new Position(l,c+1);
		Position pLeftBoardDiagSup = new Position(l-1,c+1);
		Position pLeftBoardDiagDown = new Position(l+1,c+1);
		
		if(l == 0 && c == 0 &&
				((board.getKey(p01) != null && !team.equals(getPlayerTeam(board.getKey(p01))) && board.hasValue(p01)
				&& board.getKey(p10) != null && !team.equals(getPlayerTeam(board.getKey(p10))) && board.hasValue(p10)) ||
				(board.getKey(p11) != null && !team.equals(getPlayerTeam(board.getKey(p11))) && board.hasValue(p11))))
			return true;
		else if(l == 0 && c == 34 &&
					((board.getKey(p033) != null && !team.equals(getPlayerTeam(board.getKey(p033))) && board.hasValue(p033)
					&& board.getKey(p134) != null && !team.equals(getPlayerTeam(board.getKey(p134))) && board.hasValue(p134)) ||
					(board.getKey(p133) != null && !team.equals(getPlayerTeam(board.getKey(p133))) && board.hasValue(p133))))
				return true;		
		else if(l == 34 && c == 34 &&
					((board.getKey(p3334) != null && !team.equals(getPlayerTeam(board.getKey(p3334))) && board.hasValue(p3334)
					&& board.getKey(p3433) != null && !team.equals(getPlayerTeam(board.getKey(p3433))) && board.hasValue(p3433)) ||
					(board.getKey(p3333) != null && !team.equals(getPlayerTeam(board.getKey(p3333))) && board.hasValue(p3333))))
				return true;
		else if(l == 34 && c == 0 &&
				((board.getKey(p330) != null && !team.equals(getPlayerTeam(board.getKey(p330))) && board.hasValue(p330)
				&& board.getKey(p341) != null && !team.equals(getPlayerTeam(board.getKey(p341))) && board.hasValue(p341)) ||
				(board.getKey(p331) != null && !team.equals(getPlayerTeam(board.getKey(p331))) && board.hasValue(p331))))
			return true;
		else if(l == 0 &&
				((board.getKey(pSupBoardLeft) != null && !team.equals(getPlayerTeam(board.getKey(pSupBoardLeft))) && board.hasValue(pSupBoardLeft)
				&& board.getKey(pSupBoardRight) != null && !team.equals(getPlayerTeam(board.getKey(pSupBoardRight))) && board.hasValue(pSupBoardRight)
				&& board.getKey(pSupBoardDown) != null && !team.equals(getPlayerTeam(board.getKey(pSupBoardDown))) && board.hasValue(pSupBoardDown)) ||
				(board.getKey(pSupBoardDiagLeft) != null && !team.equals(getPlayerTeam(board.getKey(pSupBoardDiagLeft))) && board.hasValue(pSupBoardDiagLeft)
				&& board.getKey(pSupBoardDiagRight) != null && !team.equals(getPlayerTeam(board.getKey(pSupBoardDiagRight))) && board.hasValue(pSupBoardDiagRight))))
			return true;
		else if(c == 34 &&
				((board.getKey(pRightBoardSup) != null && !team.equals(getPlayerTeam(board.getKey(pRightBoardSup))) && board.hasValue(pRightBoardSup)
				&& board.getKey(pRightBoardDown) != null && !team.equals(getPlayerTeam(board.getKey(pRightBoardDown))) && board.hasValue(pRightBoardDown)
				&& board.getKey(pRightBoardLeft) != null && !team.equals(getPlayerTeam(board.getKey(pRightBoardLeft))) && board.hasValue(pRightBoardLeft)) ||
				(board.getKey(pRightBoardDiagSup) != null && !team.equals(getPlayerTeam(board.getKey(pRightBoardDiagSup))) && board.hasValue(pRightBoardDiagSup)
				&& board.getKey(pRightBoardDiagDown) != null && !team.equals(getPlayerTeam(board.getKey(pRightBoardDiagDown))) && board.hasValue(pRightBoardDiagDown))))
			return true;
		else if(l == 34 &&
				((board.getKey(pDownBoardRight) != null && !team.equals(getPlayerTeam(board.getKey(pDownBoardRight))) && board.hasValue(pDownBoardRight)
				&& board.getKey(pDownBoardLeft) != null && !team.equals(getPlayerTeam(board.getKey(pDownBoardLeft))) && board.hasValue(pDownBoardLeft)
				&& board.getKey(pDownBoardSup) != null && !team.equals(getPlayerTeam(board.getKey(pDownBoardSup))) && board.hasValue(pDownBoardSup)) ||
				(board.getKey(pDownBoardDiagLeft) != null && !team.equals(getPlayerTeam(board.getKey(pDownBoardDiagLeft))) && board.hasValue(pDownBoardDiagLeft)
				&& board.getKey(pDownBoardDiagRight) != null && !team.equals(getPlayerTeam(board.getKey(pDownBoardDiagRight))) && board.hasValue(pDownBoardDiagRight))))
			return true;
		else if(c == 0 &&
				((board.getKey(pLeftBoardSup) != null && !team.equals(getPlayerTeam(board.getKey(pLeftBoardSup))) && board.hasValue(pLeftBoardSup)
				&& board.getKey(pLeftBoardDown) != null && !team.equals(getPlayerTeam(board.getKey(pLeftBoardDown))) && board.hasValue(pLeftBoardDown)
				&& board.getKey(pLeftBoardRight) != null && !team.equals(getPlayerTeam(board.getKey(pLeftBoardRight))) && board.hasValue(pLeftBoardRight)) ||
				(board.getKey(pLeftBoardDiagSup) != null && !team.equals(getPlayerTeam(board.getKey(pLeftBoardDiagSup))) && board.hasValue(pLeftBoardDiagSup)
				&& board.getKey(pLeftBoardDiagDown) != null && !team.equals(getPlayerTeam(board.getKey(pLeftBoardDiagDown))) && board.hasValue(pLeftBoardDiagDown))))
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
