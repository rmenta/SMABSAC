package visualization;


import model.Agent;

class Edge {
	final Agent a1;
	final Agent a2;
	
	/**
	 * Creates a edge with two agents and their respective coordinates
	 * @param a1 The first actor
	 * @param a2 The second actor
	 */
	Edge(Agent a1, Agent a2) {
		this.a1 = a1;
		this.a2 = a2;
	}
}
