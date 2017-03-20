package visualization;

import model.Agent;

public class Triple {
	public final Agent a;
	public int x;
	public int y;
	
	/**
	 * Creates a triple with an actor (cannot be changed) and its coordinates (can be changed)
	 * @param a The Actor
	 * @param x x coordinate
	 * @param y y coordinate
	 */
	public Triple(Agent a, int x, int y) {
		this.a = a;
		this.x = x;
		this.y = y;
	}
}
