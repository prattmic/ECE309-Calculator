import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.math.BigDecimal;
import java.util.Set;

/* ECE309 Calculator Project
 * Michael Pratt
 * Dennis Penn
 * David Wilson
 */

@SuppressWarnings("serial")
public class Accumulator extends JApplet implements ActionListener, KeyListener ,Runnable{
	private static final double comparisonPrecision = 0.01;
	private JFrame	window     = new JFrame("ECE309 Calculator - Accumulator Mode");
	private JPanel	northPanel	= new JPanel();
	private JPanel	centerPanel	= new JPanel();
	private JPanel	southPanel	= new JPanel();
	private JPanel	xPanel	= new JPanel();
	private JButton	evalButton	= new JButton("Evaluate");
	private JButton clearButton	= new JButton("Clear Sum");
	private JButton recallButton = new JButton("Recall");
	private JButton xButton     = new JButton("Set x (and any parameters)");
	private JTextArea xTextArea = new JTextArea();
	private JTextArea xIncTextArea = new JTextArea();
	private JTextArea xMaxTextArea = new JTextArea();
	private JTextArea inputTextArea  = new JTextArea();
	private JTextArea answerTextArea = new JTextArea("Sum: 0" + "\n" );
	private	JTextArea logTextArea	= new JTextArea();
	private JScrollPane xScrollPane = new JScrollPane(xTextArea);
	private JScrollPane xIncScrollPane= new JScrollPane(xIncTextArea);
	private JScrollPane xMaxScrollPane = new JScrollPane(xMaxTextArea);
	private JScrollPane inputScrollPane  = new JScrollPane(inputTextArea);
	private JScrollPane logScrollPane = new JScrollPane(logTextArea);
	private JScrollPane answerScrollPane = new JScrollPane(answerTextArea);
	private JSplitPane	splitOutput	= new JSplitPane(JSplitPane.VERTICAL_SPLIT,
			answerScrollPane, logScrollPane);
	private Container pane=window.getContentPane();
	private JMenuBar mb = new JMenuBar();
	private JMenu menuMode = new JMenu("Mode");
	private JMenuItem accumItem= new JMenuItem("Accumulator");
	private JMenuItem calcItem= new JMenuItem("Calculator");
	private JMenuItem compareItem= new JMenuItem("Comparator");
	private ButtonGroup radioGroup = new ButtonGroup();
	private JRadioButton accumRadio   = new JRadioButton("Accumulator"  , true);
	private JRadioButton calcRadio   = new JRadioButton("Calculator"   , false);
	private JRadioButton compareRadio = new JRadioButton("Comparator", false);
	private JPanel radioPanel = new JPanel();	
	private BigDecimal accumSum=BigDecimal.valueOf(0);
	private String prevSum="0";
	private String toEval;
	private String recallVal = null;
	private String x = null;
	private String xMin = null;
	private String xMax = null;
	private String xInc = null;
	private JLabel reminderLabel = new JLabel("Enter an expression");
	private JLabel xIncLabel = new JLabel("OR in increments of");
	private JLabel xMaxLabel = new JLabel("to X =");
	private JLabel xMinLabel = new JLabel("for X =");
	private boolean maxMode=false, incMode=false;
	public Accumulator() {
		window.setJMenuBar(mb);
		pane.setLayout(new GridLayout(3,1));
		mb.add(menuMode);
		menuMode.add(accumItem);
		menuMode.add(calcItem);
		menuMode.add(compareItem);
		//Input area formatting
		inputTextArea.setLineWrap(true);
		inputTextArea.addKeyListener(this);
		northPanel.setLayout(new GridLayout(2,1,0,-40));
		northPanel.add(reminderLabel);
		northPanel.add(inputScrollPane);

		//Buttons area formatting
		centerPanel.setLayout(new GridLayout(7,2));
		xPanel.setLayout(new GridLayout(1,6,0,0));
		xMinLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		xMaxLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		xIncLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		xPanel.add(xMinLabel);
		xPanel.add(xScrollPane);
		xPanel.add(xMaxLabel);
		xPanel.add(xMaxScrollPane);
		xPanel.add(xIncLabel);
		xPanel.add(xIncScrollPane);

		centerPanel.add(xPanel);
		centerPanel.add(xButton);
		xButton.addActionListener(this);
		centerPanel.add(recallButton);
		recallButton.addActionListener(this);
		centerPanel.add(evalButton);
		evalButton.addActionListener(this);
		clearButton.addActionListener(this);
		centerPanel.add(clearButton);
		accumItem.addActionListener(this);
		calcItem.addActionListener(this);
		compareItem.addActionListener(this);
		radioGroup.add(accumRadio);
		radioGroup.add(calcRadio);
		radioGroup.add(compareRadio);
		radioPanel.add(accumRadio);
		radioPanel.add(calcRadio);
		radioPanel.add(compareRadio);
		centerPanel.add(radioPanel);
		accumRadio.addActionListener(this);
		calcRadio.addActionListener(this);
		compareRadio.addActionListener(this);


		//Output area and output log formatting
		southPanel.setLayout(new GridLayout(1,1));
		splitOutput.setDividerLocation(60);
		southPanel.add(splitOutput, "Center");
		answerTextArea.setLineWrap(true);
		answerTextArea.setEditable(false);
		logTextArea.setEditable(false);
		logTextArea.setText("Output Log:"+"\n");
		pane.add(northPanel);
		pane.add(centerPanel);
		pane.add(southPanel);
		window.setSize(580,550);

		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(true);
		new Thread(this).start();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("ECE309 Calculator Project\nTeam 9\nMichael Pratt\nDennis Penn\nDavid Wilson");
		new Accumulator();
	}

