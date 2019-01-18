package com.flowill;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import edu.princeton.cs.introcs.StdDraw;


public class Game {
	
	private AdjacencyNetwork<Station> network;
	private Map<Edge, Double[]> edgeToClickablePoint = new HashMap<Edge, Double[]>();
	private Set<Edge> selectedEdges = new HashSet<Edge>();
	public String instructions;
	
	private int removedSumPlayer = 0;
	
	public Game(AdjacencyNetwork<Station> network) {
		this.network = network;
		StdDraw.setCanvasSize(800, 800);
		
		
	}
	
	public void start() {
		this.instructions = "Click an arrow of an edge to destroy/undestroy it";
		drawGraph();
		gameLoop();
	}
	
	public void commit() {
		
		AdjacencyNetwork.Result r = network.trySolution(selectedEdges);
		
		switch (r) {
		case GOOD:
			instructions = "GG you found an optimal solution. Maybe we don't have the same tho, here is mine :";
			selectedEdges = network.minCut();
			drawGraph();
			break;
		case STILL_LINKED:
			instructions = "Rly ? The sink and source are still linked, try again";
			drawGraph();
			gameLoop();
			break;
		case NOT_OPTIMAL:
			instructions = "Your solution is not optimal, check mine :";
			selectedEdges = network.minCut();
			drawGraph();
	    	StdDraw.setPenColor(StdDraw.RED);
			StdDraw.textLeft(0, 0, "Optimal : " + network.maxFlowFound);
			break;
	}
		
	}
	
	public void drawGraph() {
		
		
		Set<Station> drew = new HashSet<Station>();
		Map<Edge, Station> edgeToDest = network.getEdgesToDest();
		Map<Edge, Station> edgeToSrc = network.getEdgesToSrc();
		
    	StdDraw.clear();
    	
    	
    	
    	StdDraw.setPenColor(StdDraw.BLUE);
    	
    	StdDraw.rectangle(0.97, 0.03, 0.06, 0.03);
    	
    	StdDraw.text(0.97, 0.03, "Commit");
    	
    	StdDraw.text(0.5, 1, instructions);

    	StdDraw.textLeft(0, 0.05, "Capacity you removed : " + removedSumPlayer);
    	

    	
		for (Edge e : edgeToDest.keySet()) {
			
			

			Station src = edgeToSrc.get(e);
			Station dest = edgeToDest.get(e);
			
			Station[] current = {src, dest};
			

        	for (Station s : current) {
        		if (!drew.contains(s)) {
    				if (network.isSink(s)) {
    		        	StdDraw.setPenColor(StdDraw.BLACK);
    		        	//StdDraw.text(s.getX() / 10.0, (s.getY() - 0.5) / 10.0, "Sink");
    		        	StdDraw.setPenColor(StdDraw.GREEN);
    				}
    				else if (network.isSrc(s)) {
    					StdDraw.setPenColor(StdDraw.BLACK);
    		        	//StdDraw.text(s.getX() / 10.0, (s.getY() - 0.5) / 10.0, "Source");
    					StdDraw.setPenColor(StdDraw.BLUE);
    				}
    				else {
        	        	StdDraw.setPenColor(StdDraw.RED);
    				}
    				StdDraw.filledCircle(s.getX() / 10.0, s.getY() / 10.0, 0.03);
    	        	StdDraw.setPenColor(StdDraw.BLACK);
		        	StdDraw.text(s.getX() / 10.0, (s.getY() + 0.5) / 10.0, s.getName());

    				drew.add(s);
    			}
        	}
        	
			
			StdDraw.setPenColor(selectedEdges.contains(e) ? StdDraw.LIGHT_GRAY : StdDraw.BLACK);
			StdDraw.line(src.getX() / 10.0, src.getY() / 10.0, dest.getX() / 10.0, dest.getY() / 10.0);
			StdDraw.text((src.getX() + dest.getX() + 0.5) / 20.0, (src.getY() + dest.getY() - 0.5) / 20.0, "" + e.capacity);
			int vecX = dest.getX() - src.getX();
			int vecY = dest.getY() - src.getY();
			double originArrowX = (src.getX() + 0.5 * vecX) / 10.0;
			double originArrowY = (src.getY() + 0.5 * vecY) / 10.0;
			edgeToClickablePoint.put(e, new Double[] {originArrowX, originArrowY});
			//StdDraw.line(originArrowX, originArrowY, x1, y1);
			//StdDraw.filledPolygon(x, y);
			drawArrowLine(src.getX() / 10.0, src.getY() / 10.0, originArrowX, originArrowY, 0.02, 0.02);
		}
		
	}
	
	// speciale dedicace a stackOverflow <3
	private void drawArrowLine(double x1, double y1, double x2, double y2, double d, double h) {
	    double dx = x2 - x1, dy = y2 - y1;
	    double D = Math.sqrt(dx*dx + dy*dy);
	    double xm = D - d, xn = xm, ym = h, yn = -h, x;
	    double sin = dy / D, cos = dx / D;

	    x = xm*cos - ym*sin + x1;
	    ym = xm*sin + ym*cos + y1;
	    xm = x;

	    x = xn*cos - yn*sin + x1;
	    yn = xn*sin + yn*cos + y1;
	    xn = x;

	    double[] xpoints = {x2, xm, xn};
	    double[] ypoints = {y2, ym, yn};

	    StdDraw.filledPolygon(xpoints, ypoints);
	}
	
	
	public void gameLoop() {
		
		Map<Edge, Station> edgeToDest = network.getEdgesToDest();
		Map<Edge, Station> edgeToSrc = network.getEdgesToSrc();
		
		boolean wasPressed = false;
		
		while (true) {
			if (StdDraw.mousePressed()) {
				wasPressed = true;
			}
			else if (wasPressed) {
				wasPressed = false;
				
				if (StdDraw.mouseX() > 0.88 && StdDraw.mouseY() < 0.06) {
					commit();
					break;
				}
				
				for (Edge e : edgeToDest.keySet()) {
					
					
					//Station[] stations = {edgeToSrc.get(e), edgeToDest.get(e)};
					
					Double[] clickPoint = edgeToClickablePoint.get(e);
					if (Math.pow(StdDraw.mouseX() - clickPoint[0], 2) + Math.pow(StdDraw.mouseY() - clickPoint[1], 2) < 0.0025 ) {
						if (selectedEdges.contains(e)) {
							removedSumPlayer -= e.capacity;
							selectedEdges.remove(e);
						}
						else {
							removedSumPlayer += e.capacity;
							selectedEdges.add(e);
						}
						drawGraph();
						break;
					}
					
					
					
				}
			}
			
			try {
				TimeUnit.MILLISECONDS.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

}
