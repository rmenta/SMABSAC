package visualization;

import java.awt.*;
import java.util.Iterator;
import java.util.LinkedList;


/**
 * Created by God on 23.03.2017.
 */
public class Graph {
	// General properties of the graph
    private String graphName;
    private String xAxisLabel, yAxisLabel;
    private LinkedList<Integer> xCoords, yCoords;
    private final static int WIDTH = 360, HEIGHT = 150;
    
    // Position stuff
    private int posX, posY;
    private int plotPosX, plotPosY;
    private int plotWidth, plotHeight;
    
    // Representation variables
    private int maxValue;

    public Graph(String graphName, String xAxisLabel, String yAxisLabel, int posX, int posY) {
        this.posY = posY;
        this.posX = posX;
        this.graphName = graphName;
        this.xAxisLabel = xAxisLabel;
        this.yAxisLabel = yAxisLabel;
        this.xCoords = new LinkedList<>();
        this.yCoords = new LinkedList<>();
        
        // Calculate some infos
        plotPosY = posY + 10 + HEIGHT;
        plotPosX = posX + 20;
        plotWidth = WIDTH - 20;
        plotHeight = HEIGHT - 20;
    }

    public void addVal(int x, int y){
    	// Add new values
        xCoords.add(x);
        yCoords.add(y);
        
        // Check max value
        if(y > maxValue){
        	maxValue = y;
        }
    }

    public void updateValues(LinkedList<Integer> xCoords, LinkedList<Integer> yCoords){
        this.xCoords = xCoords;
        this.yCoords = yCoords;

    }
    
    public void paintComponent(Graphics g){
    	//Draw frame and marks, plot coordinates start at bottom left corner (not upper left)
    	g.setColor(Color.black);
    	g.drawLine(plotPosX, plotPosY - plotHeight, plotPosX, plotPosY);
    	g.drawLine(plotPosX, plotPosY, plotPosX + plotWidth, plotPosY);
    	
    	// Calculate needed values
    	Iterator<Integer> yIterator = yCoords.iterator();
    	double colWidth = (double)plotWidth / xCoords.size();
    	double scaleFactor = 1. / maxValue;
    	
    	// Plot the graph
    	g.setColor(Color.lightGray);
    	for(int i = 0; i < xCoords.size(); i++){
    		int colHeight = (int)(plotHeight * scaleFactor * yIterator.next());
    		g.fillRect(1 + (int)Math.ceil(plotPosX + i * colWidth), plotPosY - colHeight, (int)Math.ceil(colWidth), colHeight);
    	}
    	
    	// Draw infos of the graph
    	g.setColor(Color.black);
    	g.drawString(graphName, posX, posY);
    	g.drawString(yAxisLabel, posX, posY + 20);
    	g.drawString(xAxisLabel, plotPosX + plotWidth - xAxisLabel.length()*8, plotPosY + 20);
    	g.drawString(Integer.toString(maxValue), plotPosX + 5, plotPosY - plotHeight + 10);
    }
    
}