	@Override
	public void run() {

	}


	@Override

	public void actionPerformed(ActionEvent evt) {
		inputTextArea.requestFocusInWindow();
		toEval = inputTextArea.getText().trim();
		if (evt.getSource() == evalButton){
			doEvaluation();
			if((maxMode == false) && (incMode==false)){
			//notta, neither mode is possible
			}
			else{
				doGraphing();
			}
			}
		//GUI may or may not change as these are selected.
		if(evt.getSource() == accumItem || evt.getSource() == accumRadio){
			window.setTitle("ECE309 Calculator - Accumulator Mode");
			accumRadio.setSelected(true);
			clearButton.setEnabled(true);
			answerTextArea.setText("Sum: "+prevSum);
		}
		if(evt.getSource() == calcItem || evt.getSource() == calcRadio){
			window.setTitle("ECE309 Calculator - Calculator Mode");
			calcRadio.setSelected(true);
			clearButton.setEnabled(false);
		}
		if(evt.getSource() == compareItem || evt.getSource() == compareRadio){
			window.setTitle("ECE309 Calculator - Comparator Mode");
			compareRadio.setSelected(true);
			clearButton.setEnabled(false);
		}
		if (evt.getSource() == clearButton){
			if(accumRadio.isSelected()){
				accumSum=BigDecimal.valueOf(0);
				prevSum="0";	
				logTextArea.append("Sum cleared."+"\n");
				answerTextArea.setText("Sum: 0" + "\n"); 
			}
			else if(calcRadio.isSelected()){
				//notta, don't need sum here (I think)
			}
			else{
				//for comparator, sum unneeded so far
			}
		}
		if (evt.getSource() == xButton) {
			try
			{
				Double.parseDouble(xTextArea.getText());
			}
			catch(Exception e)
			{
				answerTextArea.setText("Must enter a value for x.");
				return;
			}
			x = xTextArea.getText();
			answerTextArea.setText("The value of x has been set to: " + x);


			if((xMaxTextArea.getText().trim().equals("") == false)&&(xIncTextArea.getText().trim().equals("") == false)){
				maxMode = true;
				incMode = false;
				String noteString = "NOTE: If the maximum X value and X increment are both set, then the program will automatically " +
						"use the maximum x value for graphing." + "\n" + "Clear the maximum x value and hit the set button if you wish to graph by increments.";
				String answerTxt = answerTextArea.getText();
				answerTextArea.setText(answerTxt + "\n"+ noteString + "\n");
			}

			if(xMaxTextArea.getText().trim().equals("") == false){
				xMax = xMaxTextArea.getText();
				//check xmax is double
				try
				{
					Double.parseDouble(xMax);
				}
				catch(Exception e)
				{
					answerTextArea.setText("The maximum value for x is invalid");
					return;
				}
				//check to make sure the max is greater than our initial value
				if(Double.valueOf(x) >= Double.valueOf(xMax)){
					answerTextArea.setText("The maximum X value seems to be less or equal to the initial value. \n " +
							"Please increase it to be larger than the initial X.");
					return;
				}

				//
				String answerTxt = answerTextArea.getText();
				answerTextArea.setText(answerTxt + "\n" + "The xMax value has been set to: "+xMax);

			}
			if(xIncTextArea.getText().trim().equals("") == false){
				xInc = xIncTextArea.getText();
				//
				try
				{
					Double.parseDouble(xInc);
				}
				catch(Exception e)
				{
					answerTextArea.setText("The increment value for x is invalid");
					return;
				}
				String answerTxt = answerTextArea.getText();
				answerTextArea.setText(answerTxt + "\n" + "The xInc value has been set to: "+xInc);
			}
			if((xMaxTextArea.getText().trim().equals("") == true)&&(xIncTextArea.getText().trim().equals("") == false)){
				incMode = true;
				maxMode = false;
			}
			if((xMaxTextArea.getText().trim().equals("") == false)&&(xIncTextArea.getText().trim().equals("") == true)){
				maxMode = true;
				incMode = false;
			}
			if((xInc.trim().equals("") == true)&&(xMax.trim().equals("") == true)){
				maxMode = false;
				incMode = false;
			}
		}
		if (evt.getSource() == recallButton) {
			if(recallVal == null) {
				answerTextArea.setText("Must evaluate a expression first.");
			}
			inputTextArea.setText(recallVal);
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			inputTextArea.requestFocusInWindow();
			toEval = inputTextArea.getText().trim();
			inputTextArea.setText(toEval);
			doEvaluation();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	public void doEvaluation(){
		recallVal = toEval;
		if(accumRadio.isSelected()){
			//do accumulator mode evaluations 
			try {accumSum = Expression.simplify(toEval, x).add(accumSum);}
			catch(NumberFormatException nfe){
				answerTextArea.setForeground(Color.RED);
				answerTextArea.setText("Exception Encounted: " + nfe 
						+ ". Are you sure you entered a correct input?");		
				answerTextArea.setForeground(Color.BLACK);
				return;
			}
			inputTextArea.setText("");
			String evalAnswer = accumSum.setScale(2, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString();
			answerTextArea.setText("Sum: "+evalAnswer);
			logTextArea.append(prevSum + " + "+ toEval + " = "+ evalAnswer + "\n");
			prevSum = evalAnswer;
		}
		else if(calcRadio.isSelected()){
			//do calculator mode evaluations
			try {accumSum = Expression.simplify(toEval,x);}
			catch(NumberFormatException nfe){
				answerTextArea.setForeground(Color.RED);
				answerTextArea.setText("Exception Encounted: " + nfe 
						+ ". Are you sure you entered a correct input?");		
				answerTextArea.setForeground(Color.BLACK);
				return;
			}
			inputTextArea.setText("");
			String evalAnswer = accumSum.setScale(2, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString();
			answerTextArea.setText("Answer: "+evalAnswer);
			logTextArea.append(toEval + " = "+ evalAnswer + "\n");

		}
		else{
			//do the comparator mode evaluations
			int equalOffset = ((String) toEval).indexOf("=");
			if(toEval.length() == 0){return;}
			if(toEval.contains("=") == false){
				answerTextArea.setForeground(Color.RED);
				answerTextArea.setText("Exception Encounted: "  
						+ " Please enter an \"=\" sign.");		
				answerTextArea.setForeground(Color.BLACK);
				return;
			}
			inputTextArea.setText("");
			String left =((String) toEval).substring(0,equalOffset).trim();
			String right = ((String) toEval).substring(equalOffset+1).trim();
			try{
				if(Math.abs(Expression.simplify(left, x).subtract(Expression.simplify(right, x)).doubleValue()) < comparisonPrecision){
					answerTextArea.setText(left+ " = " + right + ": Correct!");
					logTextArea.append(left+ " = " + right + ": Correct!"+"\n");
				}
				else{
					answerTextArea.setText(left+ " = " + right + ": Oops.");
					logTextArea.append(left+ " = " + right + ": Oops."+"\n");
				}
			}
			catch(NumberFormatException nfe){
				answerTextArea.setForeground(Color.RED);
				answerTextArea.setText("Exception Encounted: " + nfe 
						+ ". Are you sure you entered a correct input?");		
				answerTextArea.setForeground(Color.BLACK);
				return;
			}
		}
		logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
		inputTextArea.setCaretPosition(0);
	}
	public void doGraphing(){
		double[] actualXValues;
		double[] actualXValuesMax = new double[11];//
		double[] actualXValuesInc = new double[11];//max is 20...
		double[] actualYValues; //whatever the length of our x is will be the number for y values
		if(maxMode == true){
			//get our x values
			int i;
			double realRange;
			realRange = Double.valueOf(xMax) - Double.valueOf(x);
			double realInc = (realRange / 10);
			actualXValuesMax[0] = Double.valueOf(x);
			for(i=1;i<=10;i++){
				actualXValuesMax[i] = (realInc)+actualXValuesMax[i-1];
			}
			//got our x values
			actualYValues = new double[actualXValuesMax.length];
			for(i=0;i<actualXValuesMax.length;i++){
				BigDecimal y = Expression.simplify(toEval,String.valueOf(actualXValuesMax[i]));
				actualYValues[i]= y.doubleValue();
			}
			//got our Y values
			actualXValues = actualXValuesMax;
		}
		else{
			//we're in the increment type mode	
			int numberOfXValues=0;
			double startValue = Double.valueOf(x);
			double xIncrement = Double.valueOf(xInc);
			actualXValuesInc[0]=startValue;
			for(int i=1;i<actualXValuesInc.length;i++){
				actualXValuesInc[i]= (actualXValuesInc[i-1]+xIncrement);
			}
			//got our x values
			actualYValues = new double[actualXValuesInc.length];
			for(int i=0;i<actualXValuesInc.length;i++){
				BigDecimal y = Expression.simplify(toEval,String.valueOf(actualXValuesInc[i]));
				actualYValues[i]= y.doubleValue();
			}
			//we have our actual y values
		actualXValues = actualXValuesInc;
		}
		//GOT ALL VALUES NOW FOR X AND Y
		//let the grapher do the rest!
		new Grapher(actualXValues, actualYValues, toEval,this);

		
		
		
	}
}
