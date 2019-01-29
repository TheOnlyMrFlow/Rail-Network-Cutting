package com.flowill;


public class Station implements Comparable<Station>{

	private String name;
	private int x;
	private int y;
	
	public Station (String name, int x, int y) {
		this.name = name;
		this.x = x;
		this.y = y;
	}

	public String getName() {
		return name;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}
	
	@Override
	public int hashCode(){
		return x^(y >>> 32);
	}
	@Override
	public boolean equals(Object other){
		if (other == null) return false;
		if (other == this) return true;
		if (!(other instanceof Station))return false;
		Station otherCell = (Station)other;
		return x==otherCell.x && y==otherCell.y;
	}
	@Override
	public String toString(){
		return name + " ["+x+","+y+"]";
	}
	
	public int compareTo(Station other) {
		if (other == null) return 1;
		if (other == this) return 0;
		if (this.x == other.x) {
			return this.y - other.y;
		}
		return this.x - other.x;
	}
	
}
