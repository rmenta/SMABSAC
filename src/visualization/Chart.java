package visualization;

import java.awt.*;
import java.util.Iterator;
import java.util.LinkedList;


abstract public class Chart {
	// General properties of the graph
	private String graphName;
	private String xAxisLabel, yAxisLabel;
	protected LinkedList<Integer> xCoords, yCoords;
    
    // Representation variables
	private int maxSize;
	private int tickWidth = 5, tickHeight = 5;

	private boolean showTicksY = true;
    private boolean showLabelX = true;
    private Font valuesFont = new Font(Font.SANS_SERIF, 0, 12);
    private Font descriptionFont = new Font(Font.SANS_SERIF, 0, 12);

    public Chart(String graphName, String xAxisLabel, String yAxisLabel) {
        this.graphName = graphName;
        this.xAxisLabel = xAxisLabel;
        this.yAxisLabel = yAxisLabel;
        this.xCoords = new LinkedList<>();
        this.yCoords = new LinkedList<>();
    }

    public void addVal(int x, int y){
    	// Check for number of values in list
    	if(maxSize != 0 && xCoords.size() == maxSize){
    		xCoords.removeFirst();
    		yCoords.removeFirst();
    	}
    	
    	// Add new values
        xCoords.add(x);
        yCoords.add(y);
    }

    public void updateValues(LinkedList<Integer> xCoords, LinkedList<Integer> yCoords){
        this.xCoords = xCoords;
        this.yCoords = yCoords;
    }
    
    public void paintComponent(Graphics g, int posX, int posY, int width, int height){
    	//Draw descriptions
    	g.setFont(descriptionFont);
		g.setColor(Color.black);
    	g.drawString(graphName, posX, posY-5);
    	
    	//Set font
		if(showLabelX){g.drawString(xAxisLabel, posX + width/2 - strWidth(g, xAxisLabel)/2, posY + height + (int)(2.5*strHeight(g)));}
		
		// Draw frame
    	g.drawLine(posX, posY, posX, posY + height);
    	g.drawLine(posX, posY + height, posX + width, posY + height);
    	
    	//Calculate some values
    	double colWidth = (float)width / xCoords.size();
		double colHeightMax = computeMaxValue();
		int colPosY = posY + height;
		int intervalX = (int)Math.ceil((strWidth(g, xCoords.getLast())*1.3) / colWidth);
    	
    	// Draw horizontal ticks and values
		g.setFont(valuesFont);
		Iterator<Integer> xCoordsIt = xCoords.iterator();
		for(int i = 0; i < xCoords.size(); i++){
			// Calculate needed values
			int colPosX = posX + (int)(i * colWidth);
			String str = Integer.toString(xCoordsIt.next());
			
			// Draw horizontal ticks and write value below
			if(i%intervalX == 0){
				g.drawString(str, colPosX + (int)(colWidth/2 - strWidth(g, str) / 2), colPosY + 5 + (int)(1*strHeight(g)));
				boolean showTicksX = true;
				if(showTicksX){drawTickHor(g, colPosX + (int)(colWidth/2),  colPosY);}
			}
		}
		
		
		// Draw vertical ticks and write value below
		for(int i = 0; i < height/strHeight(g) + 1; i++){
			int tickVal = (int)(i * (colHeightMax / (height/strHeight(g))));
			String val = Integer.toString(tickVal);	
			if(tickVal > 10000){
				int exp = (int)Math.floor(Math.log10(tickVal));
				double base = (double)tickVal / Math.pow(10, exp);
				val = Double.toString(base).substring(0, 3) + "E" + Integer.toString(exp);
			}
			g.drawString(val, posX - strWidth(g, val) - tickWidth*2, colPosY - i*strHeight(g) + strHeight(g)/4);
			if(showTicksY){drawTickVert(g, posX - tickWidth, colPosY - i*strHeight(g));}
		}
    }
    
	protected void drawTickHor(Graphics g, int tickPosX, int tickPosY){
		g.drawLine(tickPosX, tickPosY, tickPosX, tickPosY + tickHeight);
	}
	
	protected void drawTickVert(Graphics g, int tickPosX, int tickPosY){
		g.drawLine(tickPosX, tickPosY, tickPosX + tickWidth, tickPosY);
	}
	
    protected int computeMaxValue(){
    	int maxVal = yCoords.getFirst();
    	for(Integer val : yCoords){
        	if(val > maxVal){
        		maxVal = val;
        	}
        }
    	return maxVal;
    }
    
    protected int strWidth(Graphics g, String str){
    	return g.getFontMetrics().stringWidth(str);
    }
    
    protected int strWidth(Graphics g, int no){
    	return g.getFontMetrics().stringWidth(Integer.toString(no));
    }
    
    protected int strHeight(Graphics g){
    	return g.getFontMetrics().getAscent();
    }
    
    public void setTickHeight(int tickHeight){
		this.tickHeight = tickHeight;
	}
    
    public void setTickWidth(int tickHeight){
		this.tickHeight = tickHeight;
	}
    
    public void setMaxSize(int s){
    	maxSize = s;
    }
    
    public void setDescriptionFont(Font f){
    	descriptionFont = f;
    }
    
    public void setValuesFont(Font f){
    	valuesFont = f;
    }
}
