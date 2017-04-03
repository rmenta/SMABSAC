package visualization;

import java.awt.Graphics;
import java.util.LinkedList;

public class ChartContainer {
	
	int posX, posY;
	int width, graphHeight;
	final int SPACE_BETWEEN_GRAPHS = 100;
	LinkedList<Chart> graphs;
	
	public ChartContainer(int posX, int posY, int width, int graphHeight) {
		this.posX = posX;
		this.posY = posY;
		this.width = width;
		this.graphHeight = graphHeight;
		
		graphs = new LinkedList<Chart>();
	}
	
	public void paintComponent(Graphics g){
		int graphPosY = posY;
		
		for(Chart graph : graphs){
			graph.paintComponent(g, posX + 50, graphPosY, width - 50, graphHeight);
			graphPosY += graphHeight + SPACE_BETWEEN_GRAPHS;
		}
	}
	
	// Graphs are added without coordinates
	public void addChart(Chart graph){
		graphs.add(graph);
	}
	
	public void removeGraph(Chart graph){
		graphs.remove(graph);
	}
}
