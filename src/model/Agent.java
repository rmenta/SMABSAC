package model;
//my commit manuel
import java.util.LinkedList;
import java.util.Random;



public class Agent{
	public LinkedList<Neighbour> neighbours;
	private LinkedList<Agent> tradingRequests;
	public Agent lastPartner;
	public int id;
	private static int idCounter = 0;
	private double resource;
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

		neighbours = new LinkedList<>();
		tradingRequests = new LinkedList<>();
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
		Random r =  new Random();
		lastPartner = null;
		posX = (posX + (int) ((r.nextDouble()-0.5)*4)) % 1500;
		posY = (posY + (int) ((r.nextDouble()-0.5)*4)) % 1000;
		if(tradingRequests.size() == 0){
			return;
		}
		
		// Check if trade happens

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
		//System.out.println("Agent " + id + " supported Agent " + theChosenOne.id);



		// move chosenOne in direction of the agent
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
	
	public double requestResource(){
		return resource;
	}
	
	public void paymentTime(double b){
		resource += b;
	}
	
	
	public void die(){
		for(Neighbour n : neighbours){
			Neighbour me = n.agent.getNeighbourFromAgent(this);
			n.agent.neighbours.remove(me);
		}
	}
}
