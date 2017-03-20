package model;

import java.util.LinkedList;
import java.util.Random;

public class Agent{
	public LinkedList<Neighbour> neighbours;
	public LinkedList<Agent> tradingRequests;
	public Agent lastPartner;
	
	private int resource;
	private final static int AMOUNT = 5;
	
	private double willingnessToTrade = 1;  // 0 to 1, 1 meaning accepting always a trade
	private double goldDigFactor;		// 0 to 1, 1 meaning preferring money over sympathy
	private int boundPoor;
	private int boundRich;
	
	/**
	 * 
	 * @param r Amount of resource given to this Agent when created
	 */
	public Agent(int resource, int bR, int bP) {
		neighbours = new LinkedList<Neighbour>();
		tradingRequests = new LinkedList<Agent>();
		this.resource = resource;
		boundPoor = bP;
		boundRich = bR;
	}
	
	
	public void proposeTrade(){
		if(resource > boundPoor){
			return;
		}
		
		Agent theChosenOne = null;
		double maxValue = Double.NEGATIVE_INFINITY;
		for(Neighbour n : neighbours){
			double value = goldDigFactor*n.agent.resource + (1 - goldDigFactor) * n.sympathy;
			if(value > maxValue){
				theChosenOne = n.agent;
				maxValue = value;
			}
		}
		
		if(theChosenOne != null){
			System.out.println("A enlisted in B");
			theChosenOne.enlist(this);
		}
		
		
	}
	
	public void trade(){
		lastPartner = null;
		if(tradingRequests.size() == 0){// || resource <= boundRich){
			return;
		}
		
		Agent theChosenOne = null;
		Neighbour other = null;
		Neighbour n = null;
		
		double maxValue = Double.NEGATIVE_INFINITY;
		for(Agent a : tradingRequests){
			n = getNeighbourFromAgent(a);
			double value = -goldDigFactor * a.resource + (1 - goldDigFactor) * n.sympathy;
			if(value > maxValue){
				theChosenOne = a;
				maxValue = value;
				other = n;
			}
		}
		
		
		// Check if trade happens
		Random r =  new Random();
		if(r.nextDouble() > willingnessToTrade){
			return;
		}
		
		// actually trade stuff
		theChosenOne.resource += AMOUNT;
		resource -= AMOUNT;
		System.out.println("A traded with B");
		
		// Update sympathy
		Neighbour me = theChosenOne.getNeighbourFromAgent(this);
		me.sympathy += 2;
		other.sympathy -= 1;
		
		tradingRequests.clear();
		lastPartner = theChosenOne;
	}
	
	public Neighbour getNeighbourFromAgent(Agent a){
		for(Neighbour n : neighbours){
			if(n.agent == a){
				return n;
			}
		}
		
		return null;
	}
	
	// A calls B's enlist if A wants to trade with B
	public void enlist(Agent a){
		for(Neighbour n : neighbours){
			if(n.agent == a){
				tradingRequests.add(a);
			}
		}
	}
	
	public int requestResource(){
		return resource;
	}
	
	public void payBills(int b){
		resource -= b;
	}
	
	public void die(){
		for(Neighbour n : neighbours){
			Neighbour me = n.agent.getNeighbourFromAgent(this);
			n.agent.neighbours.remove(me);
		}
	}
}
