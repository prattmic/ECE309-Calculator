import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.Array;

import javax.swing.*;

public class Grapher extends JPanel implements MouseListener {
	private JFrame window = new JFrame("Graph");
	double[] aYValues;
	double[] aXValues;
	private JPanel mainPanel= new JPanel();
	double xUnit;
	double chosenX=1.1;
	double chosenY=1.1;
	int y_m;
	int x_m;
	
	public static void main(String args[]) {	
		
	}

	@Override
	public void paint(Graphics g) {
		int ScreenHeight = window.getHeight();
		int ScreenWidth = window.getWidth();
		int xTickInc; //spacing between |'s in pixels! 
		int yTickInc;
		int i;
		double smallY=0, bigY=0, currentY=0;
		for(i=0;i<aYValues.length;i++){
			currentY = aYValues[i];
			if(currentY < smallY){smallY = currentY;}
			if(currentY > bigY){bigY = currentY;}
		}
		double smallX=0, bigX=0, currentX=0;
		for(i=0;i<aXValues.length;i++){
			currentX = aXValues[i];
			if(currentX < smallX){smallX = currentX;}
			if(currentX > bigX){bigX = currentX;}
		}
		
		double yUnit = ScreenHeight / (bigY-smallY);
		double xUnit = ScreenWidth / (bigX-smallX);
		double yScaleArray[] = yScaleValues(smallY,bigY); 
		//we have the YScale!
		yTickInc = ScreenHeight / yScaleArray.length; 
		//we have the increment between y values!
		double xScaleArray[] = xScaleValues(aXValues);
		//we have the xScale!
		xTickInc = ScreenWidth / xScaleArray.length;
		//draw axes first
		g.drawLine(0, ScreenHeight/2, ScreenWidth, ScreenHeight/2);
		g.drawLine(ScreenWidth/2,0,ScreenWidth/2,ScreenHeight);
		//now draw the 'ticks' and values for the scale
		//xaxis
		for(i=0;i<xScaleArray.length;i++){
		//g.drawLine((xTickInc*i), (ScreenHeight/2)-3, (xTickInc*i), (ScreenHeight/2)+3);
		//g.drawChars(String.valueOf((int) Math.round(xScaleArray[i])).toCharArray(), 0, String.valueOf((int) Math.round(xScaleArray[i])).length(), (xTickInc*i), (ScreenHeight/2)+14);
		g.drawLine((int)(xUnit*(xScaleArray[i]-smallX)), (ScreenHeight/2)-3, (int)(xUnit*(xScaleArray[i]-smallX)), (ScreenHeight/2)+3);
		g.drawChars(String.valueOf((int) Math.round(xScaleArray[i])).toCharArray(), 0, String.valueOf((int) Math.round(xScaleArray[i])).length(), (int)(xUnit*(xScaleArray[i]-smallX)), (ScreenHeight/2)+14);
		}
		//yaxis
		for(i=0;i<yScaleArray.length;i++){
		//g.drawLine((ScreenWidth/2)-3,(yTickInc*(i-1)),(ScreenWidth/2)+3,(yTickInc*(i-1)));
		//g.drawChars(String.valueOf((int) Math.round(yScaleArray[i])).toCharArray(), 0, String.valueOf((int) Math.round(yScaleArray[i])).length(), (ScreenWidth/2)+5, (ScreenHeight)-(yTickInc*(i-1))-yTickInc);
		g.drawLine((ScreenWidth/2)-3,(int)(yUnit*(yScaleArray[i]-smallY)),(ScreenWidth/2)+3,(int)(yUnit*(yScaleArray[i]-smallY)));
		g.drawChars(String.valueOf((int) Math.round(yScaleArray[i])).toCharArray(), 0, String.valueOf((int) Math.round(yScaleArray[i])).length(), (ScreenWidth/2)+5, (int)((ScreenHeight)-(yUnit*(yScaleArray[i]-smallY))));
		}
		//draw our equation!
		for(i=0;i<aYValues.length-1;i++){
			g.drawLine((int)( xUnit*(aXValues[i]-smallX)),(int)(ScreenHeight - ((aYValues[i]-smallY)*yUnit)),(int) (xUnit *(aXValues[i+1]-smallX)),(int) (ScreenHeight - ((aYValues[i+1]-smallY)*yUnit)));
			//g.drawLine((int) ((xScaleArray[i])*xUnit),(int)(ScreenHeight - (yScaleArray[i]*yUnit)-yTickInc),(int) (xScaleArray[i+1]*xUnit),(int)(ScreenHeight - (yScaleArray[i+1]*yUnit) -yTickInc));
		}
		if(chosenX != 1.1){
			g.drawString("X =" + chosenX + "\n" + "Y =" + chosenY, x_m, y_m);
		}
		
		
	}

	public Grapher(double[] xvalues, double[] yvalues, String expresssion, Accumulator calc) {
		window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		window.add(this, SwingConstants.CENTER);
		//set the x and y values
		aYValues = new double[yvalues.length];
		aYValues = yvalues;
		aXValues = new double[xvalues.length];
		aXValues = xvalues;
		window.addMouseListener(this);
		//set some default size
		window.setSize(500, 500);
		window.setVisible(true);
	}

