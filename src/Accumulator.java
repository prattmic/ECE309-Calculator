import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.math.BigDecimal;

@SuppressWarnings("serial")
public class Accumulator extends JApplet implements ActionListener, KeyListener ,Runnable{
	private static final double comparisonPrecision = 0.01;
	private JFrame	window     = new JFrame("ECE309 Calculator - Accumulator Mode");
	private JPanel	northPanel	= new JPanel();
	private JPanel	centerPanel	= new JPanel();
	private JPanel	southPanel	= new JPanel();
	private JButton	evalButton	= new JButton("Evaluate");
	private JButton clearButton	= new JButton("Clear Sum");
	private JButton recallButton = new JButton("Recall");
	private JButton xButton     = new JButton("Set x");
	private JTextArea xTextArea = new JTextArea("Enter x value");
	private JTextArea inputTextArea  = new JTextArea();
	private JTextArea answerTextArea = new JTextArea("Sum: 0" + "\n" );
	private	JTextArea logTextArea	= new JTextArea();
	private JScrollPane xScrollPane = new JScrollPane(xTextArea);
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
	private JLabel reminderLabel = new JLabel("Only +, -, *, and / are allowed. Operands can only be numbers.");
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
		centerPanel.setLayout(new GridLayout(6,2));
		//xTextArea.addKeyListener(this);
		centerPanel.add(xScrollPane);
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
		window.setSize(450,500);

		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(true);
		new Thread(this).start();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
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
			try {accumSum = Expression.simplify(toEval).add(accumSum);}
			catch(NumberFormatException nfe){
				answerTextArea.setForeground(Color.RED);
				answerTextArea.setText("Exception Encounted: " + nfe 
						+ ". Are you sure you entered a correct input?");		
				answerTextArea.setForeground(Color.BLACK);
				return;
			}
			inputTextArea.setText("");
			String evalAnswer = accumSum.setScale(2, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toString();
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
			String evalAnswer = accumSum.setScale(2, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toString();
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
				if(Math.abs(Expression.simplify(left).subtract(Expression.simplify(right)).doubleValue()) < comparisonPrecision){
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
	
}