import java.awt.*;

import javax.swing.*;

public class Graphing extends JPanel {
	private JFrame window = new JFrame("Graph");

	public static void main(String args[]) {	
		new Graphing();
	}

	@Override
	public void paint(Graphics g) {
		int ScreenHeight = window.getHeight();
		int ScreenWidth = window.getWidth();
	}

	public Graphing() {
		window.add(this, SwingConstants.CENTER);

	}

	// Some of this is from Bowman's example, some not...
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
		System.out.println("The closest even increment (and therefore the one chosen) = " + selectedIncrement);

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
		System.out.println("The lowest Y scale value will be " + lowestYscaleValue + ")");


		numberOfYscaleValues = 1;
		for (highestYscaleValue = lowestYscaleValue; highestYscaleValue < yMax; highestYscaleValue += selectedIncrement)
			numberOfYscaleValues++;
		System.out.println("The highest Y scale value will be "
				+ highestYscaleValue);
		System.out.println("The number of Y scale click marks will be "
				+ numberOfYscaleValues);
yScaleArray = new double[numberOfYscaleValues];
		for(int i=0;i<numberOfYscaleValues;i++){
			yScaleArray[i] = (i*selectedIncrement)+lowestYscaleValue;
		}
		
		
		return yScaleArray;
	}

	

}