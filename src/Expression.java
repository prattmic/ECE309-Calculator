import java.math.BigDecimal;
import java.util.regex.*;

/* ECE309 Calculator Project
 * Michael Pratt
 * Dennis Penn
 * David Wilson
 */

/**
 * Parses mathematical expressions.
 * Uses BigDecimal to ensure precision.
 */
public class Expression {
	private static boolean DEBUG = true;

	private static Pattern plus_minus = Pattern.compile("([\\+\\-])");
	private static Pattern mult_div = Pattern.compile("([\\*/])");
	
	public static void main(String args[]) {
		if (args.length != 1) {
			System.err.println("Please provide test input.");
		}
		
		String test = args[0];
		
		System.out.println(simplify(test));
	}
	
	public static BigDecimal simplify(String expression) {
		if (DEBUG) {
			System.err.println("Expression: " + expression);
		}
		
		try {	// If expression is a valid number, simply return it.
			return new BigDecimal(expression);
		} catch (NumberFormatException e) {}
		
		/* Perform simplification in reverse order of operations.
		 * As this function recurses down, the highest order of operations
		 * function will be performed first. */
		Matcher pm = plus_minus.matcher(expression);
		if (pm.find()) {
			String operation = pm.group();
			String before = expression.substring(0, pm.start());
			String after = expression.substring(pm.end());
			
			if (DEBUG) {
				System.err.printf("Found operation: '%s' '%s' '%s'\n", before, operation, after);
			}
			
			BigDecimal simple_before = simplify(before);
			BigDecimal simple_after = simplify(after);
			
			switch (operation) {
			case "+":
				return simple_before.add(simple_after);
			case "-":
				return simple_before.subtract(simple_after);
			default:
				throw new NumberFormatException("Invalid add/substract operator " + operation + " in " + expression);
			}
		}
		
		Matcher md = mult_div.matcher(expression);
		if (md.find()) {
			String operation = md.group();
			String before = expression.substring(0, md.start());
			String after = expression.substring(md.end());
			
			if (DEBUG) {
				System.out.printf("Found operation: '%s' '%s' '%s'\n", before, operation, after);
			}
			
			BigDecimal simple_before = simplify(before);
			BigDecimal simple_after = simplify(after);
			
			switch (operation) {
			case "*":
				return simple_before.multiply(simple_after);
			case "/":
				return simple_before.divide(simple_after);
			default:
				throw new NumberFormatException("Invalid multiply/divide operator " + operation + " in " + expression);
			}
		}
		
		/* Only reached on invalid input */
		throw new NumberFormatException("No operation found in " + expression);
	}
}