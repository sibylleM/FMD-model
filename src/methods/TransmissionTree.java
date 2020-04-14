package methods;

import java.util.ArrayList;

public class TransmissionTree {
	private ArrayList<Edge> edges;
	
	private class Edge {
		public String src, rcp;
		int day;
		int type; // local, market, direct
		
		public Edge(String src, String rcp, int day, int type) {
			super();
			this.src = src;
			this.rcp = rcp;
			this.day = day;
			this.type = type;
		}
		
		@Override
		public String toString() {
			return src + " -> " + rcp + " (day = " + day + "; type = " + type + ")";
		}
	}
	
	public void addEdge(String src, String rcp, int date, int type) {
		edges.add(new Edge(src, rcp, date, type));
	}
	
	public ArrayList<Edge> getEdges() { // for visualising
		return edges;
	}
}
