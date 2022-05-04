package Classes;

public class Board {
	//private HashMap<AID, Position> allPlayersPosition;
	private static int SIZE = 35;
	private int[][] board = new int[SIZE][SIZE];
	
	public int[][] getBoard() {
		return board;
	}
	
	public void setBoard(int[][] board) {
		this.board = board;
	}
}
