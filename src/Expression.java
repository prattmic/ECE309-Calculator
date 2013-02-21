import java.math.BigDecimal;
import java.util.regex.*;

/* ECE309 Calculator Project
 * Michael Pratt
 * Dennis Penn
 * David Wilson
 */

interface Operator {
	BigDecimal operate(BigDecimal operand1, BigDecimal operand2);
}

/**
 * Parses mathematical expressions.
 * Uses BigDecimal to ensure precision.
 */
public class Expression {
	private static final boolean DEBUG = false;
	
	/* Digits on precision in division. */
	private static int precision = 30;

	/* Matches any character except operators. */
	private static final String non_op = "[^\\+\\-\\*/]";
	
	/* Matches any character except operators, minus '-',
	 * which is allowed after an operation as a negative sign. */
	private static final String non_op_except_minus = "[^\\+\\*/]";
	
	/* Matches an operator.  Requires a character before which is 
	 * not an operator.  After, requires a character which is not 
	 * an operator, except '-' is allowed as a negative sign. */
	private static final Pattern plus = Pattern.compile(non_op + "+([\\+])" + non_op_except_minus + "+");
	private static final Pattern minus = Pattern.compile(non_op + "+([\\-])" + non_op_except_minus + "+");
	private static final Pattern divide = Pattern.compile(non_op + "+(/)" + non_op_except_minus + "+");
	private static final Pattern multiply = Pattern.compile(non_op + "+(\\*)" + non_op_except_minus + "+");
	
	/* Operations in reverse order of operations, so highest order operation
	 * is computed at tail of recursion.  Multiply is lower order than divide
	 * to prevent '3/5*4' from becoming '3/(5*4)', rather than '(3/5)*4'.
	 */
	private static final Pattern[] patterns = {plus, minus, multiply, divide};
	private static final Operator[] operators = {
		/* Addition */
		new Operator() {
			public BigDecimal operate(BigDecimal before, BigDecimal after) {
				return before.add(after);
			}
		},
		/* Subtraction */
		new Operator() {
			public BigDecimal operate(BigDecimal before, BigDecimal after) {
				return before.subtract(after);
			}
		},
		/* Multiplication */
		new Operator() {
			public BigDecimal operate(BigDecimal before, BigDecimal after) {
				return before.multiply(after);
			}
		},
		/* Division */
		new Operator() {
			public BigDecimal operate(BigDecimal before, BigDecimal after) {
				return before.divide(after, precision, BigDecimal.ROUND_HALF_UP).stripTrailingZeros();
			}
		},
	};
	
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
	
	/**
	 * Recursively simplifies expression to a single value.
	 * Checks for order of operations in reverse order, thus
	 * ensuring that the highest order of operations are performed
	 * at the tail.
	 * @param expression Expression to simplify
	 * @return Simplification
	 */
	public static BigDecimal simplify(String expression) {
		if (DEBUG) {
			System.err.println("Expression: " + expression);
		}
		
		try {	// If expression is a valid number, simply return it.
			return new BigDecimal(expression);
		} catch (NumberFormatException e) {}
		
		/* Perform simplification in reverse order of operations.
		 * As this function recurses down, the highest order of operations
		 * function will be performed first. Recursion will be called
		 * in handle_operator. */
		Matcher matcher;
		for (int i = 0; i < operators.length; i++) {
			matcher = patterns[i].matcher(expression);
			if (matcher.find()) {
				return handle_operator(expression, matcher, operators[i]);
			}
		}
		
		/* Only reached on invalid input */
		throw new NumberFormatException("No operation found in " + expression);
	}

	/**
	 * Once a match has been found, handles simplification of operator.
	 * Calls simplify on each side of operator to turn them into a single
	 * number that can be operated on.
	 * @param expression Expression being matched
	 * @param matcher Matcher object, after .find() successful
	 * @param operator Operator interface, implementing actual operator work.
	 * @return Result of operator
	 */
	private static BigDecimal handle_operator(String expression, Matcher matcher,
			Operator operator) {
		/* Characters required before operator, so operation is group 1. */
		String operation = matcher.group(1);
		String before = expression.substring(0, matcher.start(1));
		String after = expression.substring(matcher.end(1));
		
		if (DEBUG) {
			System.err.printf("Found operation: '%s' '%s' '%s'\n", before, operation, after);
		}
		
		BigDecimal simple_before = simplify(before);
		BigDecimal simple_after = simplify(after);
		
		if (DEBUG) {
			System.err.printf("Simplified operation: '%s' '%s' '%s'\n", simple_before, operation, simple_after);
		}
		
		return operator.operate(simple_before, simple_after);
	}
}