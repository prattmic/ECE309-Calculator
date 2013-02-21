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

	/* Matches any character except operators. */
	private static String non_op = "[^\\+\\-\\*/]";
	
	/* Matches any character except operators, minus '-',
	 * which is allowed after an operation as a negative sign. */
	private static String non_op_except_minus = "[^\\+\\*/]";
	
	/* Matches an addition or subtraction operator.  Requires a character
	 * before which is not an operator.  After, requires a character which
	 * is not an operator, except '-' is allowed as a negative sign. */
	private static Pattern plus_minus = Pattern.compile(non_op + "+([\\+\\-])" + non_op_except_minus + "+");
	
	/* Matches a multiplication or division operator.  Requires a character
	 * before which is not an operator.  After, requires a character which
	 * is not an operator, except '-' is allowed as a negative sign. */
	private static Pattern mult_div = Pattern.compile(non_op + "+([\\*/])" + non_op_except_minus + "+");
	
	public static void main(String args[]) {
		if (args.length == 1) {
			System.out.println(simplify(args[0]));
		}
		
		test("1", new BigDecimal(1));
		test("1+1", new BigDecimal(2));
		test("-1*1+1", new BigDecimal(0));
		test("-1*1+1/2", new BigDecimal(-0.5));
		test("2*2+2", new BigDecimal(6));
		test("5/-6+3/5*4", new BigDecimal(5.0/-6+3.0/5*4));
		test("5/-6+4*3/5", new BigDecimal(5.0/-6+3.0/5*4));
		test("1+1+1+1+1/4", new BigDecimal(4.25));
		test("1/4+1+1+1+1*10/5", new BigDecimal(5.25));
	}
	
	private static boolean test(String expression, BigDecimal expectedValue) {
		BigDecimal result = null;
		
		System.out.printf("Testing '" + expression + "'\t");
		
		try {
			result = simplify(expression);
		} catch (Exception e) {
			System.out.println("FAIL: Test raised exception " + e + "\n");
			return false;
		}
		
		if (Math.abs(result.subtract(expectedValue).doubleValue()) > 0.00001) {
			System.out.println("FAIL: Test returned " + result + ", expected " + expectedValue + "\n");
			return false;
		}
		
		System.out.printf("PASSED\n");
		return true;
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
			/* Characters required before plus/minus, so operation is group 1. */
			String operation = pm.group(1);
			String before = expression.substring(0, pm.start(1));
			String after = expression.substring(pm.end(1));
			
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
			/* Characters required before mult/div, so operation is group 1. */
			String operation = md.group(1);
			String before = expression.substring(0, md.start(1));
			String after = expression.substring(md.end(1));
			
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