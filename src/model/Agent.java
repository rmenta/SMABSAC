package model;
//my commit manuel
import java.util.LinkedList;
import java.util.Random;



public class Agent{
	public LinkedList<Neighbour> neighbours;
	public LinkedList<Agent> tradingRequests;
	public Agent lastPartner;
	public int id;
	private static int idCounter = 0;
	private int resource;
	private final static int AMOUNT = 5;

	public int posX, posY;
	
	private double willingnessToTrade;  // 0 to 1, 1 meaning accepting always a trade
	private double goldDigFactor;		// 0 to 1, 1 meaning preferring money over sympathy
	
	/**
	 * 
	 * @param r Amount of resource given to this Agent when created
	 */
	public Agent(int resource, double wTT, double gDF, int xPos, int yPos) {
		id = idCounter;
		idCounter++;

		this.posX = xPos;
		this.posY = yPos;

		neighbours = new LinkedList<Neighbour>();
		tradingRequests = new LinkedList<Agent>();
		this.resource = resource;
		this.willingnessToTrade = wTT;
		this.goldDigFactor = gDF;
	}
	
	
	public void proposeTrade(){
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
			theChosenOne.enlist(this);
		}
		
		
	}
	
	public void trade(){
		lastPartner = null;
		if(tradingRequests.size() == 0){
			return;
		}
		
		// Check if trade happens
		Random r =  new Random();
		if(r.nextDouble() > willingnessToTrade){
			tradingRequests.clear();
			return;
		}
		
		Agent theChosenOne = null;
		Neighbour other = null;
		Neighbour n = null;
		
		// find trading partner
		double maxValue = Double.NEGATIVE_INFINITY;
		for(Agent a : tradingRequests){
			n = getNeighbourFromAgent(a);
			double value = -goldDigFactor * a.resource + (1 - goldDigFactor) * n.sympathy;
			if(value > maxValue && resource > a.requestResource() + 2 * AMOUNT){
				theChosenOne = a;
				maxValue = value;
				other = n;
			}
		}
		
		// abort if nobody found
		if(theChosenOne == null){
			tradingRequests.clear();
			return;
		}
		
		// actually trade stuff
		theChosenOne.resource += AMOUNT;
		resource -= AMOUNT;
		System.out.println("Agent " + id + " supported Agent " + theChosenOne.id);



		// move agent in direction of chosenOne
		//tetetetetet
		double diffX = (double) theChosenOne.posX - this.posX;
		double diffY = (double) theChosenOne.posY - this.posY;
		double distanceBetweenAgents = Math.sqrt((diffX*diffX) + (diffY*diffY));
		theChosenOne.posX = theChosenOne.posX + (int)((((double)this.posX - (double)theChosenOne.posX) / distanceBetweenAgents) * 5);
		theChosenOne.posY = theChosenOne.posY + (int)((((double)this.posY - (double)theChosenOne.posY) / distanceBetweenAgents) * 5);


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
	
	public void getWhatYouDeserve(int b){
		resource += b;
	}
	
	
	public void die(){
		for(Neighbour n : neighbours){
			Neighbour me = n.agent.getNeighbourFromAgent(this);
			n.agent.neighbours.remove(me);
		}
	}
}
