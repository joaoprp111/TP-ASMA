package Classes;

import jade.util.leap.Serializable;

public class Position implements Serializable{
	private int posX;
	private int posY;
	
	public Position(int x,int y) {
		this.posX = x;
		this.posY = y;
	}
	
	public int getPosX() {
		return this.posX;
	}
	
	public int getPosY() {
		return this.posY;
	}
	
	public void updatePos(int x, int y) {
		this.posX = x;
		this.posY = y;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if(!(o instanceof Position))
			return false;
		Position other = (Position) o;
		boolean positionEquals = (this.posX == other.getPosX()) && (this.posY == other.getPosY());
		return positionEquals;
	}
	
	@Override
	public String toString() {
		return Integer.toString(posX) + " x " + Integer.toString(posY);
	}
}
