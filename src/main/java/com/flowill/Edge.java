package com.flowill;


public class Edge {
	
	private static int nextId = 0;
	
	private Integer id;
	public int capacity;
	
	public Edge(int capacity) {
		this.id = Edge.nextId;
		Edge.nextId ++;
		this.capacity = capacity;
	}
	
	@Override
	public int hashCode(){
		return id.hashCode();
	}

	@Override
	public String toString() {
		return "{Id: " + id + " capacity: " + capacity + "}";
	}

}
