package visualization;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.TileObserver;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import model.*;

/**
 * Doing all the dirty stuff and initialization
 *
 * @author renato
 */
public class God extends JPanel {
	// Properties of the visualization
	private final static int NUMBER_OF_AGENTS = 500;
	private final static int WIDTH = 1500;
	private final static int HEIGHT = 1000;
	private final static int PANEL_WIDTH = 400;
	private final static int RADIUS = 4;
	private final static int TILE_SIZE = 50;
	private final static boolean showInfo = false;

	// Properties of the model
	public final static double PROXIMITY = 0.95;
	private final static int COST_OF_LIFE = 1;
	private final static int STARTING_CAPITAL = 255;
	private static int[][] parcelCount = new int[WIDTH/TILE_SIZE][HEIGHT/TILE_SIZE];
	
	// Interesting factors
	private final static double MERCIFUL_GOD_FACTOR = 2.;	// 1 -> total of distribution = total need, < 1 -> dist < need
	private final static double WILLINGNESS_TO_TRADE = 1;	// chance that a trade even takes place at all (independent of rest)
	private final static double GOLD_DIG_FACTOR = 0.5;		// 0: Only trade with people you like, 1: Only trade with poor people

	// Graphs
	private Chart livingAgents;
	private Chart totalWealth;
	private Chart wealthNeighbours;
	private Chart neighboursPerAgent;

	// Bookkeeping
	private ChartContainer chartContainer = new ChartContainer(WIDTH + 10, 100, PANEL_WIDTH - 80, 120);
	private LinkedList<Agent> agents;
	private LinkedList<Edge> edges;
	private int tickCounter = 0;
	private long fpsTime = System.currentTimeMillis();
	private long fpsCounter = 0;
	private long fps = 0;

	private God() {

		// Create lists
		agents = new LinkedList<Agent>();
		edges = new LinkedList<Edge>();


		// Create graphs
		livingAgents = new XYChart("Number of living agents", "Time", "Living Agents");
		totalWealth = new XYChart("Wealth of all living agents", "Time", "Total Wealth");
		wealthNeighbours = new Histogram("Wealth compared to amount of neighbours", "Neighbours", "Wealth");
		neighboursPerAgent = new Histogram("Neighbours per Agent", "Neighbours", "Agents");
		totalWealth.setMaxSize(500);

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
            Agent a = new Agent(r.nextInt(STARTING_CAPITAL), WILLINGNESS_TO_TRADE, GOLD_DIG_FACTOR, x, y);

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

    private void assignNeighbours() {
        Random r = new Random();
        double maxLength = getDistance(0, 0, WIDTH, HEIGHT);
        LinkedList<Agent> others = (LinkedList<Agent>) agents.clone();
        for (Agent me : agents) {
            for (Agent other : others) {
                // don't make connections to itself
                if (me == other) {
                    continue;
                }
                // the higher the ratio the closer two agents are
                double distance = getDistance(me.posX, me.posY, other.posX, other.posY);
                double ratio = Math.pow(1 - distance / maxLength, 1. / (1 - PROXIMITY)); // bit
                // of
                // a
                // complicated
                // function
                if (ratio > r.nextDouble()) {
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
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		while (true) {
			oli.rule();
			oli.paintImmediately(0, 0, WIDTH + PANEL_WIDTH, HEIGHT);
		}


	}

	private void rule() {
		
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
			if (agent.requestResource() < 0) {
				agent.die();
				parcelCount[agent.posX / TILE_SIZE][agent.posY / TILE_SIZE]--;
				toRemove.add(agent);
			} else{
				totalWealthCounter += agent.requestResource();
			}
		}

		// delete dead agents and mark edges to be removed
		LinkedList<Edge> edgesToRemove = new LinkedList<Edge>();
		for (Agent a : toRemove) {
			System.out.println("Agent " + a.id + " has passed away.");
			agents.remove(a);
			for (Edge e : edges) {
				if (e.a1 == a || e.a2 == a) {
					edgesToRemove.add(e);
				}
			}
		}

		// delete edges
        edges.removeAll(edgesToRemove);
		
		// copmute stuff for graphs
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
			g.fillOval(a.posX - RADIUS, a.posY - RADIUS, 2 * RADIUS, 2 * RADIUS);
			g.setColor(Color.black);
			if(showInfo){
				g.drawString("   ID: " + a.id + ", " + Double.toString(a.requestResource()).substring(0, 6), a.posX, a.posY);
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
		g.drawString("TPS: " + fps, WIDTH + PANEL_WIDTH - 100, 20);
		
		// Draw graphs
		chartContainer.paintComponent(g);
	}

	private double distFunction(int xPos, int yPos, int t){
		double x = ((double)xPos / WIDTH) * 6;
		double y = ((double)yPos / HEIGHT) * 6;
		double aux0 = Math.sin(x*Math.cos(x) + (double)tickCounter/10.) * Math.cos(y * Math.sin(x) );
		double aux1 = Math.sin(x + (double)tickCounter/500) * Math.cos(y + (double)tickCounter/500);
		double aux2 = Math.sin(x) * Math.cos(y);
		double aux3 = Math.sin(x * Math.sin(y) + (double)tickCounter/10) * Math.cos(y + (double)tickCounter/30);
		Random r = new Random();
		double aux4 = r.nextDouble();
		return (aux1 + 1)/2;
		//return aux4;
	}
	
	private double getDistance(int x1, int y1, int x2, int y2) {
		return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
	}


}
