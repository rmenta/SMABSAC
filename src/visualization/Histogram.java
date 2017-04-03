package visualization;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Iterator;

public class Histogram extends Chart{
	
	public Histogram(String graphName, String xAxisLabel, String yAxisLabel) {
		super(graphName, xAxisLabel, yAxisLabel);
	}

	@Override
	public void paintComponent(Graphics g, int posX, int posY, int width, int height) {
		// paint frame and descriptions
		super.paintComponent(g, posX, posY, width, height);
		
		// Calculate column values
		double colWidth = (float)width / xCoords.size();
		double colHeightMax = computeMaxValue();
		int plotPosY = posY + height;
		
		// Draw histogram
		Iterator<Integer> yCoordsIt = yCoords.iterator();
		for(int i = 0; i < xCoords.size(); i++){
			
			// Calculate needed values
			int colPosX = posX + (int)(i * colWidth);
			int colHeight = (int)(((height * 0.9) / colHeightMax)*yCoordsIt.next());

			// Draw columns
			g.setColor(Color.lightGray);
			g.fillRect(colPosX, plotPosY - colHeight, (int)Math.ceil(colWidth), colHeight);
			
			// Draw average line
			int avgVal = computeAverage();
			g.setColor(Color.blue);
			g.drawLine(posX, plotPosY - (int)(((height * 0.9) / colHeightMax)*avgVal), posX + width, plotPosY - (int)(((height * 0.9) / colHeightMax)*avgVal));
		}
	}
	
	private int computeAverage(){
		int totalVal = 0;
		for(Integer val : yCoords){
			totalVal += val;
		}
		
		return totalVal / yCoords.size();
	}
}
