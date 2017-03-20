package visualization;

public class Edge {
	public final Triple a1;
	public final Triple a2;
	public final double length;
	
	/**
	 * Creates a edge with two agents and their respective coordinates
	 * @param a1 The first actor
	 * @param a2 The second actor
	 */
	public Edge(Triple a1, Triple a2) {
		this.a1 = a1;
		this.a2 = a2;
		
		this.length = Math.sqrt((a1.x - a2.x) * (a1.x - a2.x) + (a1.y - a2.y) * (a1.y - a2.y));
	}
}
