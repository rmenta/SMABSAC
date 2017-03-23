package visualization;

import java.awt.*;
import java.util.LinkedList;


/**
 * Created by God on 23.03.2017.
 */
public class Graph {
    private String graphName;
    private String xAxisLabel, yAxisLabel;
    private LinkedList<Integer> xCoords, yCoords;
    private final static int WIDTH = 160, HEIGHT = 100;

    private int posX, posY;

    public Graph(String graphName, String xAxisLabel, String yAxisLabel, int posX, int posY) {
        this.posY =posY;
        this.posX = posX;
        this.graphName = graphName;
        this.xAxisLabel = xAxisLabel;
        this.yAxisLabel = yAxisLabel;
        this.xCoords = new LinkedList<>();
        this.yCoords = new LinkedList<>();
    }

    public void addVal(int x, int y){
        xCoords.add(x);
        yCoords.add(y);

    }

    public void updateValues(LinkedList<Integer> xCoords, LinkedList<Integer> yCoords){
        this.xCoords = xCoords;
        this.yCoords = yCoords;

    }
    
    public void paintComponent(Graphics g){
    	int textHeight = g.getFontMetrics().getHeight();
    	g.drawString(graphName, posX, posY);
        
    	//Draw frame and marks
    	g.drawLine(posX, posY + textHeight, posX, posY + textHeight + HEIGHT);
    	g.drawLine(posX, posY + textHeight + HEIGHT, posX + WIDTH, posY + textHeight + HEIGHT);
    }
    
}
