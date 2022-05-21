package Classes;

public class Report {
	private String winner;
	private String loser;
	private int remainingPlayersWinner = 0;
	private int remainingPlayersLoser = 0;
	private int durationInRounds = 0;
	
	public Report(String winner, String loser, int remainingPlayersWinner, int remainingPlayersLoser, int durationInRounds) {
		this.winner = winner;
		this.loser = loser;
		this.remainingPlayersWinner = remainingPlayersWinner;
		this.remainingPlayersLoser = remainingPlayersLoser;
		this.durationInRounds = durationInRounds;
	}
	
	@Override
	public String toString() {
		String res = "";
		
		res += "------------- Game report --------------\n";
		if (remainingPlayersWinner == remainingPlayersLoser)
			res += "Draw, teams have " + remainingPlayersWinner + " alive\n";
		else {
			res += "Team " + winner + " won with " + remainingPlayersWinner + " players remaining!\n";
			res += "Team " + loser + " lost with " + remainingPlayersLoser + " players remaining!\n";
		}
		res += "The game had a duration of " + durationInRounds + " rounds!\n";
		
		return res;
	}
}
