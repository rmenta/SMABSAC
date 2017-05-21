package visualization;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;


import javax.swing.*;

import model.*;

/**
 * Doing all the dirty stuff and initialization
 *
 * @author renato
 */
public class God extends JPanel {
	// Properties of the visualization
	private final static int NUMBER_OF_AGENTS = 500;
	private final static int WIDTH = 880;
	private final static int HEIGHT = 800;
	private final static int PANEL_WIDTH = 400;
	private final static int RADIUS = 4;
	private final static int TILE_SIZE = 40;
	private final static boolean showInfo = false;

	// Properties of the model
	private final static double PROXIMITY = 0.95;
	private final static int COST_OF_LIFE = 1;
	private final static int STARTING_CAPITAL = 500;
	private static int[][] parcelCount = new int[WIDTH/TILE_SIZE][HEIGHT/TILE_SIZE];
	private static int MAX_AGE = 1000;
	private static int EARLIEST_BIRTH_AGE = 500;
	private static int BIRTH_PERIOD = 20;
	
	// Interesting factors
	private final static double MERCIFUL_GOD_FACTOR = 1;			// 1 -> total of distribution = total need, < 1 -> dist < need
	private final static double WILLINGNESS_TO_TRADE = 0.7;			// chance that a trade even takes place at all (independent of rest)
	private final static double GOLD_DIG_FACTOR = 0.00;				// 0: Only trade with people you like, 1: Only trade with poor people
	private final static double CIRCLE_OF_LIFE_FACTOR = 0.0001;		// possiility for a baby/death

	// Graphs
	private Chart livingAgents;
	private Chart totalWealth;
	private Chart wealthNeighbours;
	private Chart neighboursPerAgent;

	// Bookkeeping
	private ChartContainer chartContainer = new ChartContainer(WIDTH + 10, 100, PANEL_WIDTH - 80, 75);
	private LinkedList<Agent> agents;
	private LinkedList<Edge> edges;
	private int tickCounter = 0;
	private long fpsTime = System.currentTimeMillis();
	private long fpsCounter = 0;
	private long fps = 0;

	private God() {

		// Create lists
		agents = new LinkedList<>();
		edges = new LinkedList<>();


		// Create graphs
		livingAgents = new XYChart("Number of living agents", "Time", "Living Agents");
		totalWealth = new XYChart("Wealth of all living agents", "Time", "Total Wealth");
		wealthNeighbours = new Histogram("Wealth compared to amount of neighbours", "Neighbours", "Wealth");
		neighboursPerAgent = new Histogram("Neighbours per Agent", "Neighbours", "Agents");
		totalWealth.setMaxSize(0);
		livingAgents.setMaxSize(5000);

		// Add graphs to list
		chartContainer.addChart(livingAgents);
		chartContainer.addChart(totalWealth);
		chartContainer.addChart(wealthNeighbours);chartContainer.addChart(neighboursPerAgent);

        // Create agents at random locations
        Random r = new Random();
        System.out.println("Create " + NUMBER_OF_AGENTS + " agents...");
        while (agents.size() < NUMBER_OF_AGENTS) {

            // Create agent
            int x = RADIUS + r.nextInt(WIDTH - 10 * RADIUS);
            int y = RADIUS + r.nextInt(HEIGHT - 10 * RADIUS);
            int age = r.nextInt(MAX_AGE);
            Agent a = new Agent(r.nextInt(STARTING_CAPITAL), WILLINGNESS_TO_TRADE, GOLD_DIG_FACTOR, x, y, age);

            // Check if space is not yet occupied
            boolean tooClose = false;
            for (Agent other : agents) {
                if (getDistance(x, y, other.posX, other.posY) < 3 * RADIUS) {
                    tooClose = true;
                }
            }

            // add newly created agent to list
            if (!tooClose) {
                agents.add(a);
                parcelCount[a.posX / TILE_SIZE][a.posY / TILE_SIZE]++;
            }
        }

        // Create connections between agents.
        System.out.println("Create edges...");
        assignNeighbours();

    }

