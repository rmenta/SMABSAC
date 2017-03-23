package visualization;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import model.*;

/**
 * Doing all the dirty stuff and initialization
 *GEEILE SHIIIIT
 * !!!!!
 * @author renato
 */
public class God extends JPanel {
	// Properties of the visualization
	private final static int NUMBER_OF_AGENTS = 500;
	private final static int WIDTH = 1720;
	private final static int HEIGHT = 1080;
	private final static int PANEL_WIDTH = 200;
	private final static int RADIUS = 5;
	private final static boolean showInfo = true;
	public final static double PROXIMITY = 0.95;
	private final static int TILE_SIZE = 30;
	private final static int SLEEP_TIMER = 10;
	private final static double MERCIFUL_GOD_FACTOR = 1.3; // 1 -> total of distribution = total need, < 1 -> dist < need

	// Properties of the model
	private final static int COST_OF_LIFE = 1;
	private final static int STARTING_CAPITAL = 500;

	private LinkedList<Triple> agents;
	private LinkedList<Edge> edges;
	private int tickCounter = 0;

	public God() {
		agents = new LinkedList<Triple>();
		edges = new LinkedList<Edge>();

		// Create agents at random locations
		Random r = new Random();
		System.out.println("Create " + NUMBER_OF_AGENTS + " agents...");
		while (agents.size() < NUMBER_OF_AGENTS) {

			// Create agent
			Agent a = new Agent(r.nextInt(STARTING_CAPITAL));
			int x = RADIUS + r.nextInt(WIDTH - 10 * RADIUS);
			int y = RADIUS + r.nextInt(HEIGHT - 10 * RADIUS);

			// Check if space is not yet occupied
			boolean tooClose = false;
			for (Triple other : agents) {
				if (getDistance(x, y, other.x, other.y) < 3 * RADIUS) {
					tooClose = true;
				}
			}

			// add newly created agent to list
			if (!tooClose) {
				agents.add(new Triple(a, x, y));
			}
		}

		// Create connections between agents.
		System.out.println("Create edges...");
		double maxLength = getDistance(0, 0, WIDTH, HEIGHT);
		LinkedList<Triple> others = (LinkedList<Triple>) agents.clone();
		for (Triple me : agents) {
			for (Triple other : others) {
				// don't make connections to itself
				if (me == other) {
					continue;
				}
				// the higher the ratio the closer two agents are
				double distance = getDistance(me.x, me.y, other.x, other.y);
				double ratio = Math.pow(1 - distance / maxLength, 1. / (1 - PROXIMITY)); // bit
																							// of
																							// a
																							// complicated
																							// function
				if (ratio > r.nextDouble()) {
					edges.add(new Edge(me, other));
					me.a.neighbours.add(new Neighbour(other.a, 0));
					other.a.neighbours.add(new Neighbour(me.a, 0));
				}
			}

			// Remove node from list to avoid duplication
			others.remove(me);
		}
	}

