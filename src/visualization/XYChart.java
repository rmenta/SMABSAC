package visualization;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Iterator;

public class XYChart extends Chart{

	public XYChart(String graphName, String xAxisLabel, String yAxisLabel) {
		super(graphName, xAxisLabel, yAxisLabel);
	}

	@Override
	public void paintComponent(Graphics g, int posX, int posY, int width, int height) {
		super.paintComponent(g, posX, posY, width, height);
		
		// Calculate values
		double dist = (float)width / xCoords.size();
		double heightMax = computeMaxValue();
		int plotPosY = posY + height;
		int lastPosY = plotPosY - (int)((height/heightMax)*yCoords.getLast());
		
		// Draw XY Chart
		Iterator<Integer> yCoordsIt = yCoords.iterator();
		int posYOld = plotPosY - (int)((height/heightMax)*yCoordsIt.next());
		for(int i = 0; i < xCoords.size() - 1; i++){
			g.setColor(Color.lightGray);
			int posYNext = plotPosY - (int)((height/heightMax)*yCoordsIt.next());
			g.drawLine(posX + (int)(dist * i), posYOld, posX + (int)(dist * (i + 1)), posYNext);
			posYOld = posYNext;
		}
		
		// Draw line with current value
		g.setColor(Color.blue);
		g.drawLine(posX, lastPosY, posX + width, lastPosY);
	}

}