    private double getDistanceRatio(double distance){
		double maxLength = getDistance(0, 0, WIDTH, HEIGHT);
		return Math.pow(1 - distance / maxLength, 1. / (1 - PROXIMITY));
	}
    private void assignNeighbours() {
        Random r = new Random();

        LinkedList<Agent> others = (LinkedList<Agent>) agents.clone();
        for (Agent me : agents) {
            for (Agent other : others) {
                // don't make connections to itself
                if (me == other) {
                    continue;
                }
                // the higher the ratio the closer two agents are
                double distance = getDistance(me.posX, me.posY, other.posX, other.posY);
                if (getDistanceRatio(distance) > r.nextDouble()) {
                    edges.add(new Edge(me, other));
                    me.neighbours.add(new Neighbour(other, 0));
                    other.neighbours.add(new Neighbour(me, 0));
                }
            }

            // Remove node from list to avoid duplication
            others.remove(me);
        }
    }

    public static void main(String[] args) {
        God oli = new God();

		JFrame frame = new JFrame("[SMABSAC]");
		frame.add(oli);
		frame.setSize(WIDTH + PANEL_WIDTH, HEIGHT);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);
		int i = 0;
		while (true) {
			oli.rule();
			 if(i%1==0)
				oli.paintImmediately(0, 0, WIDTH + PANEL_WIDTH, HEIGHT);
			i++;

		}


	}

	private void rule() {
		Random r = new Random();

		//Increase tick counter by one
		tickCounter++;
		fpsCounter++;
		
		// Agents propose trades with their neighbours
		for (Agent agent : agents) {
			parcelCount[agent.posX / TILE_SIZE][agent.posY / TILE_SIZE]--;
			agent.proposeTrade();
		}

		// Agents trade
		for (Agent agent : agents) {
			agent.trade();
		}

		// Change parcelCount according to movement
		for (Agent agent : agents) {
			parcelCount[agent.posX / TILE_SIZE][agent.posY / TILE_SIZE]++;
		}

		// calculate factor for distribution
		double sumDistribution = 0;
		for(int i = 0; i < WIDTH; i+=TILE_SIZE){
			for(int j = 0; j < HEIGHT; j+=TILE_SIZE){
				sumDistribution += distFunction(i + TILE_SIZE/2, j + TILE_SIZE/2, tickCounter);
			}
		}
		double factor = COST_OF_LIFE * NUMBER_OF_AGENTS * MERCIFUL_GOD_FACTOR * (1. / sumDistribution);
		
		// Distribute resources and mark dead agents
		LinkedList<Agent> toRemove = new LinkedList<>();
		int totalWealthCounter = 0;
		for (Agent agent : agents) {
			int xPos = (agent.posX / TILE_SIZE) * TILE_SIZE;
			int yPos = (agent.posY / TILE_SIZE) * TILE_SIZE;
			int numberInParcel = parcelCount[agent.posX / TILE_SIZE][agent.posY / TILE_SIZE];
			double salary = factor * distFunction(xPos, yPos, tickCounter) / (double)numberInParcel;
			agent.paymentTime(salary - COST_OF_LIFE);

			if (agent.requestResource() < 0 || r.nextDouble() < CIRCLE_OF_LIFE_FACTOR) {
				agent.die();
				parcelCount[agent.posX / TILE_SIZE][agent.posY / TILE_SIZE]--;
				toRemove.add(agent);
				if(agent.requestResource() < 0){
					System.out.println("Agent " + agent.id + " has passed away (resource < 0).");
				}else{
					System.out.println("Agent " + agent.id + " has passed away (tough luck).");
				}
			} else{
				totalWealthCounter += agent.requestResource();
			}
		}

		// delete dead agents and mark edges to be removed
		LinkedList<Edge> edgesToRemove = new LinkedList<>();
		for (Agent a : toRemove) {
			agents.remove(a);
			for (Edge e : edges) {
				if (e.a1 == a || e.a2 == a) {
					edgesToRemove.add(e);
				}
			}
		}



		// probably delete far away neighbours
		for(Agent a : agents){
			double maxDistance = 0.0;
			Agent maxAgent = null;
			for(Neighbour n: a.neighbours){
				double distance = getDistance(a.posX, a.posY, n.agent.posX, n.agent.posY);
				if(distance > maxDistance) {
					maxDistance = distance;
					maxAgent = n.agent;
				}
			}

			if(maxAgent == null) continue;

			if(getDistanceRatio(maxDistance)*5 < r.nextDouble()){
				a.neighbours.remove(a.getNeighbourFromAgent(maxAgent));
				maxAgent.neighbours.remove(maxAgent.getNeighbourFromAgent(a));
				for(Edge e : edges){
					if(e.a1 == a && e.a2 == maxAgent || e.a2 == a && e.a1 == maxAgent  ){
						edgesToRemove.add(e);
					}
				}

			}

		}


		// probably add new neighbours


		for(Agent a: agents){
			double probability = 1./a.neighbours.size();
			if(probability < r.nextDouble()) continue;
			for(Agent b: agents){
				if(a == b) continue;
				if(a.neighbours.contains(a.getNeighbourFromAgent(b))) continue;
				if(getDistanceRatio(getDistance(a.posX,a.posY,b.posX,b.posY)) > r.nextDouble()){
					a.neighbours.add(new Neighbour(b,0));
					b.neighbours.add(new Neighbour(a,0));
					edges.add(new Edge(a,b));
					break;
				}
			}
		}

		// delete edges
		edges.removeAll(edgesToRemove);

		// MAKE BABIES :D
		LinkedList<Agent> childrenToAdd = new LinkedList<>();
		for(Agent a : agents){
			if(r.nextDouble() < 2*CIRCLE_OF_LIFE_FACTOR){
				Agent child = new Agent((int)a.requestResource()/2, WILLINGNESS_TO_TRADE, GOLD_DIG_FACTOR, a.posX, a.posY, 0);
				a.paymentTime(-a.requestResource()/2);
				childrenToAdd.add(child);
				parcelCount[a.posX / TILE_SIZE][a.posY / TILE_SIZE]++;
				for(Neighbour n : a.neighbours){
					child.neighbours.add(n);
					edges.add(new Edge(child, n.agent));
				}
				System.out.println("Agent " + a.id + " has made a fucking Baby :D");
			}
		}

		agents.addAll(childrenToAdd);
		
		// compute stuff for graphs
		int maxCountNeighbours = 0;
		for(Agent a : agents){
			if(a.neighbours.size() > maxCountNeighbours){
				maxCountNeighbours = a.neighbours.size();
			}
		}
		int[] neighboursCount = new int[maxCountNeighbours + 1];
		Integer [] neighboursAxisArray = new Integer[maxCountNeighbours + 1];
		Integer[] wealthForGraphArray = new Integer[maxCountNeighbours + 1];
		Integer[] neighboursForGraphArray = new Integer[maxCountNeighbours + 1];
		for(int i = 0; i < maxCountNeighbours + 1; i++){
			neighboursAxisArray[i] = i;
			wealthForGraphArray[i]= 0;
			neighboursForGraphArray[i]= 0;
		}
		for(Agent a : agents){
			wealthForGraphArray[a.neighbours.size()] += (int)a.requestResource();
			neighboursForGraphArray[a.neighbours.size()] += 1;
			neighboursCount[a.neighbours.size()]++;
		}
		for(int i = 0; i < maxCountNeighbours + 1; i++){
			if(neighboursCount[i] > 0){
				wealthForGraphArray[i] /= neighboursCount[i];
			}
		}

		LinkedList<Integer> neighboursAxis = new LinkedList<>(Arrays.asList(neighboursAxisArray));
		LinkedList<Integer> wealthForGraph = new LinkedList<>(Arrays.asList(wealthForGraphArray));
		LinkedList<Integer> neighboursForGraph = new LinkedList<>(Arrays.asList(neighboursForGraphArray));
		

		
		// update graphs
		livingAgents.addVal(tickCounter, agents.size());
		totalWealth.addVal(tickCounter, totalWealthCounter);
		wealthNeighbours.updateValues(neighboursAxis, wealthForGraph);
		neighboursPerAgent.updateValues(neighboursAxis, neighboursForGraph);

	}

	@Override
	public void paint(Graphics g) {
		// Draw background
		g.setColor(Color.white);
		g.fillRect(0, 0, WIDTH, HEIGHT);
		
		// Draw distribution in background
		for(int i = 0; i < WIDTH; i+=TILE_SIZE){
			for(int j = 0; j < HEIGHT; j+=TILE_SIZE){
				g.setColor(new Color(255, (int)(255 * distFunction(i + TILE_SIZE / 2, j + TILE_SIZE / 2, tickCounter)), 0));
				g.fillRect(i, j, TILE_SIZE, TILE_SIZE);
				
				g.setColor(Color.black);
				if(showInfo){g.drawString(Integer.toString(parcelCount[i / TILE_SIZE][j / TILE_SIZE]), i, j+10);}
			}
		}

		// Draw all the edges
		g.setColor(Color.gray);
		for (Edge e : edges) {
			g.drawLine(e.a1.posX, e.a1.posY, e.a2.posX, e.a2.posY);
		}

		// Draw last transactions
		Graphics2D g2 = (Graphics2D) g;
		for (Agent a : agents) {
			Agent p = a.lastPartner;
			if (p != null) {
				for (Agent t : agents) {
					if (t == p) {
						g2.setStroke(new BasicStroke(2));
						g2.setColor(Color.black);
						g2.drawLine(a.posX, a.posY, t.posX, t.posY);
						break;
					}
				}
			}
		}

		// Draw all the agents
		g.setColor(Color.black);
		for (Agent a : agents) {
			g.setColor(new Color(0, Math.min(255, (int)a.requestResource()), 0));
			g.fillOval(a.posX - RADIUS, a.posY - RADIUS, 2*RADIUS, 2*RADIUS);
			g.setColor(Color.black);
			if(showInfo){
				g.drawString("   ID: " + a.id + ", " + Double.toString(a.requestResource()).substring(0, Math.min(6, Double.toString(a.requestResource()).length())), a.posX, a.posY);
			}
		}

		// Clear side panel
		g.setColor(Color.white);
		g.fillRect(WIDTH, 0, WIDTH + PANEL_WIDTH, HEIGHT);
		
		// draw infos
		if(System.currentTimeMillis() - fpsTime > 1000){
			fps = fpsCounter;
			fpsTime = System.currentTimeMillis();
			fpsCounter = 0;
		}
		g.setColor(Color.black);
		g.drawString("Number of Agents: " + agents.size(), WIDTH + 10, 20);
		g.drawString("Number of Edges:  " + edges.size(), WIDTH + 10, 40);
		//g.drawString("Merciful god factor: " + MERCIFUL_GOD_FACTOR, WIDTH + 10, 60);
		g.drawString("TPS: " + fps, WIDTH + PANEL_WIDTH - 100, 20);
		
		// Draw graphs
		chartContainer.paintComponent(g);
	}

	private double distFunction(int xPos, int yPos, int t){
		double x = ((double)xPos / WIDTH) * 6;
		double y = ((double)yPos / HEIGHT) * 6;
		double aux0 = Math.sin(x*Math.cos(x) + (double)tickCounter/10.) * Math.cos(y * Math.sin(x) );
		double dynamic = Math.sin(x + (double)tickCounter/500) * Math.cos(y + (double)tickCounter/500);
		double staticc = Math.sin(x) * Math.cos(y);
		double hard = Math.sin(x/5 * Math.sin(y) + (double)tickCounter/1000) * Math.cos(y/2 + (double)tickCounter/4000);
		Random r = new Random();
		double random = r.nextDouble();
		return (staticc + 1)/2;
	}
	
	private double getDistance(int x1, int y1, int x2, int y2) {
		return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
	}


}