	public static void main(String[] args) {
		God renato = new God();

		JFrame frame = new JFrame("[SMABSAC]");
		frame.add(renato);
		frame.setSize(WIDTH + PANEL_WIDTH, HEIGHT);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		while (true) {
			renato.rule();
			renato.repaint();
			try {
				Thread.sleep(SLEEP_TIMER);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// make svg-file
		// renato.createSVG("svgGraph.html");
		// System.out.println("SVG saved!");
	}

	public void rule() {
		
		//Increase tick counter by one
		tickCounter++;
		
		// Let agents do their stuff
		for (Triple agent : agents) {
			agent.a.proposeTrade();
		}

		for (Triple agent : agents) {
			agent.a.trade();
		}

		// life's a bitch
		double sumDistribution = 0;
		for(Triple agent : agents){
			sumDistribution += distFunction(agent.x, agent.y, tickCounter);
		}
		double factor = 1 / sumDistribution;
		
		LinkedList<Triple> toRemove = new LinkedList<Triple>();
		for (Triple agent : agents) {
			int salary = (int)(factor * distFunction(agent.x, agent.y, tickCounter) * (COST_OF_LIFE * agents.size() * MERCIFUL_GOD_FACTOR));
			agent.a.getWhatYouDeserve(salary - COST_OF_LIFE);
			if (agent.a.requestResource() < 0) {
				agent.a.die();
				toRemove.add(agent);
			}
		}

		// delete dead agents & edges
		LinkedList<Edge> edgesToRemove = new LinkedList<Edge>();
		for (Triple a : toRemove) {
			agents.remove(a);
			for (Edge e : edges) {
				if (e.a1 == a || e.a2 == a) {
					edgesToRemove.add(e);
				}
			}
		}

		for (Edge e : edgesToRemove) {
			edges.remove(e);
		}

	}

	@Override
	public void paint(Graphics g) {
		
		// Draw background
		g.setColor(Color.white);
		g.fillRect(0, 0, WIDTH + PANEL_WIDTH, HEIGHT);
		
		// Draw dist in background
		for(int i = 0; i < WIDTH; i+=TILE_SIZE){
			for(int j = 0; j < HEIGHT; j+=TILE_SIZE){
				g.setColor(new Color(255, (int)(255 * distFunction(i, j, tickCounter)), 255));
				g.fillRect(i, j, TILE_SIZE, TILE_SIZE);
			}
		}


		// Draw all the edges
		g.setColor(Color.gray);
		for (Edge e : edges) {
			g.drawLine(e.a1.x, e.a1.y, e.a2.x, e.a2.y);
		}

		// Draw last transactions
		Graphics2D g2 = (Graphics2D) g;
		for (Triple a : agents) {
			Agent p = a.a.lastPartner;
			if (p != null) {
				for (Triple t : agents) {
					if (t.a == p) {
						g2.setStroke(new BasicStroke(2));
						g2.setColor(Color.black);
						g2.drawLine(a.x, a.y, t.x, t.y);
						break;
					}
				}
			}
		}

		// Draw all the agents
		g.setColor(Color.black);
		for (Triple a : agents) {
			g.setColor(new Color(0, Math.min(255, a.a.requestResource()), 0));
			g.fillOval(a.x - RADIUS, a.y - RADIUS, 2 * RADIUS, 2 * RADIUS);
			g.setColor(Color.black);
			if(showInfo){
				g.drawString("   ID: " + a.a.id + ", " + a.a.requestResource(), a.x, a.y);
			}
		}

		// draw infos
		g.drawString("Number of Agents: " + agents.size(), WIDTH + 10, 20);
		g.drawString("Number of Edges:  " + edges.size(), WIDTH + 10, 40);
	}

	private double distFunction(int xPos, int yPos, int t){
		double x = ((double)xPos / WIDTH) * 6;
		double y = ((double)yPos / HEIGHT) * 6;
		double aux = Math.sin(x + (double)tickCounter/10) * Math.cos(y + (double)tickCounter/10);
		return (aux + 1)/2;
	}
	
	private double getDistance(int x1, int y1, int x2, int y2) {
		return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
	}

	private void createSVG(String path) {
		FileWriter fw = null;
		try {
			fw = new FileWriter(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedWriter bw = new BufferedWriter(fw);

		// Write svg-file
		try {
			// Write header stuff
			bw.write("<html><body><svg width=\"" + WIDTH + "\" height=\"" + HEIGHT + "\">\n");

			// Write edges
			for (Edge e : edges) {
				bw.write("<line x1=\"" + e.a1.x + "\" y1=\"" + e.a1.y + "\" x2=\"" + e.a2.x + "\" y2=\"" + e.a2.y
						+ "\" style=\"stroke:rgb(100, 100, 100);stroke-width:1\"/>\n");
			}

			// Write nodes
			for (Triple a : agents) {
				bw.write("<circle cx=\"" + a.x + "\" cy=\"" + a.y + "\" r=\"" + RADIUS + "\" />\n");
			}

			// Write bottom
			bw.write("</svg></body></html>");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bw != null) {
					bw.close();
				}

				if (fw != null) {
					fw.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}

		}
	}
}
