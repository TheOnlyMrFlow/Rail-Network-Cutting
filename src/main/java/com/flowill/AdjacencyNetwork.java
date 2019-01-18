package com.flowill;


import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class AdjacencyNetwork <Vertex> {
	
	public enum Result {
		GOOD,
		STILL_LINKED,
		NOT_OPTIMAL
	}
	
	private Vertex source;
	private Vertex sink;
	
	public int maxFlowFound; //not used in algos, just here for displaying purpose
	
	// original graph
	private Map<Edge, Vertex> edgesToSrc = new HashMap<Edge, Vertex>();
	private Map<Edge, Vertex> edgesToDest = new HashMap<Edge, Vertex>();
	
	//Having only outgoing edges is very usefull to loop on neighbours vertices while knowing the edge we took
	private Map<Vertex, Set<Edge> > vertexToOutgoingEdges= new HashMap<Vertex, Set<Edge>>(); 
	
	// those 3 getters are for displaying the game
	public Map<Edge, Vertex> getEdgesToSrc() {
		return Collections.unmodifiableMap(this.edgesToSrc);
	}
	public Map<Edge, Vertex> getEdgesToDest() {
		return Collections.unmodifiableMap(this.edgesToDest);
	}
	public Map<Vertex, Set<Edge>> getVertexToOutgoingEdges() {
		return Collections.unmodifiableMap(this.vertexToOutgoingEdges);
	}
	
	// residual graph
	private Map<Edge, Vertex> residualEdgesToSrc = new HashMap<Edge, Vertex>(edgesToSrc);
	private Map<Edge, Vertex> residualEdgesToDest = new HashMap<Edge, Vertex>(edgesToDest);
	private Map<Vertex, Set<Edge> > residualVertexToOutgoingEdges= new HashMap<Vertex, Set<Edge>>(vertexToOutgoingEdges);
	// find an edge in the original graph that corresponds to one in the residual
	private Map<Edge, Edge> residualEdgeToEdge = new HashMap<Edge, Edge>(); 
	// find reverse edge of a given edge in residual graph
	private Map<Edge, Edge> residualEdgeToReverse = new HashMap<Edge, Edge>(); 
	
	
	public void setSrc (Vertex src) {
		this.source = src;
	}
	
	public void setSink (Vertex sink) {
		this.sink = sink;
	}
	
	public boolean isSink (Vertex v) {
		return v == this.sink; // == is meant here
	}
	
	public boolean isSrc (Vertex v) {
		return v == this.source; // == is meant here
	}
	

	// adding an edge fills up both original and residual graphs so we dont waste an etra O(E) loop for filling the residual
	// the drawback is that we can only run the maxflow algorithm once
	public void addEdge (Edge e, Vertex from, Vertex to) {
		
		// fill original graph
		
		edgesToSrc.put(e, from);
		edgesToDest.put(e, to);
		
		if (vertexToOutgoingEdges.get(from) == null) {
			vertexToOutgoingEdges.put(from, new HashSet<Edge>());
		}
		
		vertexToOutgoingEdges.get(from).add(e);
		
		///
		
		// fill residual graph
		
		Edge copy = new Edge(e.capacity);
		residualEdgesToSrc.put(copy, from);
		residualEdgesToDest.put(copy, to);
		
		Edge empty = new Edge(0);
		residualEdgesToSrc.put(empty, to);
		residualEdgesToDest.put(empty, from);
		
		if (residualVertexToOutgoingEdges.get(from) == null) {
			residualVertexToOutgoingEdges.put(from, new HashSet<Edge>());
		}
		
		if (residualVertexToOutgoingEdges.get(to) == null) {
			residualVertexToOutgoingEdges.put(to, new HashSet<Edge>());
		}
		
		residualVertexToOutgoingEdges.get(from).add(copy);
		residualVertexToOutgoingEdges.get(to).add(empty);
		
		residualEdgeToReverse.put(empty, copy);
		residualEdgeToReverse.put(copy, empty);
		
		///
		
	}
	
	
	
	public int maxFlow() {	
		
		//ford-fulkerson algorithm
		
		Map<Edge, Integer> edgesToFlow = new HashMap<Edge, Integer>();
		
		for (Edge e : edgesToSrc.keySet()) {
			edgesToFlow.put(e, 0);
		}
		
		int maxFlow = 0;
		
		boolean foundPath = true;
		
		// while there is an augmenting path from s to t
		// since we use edmond karp, this will be executed no more than E times
		while (foundPath) {
			
			foundPath = false;
		
			// Edmond Karp special case : we use bfs to find path		
		
			Map<Vertex, Vertex> verticesHistory = new HashMap<Vertex, Vertex>();
			// keeps track of the min capacity of the edges we took to arrive at this point
			// so we dont have to loop through the found path an extra time to find bottleneck
			Map<Vertex, Integer> vertexToBottleNeck = new HashMap<Vertex, Integer>(); 
			// edge history is usefull once we found a path : we just augment flow on every one of them
			// without having to find them back from the verticesHistoryMap
			Map<Vertex, Edge> edgeHistory = new HashMap<Vertex, Edge>();
			Queue<Vertex> toVisitQueue = new LinkedList<Vertex>();
			toVisitQueue.add(this.source);
			vertexToBottleNeck.put(this.source, Integer.MAX_VALUE);
			Vertex visiting = null;
			
			while (!toVisitQueue.isEmpty() && !foundPath) {
				
				visiting = toVisitQueue.remove();
				
				//loop on neighbors in residual graph
				for (Edge e : residualVertexToOutgoingEdges.get(visiting)) {
					
					// 0 capacity is just like a non exisisting edge
					if (e.capacity == 0) {
						continue;
					}
					
					Vertex v = residualEdgesToDest.get(e);
					
					if (!verticesHistory.containsKey(v)) {
					
						verticesHistory.put(v, visiting);
						edgeHistory.put(v, e);
						
						// always keep track of the bottleneck of the path we are on, so we dont have to loop over the found path an extra time at the end to get it.
						int currentMinCap = Math.min(vertexToBottleNeck.get(visiting), e.capacity);
						vertexToBottleNeck.put(v, currentMinCap);
					
						if (v.equals(this.sink)) {
							
							
							maxFlow += currentMinCap;
							
							// we go through the found path and update both flow and residual graph
							while (v != this.source) {
								Edge residualEdge = edgeHistory.get(v);

								// remove residual capacity in the ingoing direction
								residualEdge.capacity -= currentMinCap;
								
								// add residual capacity in the reverse direction
								residualEdgeToReverse.get(residualEdge).capacity += currentMinCap; 


								Edge originalEdge = residualEdgeToEdge.get(residualEdge);
								
								// was it an undo operation or not ? if not we add flow, if yes we remove flow
								boolean parallel = edgesToSrc.get(originalEdge) == residualEdgesToSrc.get(residualEdge);
								edgesToFlow.put(originalEdge, parallel ? currentMinCap : - currentMinCap);								
								
														
								
								v = residualEdgesToSrc.get(residualEdge);
							}
							
							foundPath = true;
							break;
						}
						
						else {
							toVisitQueue.add(v); 
						}
						
					}
				}
				
				
			}

		}
		
		///
		System.out.println("");
		System.out.println("MaxFlow / MinCut = " + maxFlow);
		System.out.println("");
		return maxFlow;

		
		
	}
	
	
	public Set<Edge> minCut() {	//to be run after maxFlow()
		
		// we first find all vertices in the residual graph that are reachable from the source

		Set<Vertex> reachable = new HashSet<Vertex>();

		Queue<Vertex> toVisitQueue = new LinkedList<Vertex>();
		toVisitQueue.add(source);
		while (!toVisitQueue.isEmpty()) {
			Vertex visiting = toVisitQueue.remove();
			reachable.add(visiting);
			Set<Edge> edges =residualVertexToOutgoingEdges.get(visiting);
			if (edges == null) {
				continue;
			}
			for (Edge e : edges ) {
				if (e.capacity == 0) {
					continue;
				}
				Vertex v = residualEdgesToDest.get(e);
				if (!reachable.contains(v)) {
					toVisitQueue.add(v);
				}
			}
		}

		// for every of the reachable vertices, we check if an edge connect them to a non reachable one.
		// if so, it means this edge must be cut
		
		System.err.println("Edges to cut : (id starts from 0)");
		Set<Edge> minCutEdges = new HashSet<Edge>();
		for (Vertex v : reachable) {
			Set<Edge> edges =vertexToOutgoingEdges.get(v); 
			if (edges == null) {
				continue;
			}
			for (Edge e : edges ) {
				if (!reachable.contains(edgesToDest.get(e))) {
					minCutEdges.add(e);
					System.err.println(e.toString() + " from " + edgesToSrc.get(e).toString() + " to " + edgesToDest.get(e).toString());
				}
			}
		}
		System.err.println("");
		
		return minCutEdges;
	}
	
	
	// try the solution provided by user
	public Result trySolution(Set<Edge> neutralizedEges) {
		
		//1st check if the user isolated the src from the sink
		
		Queue<Vertex> toVisitQueue = new LinkedList<Vertex>();
		Set<Vertex> visited = new HashSet<Vertex>();
		toVisitQueue.add(this.source);
		
		
		while (!toVisitQueue.isEmpty()) {
			Vertex visiting = toVisitQueue.remove();
			visited.add(visiting);
			Set<Edge> edges =vertexToOutgoingEdges.get(visiting);
			if (edges == null) {
				continue;
			}
			for (Edge e : edges) {
				if (e.capacity == 0 || neutralizedEges.contains(e)) {
					continue;
				}
				Vertex v = edgesToDest.get(e);
				if (visited.contains(v)){
					continue;
				}
				toVisitQueue.add(v);
				if (v.equals(sink)) {
					return Result.STILL_LINKED;
				}
			}
		}

		// if he isolated src and sink, we check if his solution is optimal :
		
		int cutCap = 0;
		for (Edge e : neutralizedEges) {
			cutCap += e.capacity;
		}
		maxFlowFound = maxFlow();
		if (maxFlowFound != cutCap) {
			return Result.NOT_OPTIMAL;
		}
		
		return Result.GOOD;
		
	}
	
	

}
