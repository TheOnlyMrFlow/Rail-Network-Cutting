package com.flowill;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;


public class App {
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		

		try {
			
			
			AdjacencyNetwork<Station> network =  generate(new FileInputStream(new File(args[0])),
					new FileInputStream(new File(args[1])));
			
			if (args.length > 2 && args[2].equals("-quiet")) {
				network.maxFlow();
				network.minCut();
			}
			else {
				new Game(network).start();
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
		
	public static AdjacencyNetwork <Station> generate(InputStream vertexIs, InputStream edgesIs) throws IOException {
		
		
		AdjacencyNetwork<Station> network = new AdjacencyNetwork<Station>();

		
		BufferedReader br= new BufferedReader(new InputStreamReader(vertexIs));
		
		
		Map<String, Station> tempStationMap = new HashMap<String, Station>();
		
		while (br.ready()) {
			String line = br.readLine();
			String[] props = line.split(" ");
			tempStationMap.put(props[0], new Station (props[0], Integer.parseInt(props[1]), Integer.parseInt(props[2])));
			
		}
		
		br= new BufferedReader(new InputStreamReader(edgesIs));
		
		while (br.ready()) {
			String line = br.readLine();
			String[] props = line.split(" ");
			
			Station from = tempStationMap.get(props[0]);
            Station to = tempStationMap.get(props[1]);
            			
			if (from.getName().contains("Source")) {
				network.setSrc(from);
			}
			if (to.getName().contains("Sink")) {
				network.setSink(to);
			}
			
			
			network.addEdge(new Edge(Integer.parseInt(props[2])), from, to);
			
			
		}
		
		
			
		return network;
		
	}

}