	// Some of this is from Bowman's example, some not...
	
	public static double[] xScaleValues(double[] xValues) {
		double[] xScaleArray=new double[xValues.length];
		//we'll show 10 increments of x..
		double smallX=0, bigX=0, currentX=0;
		for(int i=0;i<xValues.length;i++){
			currentX = xValues[i];
			if(currentX < smallX){smallX = currentX;}
			if(currentX > bigX){bigX = currentX;}
		}
		double range = bigX - smallX;
		double increment = range/10;
		xScaleArray[0] = smallX;
		for(int i = 1;i<=10;i++){
		xScaleArray[i] = xScaleArray[i-1]+increment;	
		}
		return xScaleArray;
		
	}
	public static double[] yScaleValues(double yMin, double yMax) {
		double exactRange;
		int roundedRange = 0, initialIncrement = 0, upperIncrement, lowerIncrement, selectedIncrement, numberOfYscaleValues, lowestYscaleValue, highestYscaleValue;
		double[] yScaleArray = null;
		int firstCheck = 10;
		String zeros = "0000000000";
		try {
			// we'll assume they just switched the values if yMin > yMax
			if (yMin > yMax) {
				double temp = yMax;
				yMax = yMin;
				yMin = temp;
			}
		} catch (NumberFormatException nfe) {
			System.out.println("Both input parms must be numeric.");
			return null;
		}

		exactRange = yMax - yMin;
		boolean checked = false;
		while (checked == false) {
			if (exactRange > firstCheck) {
				roundedRange = (int) exactRange;
				checked = true;
			} else {
				firstCheck = firstCheck / 10;
			}
		}

		initialIncrement = roundedRange / firstCheck;
		String initialIncrementString = String.valueOf(initialIncrement);
		String leadingDigit = initialIncrementString.substring(0, 1);
		int leadingNumber = Integer.parseInt(leadingDigit);
		int bumpedLeadingNumber = leadingNumber + 1;
		String bumpedLeadingDigit = String.valueOf(bumpedLeadingNumber);
		String upperIncrementString = bumpedLeadingDigit
				+ zeros.substring(0, initialIncrementString.length() - 1);
		String lowerIncrementString = leadingDigit
				+ zeros.substring(0, initialIncrementString.length() - 1);
		upperIncrement = Integer.parseInt(upperIncrementString);
		lowerIncrement = Integer.parseInt(lowerIncrementString);

		int distanceToUpper = upperIncrement - initialIncrement;
		int distanceToLower = initialIncrement - lowerIncrement;
		if (distanceToUpper > distanceToLower)
			selectedIncrement = lowerIncrement;
		else
			selectedIncrement = upperIncrement;
		

		numberOfYscaleValues = 0;
		lowestYscaleValue = 0;
		if (yMin < 0) {
			for (; lowestYscaleValue > yMin; lowestYscaleValue -= selectedIncrement)
				numberOfYscaleValues++;
		}
		if (yMin > 0) {
			for (; lowestYscaleValue < yMin; lowestYscaleValue += selectedIncrement)
				numberOfYscaleValues++;
			numberOfYscaleValues--;
			lowestYscaleValue -= selectedIncrement;
		}
		

		numberOfYscaleValues = 1;
		for (highestYscaleValue = lowestYscaleValue; highestYscaleValue < yMax; highestYscaleValue += selectedIncrement)
			numberOfYscaleValues++;
yScaleArray = new double[numberOfYscaleValues];
		for(int i=0;i<numberOfYscaleValues;i++){
			yScaleArray[i] = (i*selectedIncrement)+lowestYscaleValue;
		}
		
		
		return yScaleArray;
	}

	public double[] yScaleHolder(double[] yscalehold){
		return yscalehold;
	}
	
public double[] xScaleHolder(double[] xscalehold){
		return xscalehold;
	}

@Override
public void mouseClicked(MouseEvent arg0) {
	
}

@Override
public void mouseEntered(MouseEvent arg0) {
	
}

@Override
public void mouseExited(MouseEvent arg0) {
	
}

@Override
public void mousePressed(MouseEvent arg0) {
	x_m = arg0.getX();
	y_m = arg0.getY();
	int x = (int) Math.round(arg0.getX()/xUnit);
	int length = aYValues.length;
	length = length - 1;
	double diff=0;
	diff = Math.abs(x - aXValues[0]);
	double currentDiff;
	int chosenIndex=0;
	for(int i=1;i<=length;i++){
	currentDiff = Math.abs(aXValues[i]-x);
	if(currentDiff < diff){
		diff = currentDiff;
	chosenIndex = i;
	}
	}
	System.out.println("values: "+ "\n"+aXValues[chosenIndex]+ " " + aYValues[chosenIndex]);
	chosenY = aYValues[chosenIndex];
	chosenX = aXValues[chosenIndex];
	
	this.repaint();
}

@Override
public void mouseReleased(MouseEvent arg0) {
	chosenX = 1.1;
	chosenY = 1.1;
	this.repaint();
}

}
