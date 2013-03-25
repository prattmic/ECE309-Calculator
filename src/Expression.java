import java.math.BigDecimal;
import java.util.regex.*;

/* ECE309 Calculator Project
 * Michael Pratt
 * Dennis Penn
 * David Wilson
 */

interface Operator {
	BigDecimal operate(BigDecimal operand1, BigDecimal operand2, char operator);
}

/**
 * Parses mathematicaPart2_Calculator.html expressions.
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
	 * an operator, except '-' is allowed as a negative sign.
	 * Use reluctant +? matching on non_op to match first
	 * instance of equal operators, which will result in
	 * right-to-left evaluation. */
	private static final Pattern addition = Pattern.compile(non_op + "+?([\\+])" + non_op_except_minus + "+");
	private static final Pattern subtraction = Pattern.compile(non_op + "+?([\\-])" + non_op_except_minus + "+");
	private static final Pattern multiply = Pattern.compile(non_op + "+?(\\*)" + non_op_except_minus + "+");
	private static final Pattern divide = Pattern.compile(non_op + "+?(/)" + non_op_except_minus + "+");
	private static final Pattern power = Pattern.compile(non_op + "+?([\\^rR])" + non_op_except_minus + "+");
	
	/* Operations in reverse order of operations, so highest order operation
	 * is computed at tail of recursion.  Multiply is lower order than divide
	 * to prevent '3/5*4' from becoming '3/(5*4)', rather than '(3/5)*4'.
	 */
	private static final Pattern[] patterns = {addition, subtraction, multiply, divide, power};
	private static final Operator[] operators = {
		/* Addition */
		new Operator() {
			public BigDecimal operate(BigDecimal before, BigDecimal after, char operator) {
				return before.add(after);
			}
		},
		/* Subtraction */
		new Operator() {
			public BigDecimal operate(BigDecimal before, BigDecimal after, char operator) {
				return before.subtract(after);
			}
		},
		/* Multiplication */
		new Operator() {
			public BigDecimal operate(BigDecimal before, BigDecimal after, char operator) {
				return before.multiply(after);
			}
		},
		/* Division */
		new Operator() {
			public BigDecimal operate(BigDecimal before, BigDecimal after, char operator) {
				return before.divide(after, precision, BigDecimal.ROUND_HALF_UP).stripTrailingZeros();
			}
		},
		/* Power */
		new Operator() {
			public BigDecimal operate(BigDecimal before, BigDecimal after, char operator) {
				switch (operator) {
				case '^':
					return power(before, after);
				case 'r':
				case 'R':
					return power(before, BigDecimal.ONE.divide(after, precision, BigDecimal.ROUND_HALF_UP));
				default:
					throw new NumberFormatException("Invalid power operator " + operator);
				}
			}
		},
	};
	
	public static void main(String args[]) {
		if (args.length == 1) {
			System.out.println(simplify(args[0]));
			return;
		}
		
		test("1", new BigDecimal(1));
		test("1+1", new BigDecimal(2));
		test("1-1+1", new BigDecimal(1));
		test("(2-1)*2", new BigDecimal(2));
		test("-1*1+1", new BigDecimal(0));
		test("-1*1+1/2", new BigDecimal(-0.5));
		test("2*2+2", new BigDecimal(6));
		test("1+2-3/4+5", new BigDecimal(7.25));
		test("5/-6+3/ 5*4", new BigDecimal(5.0/-6+3.0/5*4));
		test("5/-6+4*3/5", new BigDecimal(5.0/-6+3.0/5*4));
		test("1+1+1+1+1/4", new BigDecimal(4.25));
		test("1/4+1	+1+1+1*10/5", new BigDecimal(5.25));
		test("1+----2", new BigDecimal(3));
		test("1+---2", new BigDecimal(-1));
		test("- -- -2+1", new BigDecimal(3));
		test("---2+1", new BigDecimal(-1));
		test("3+(4*2)", new BigDecimal(11));
		test("(4+(4*2))/2", new BigDecimal(6));
		test("(((4)))", new BigDecimal(4));
		test("(0)(((4)))", new BigDecimal(0));
		test("(4*2)/(5+5) + (4*(6+6))", new BigDecimal(48.8));
		test("2^4", new BigDecimal(16));
		test("1.5^2", new BigDecimal(2.25));
		test("2^1.5", new BigDecimal(Math.pow(2, 1.5)));
		test("25r2", new BigDecimal(5));
		test("2^(1+3^2)/2", new BigDecimal(Math.pow(2, 9)));
		test("2^4r3", new BigDecimal(Math.pow(2, Math.pow(4, 1/3.0))));
		test("2^2^3", new BigDecimal(256));
		test("(2^2)^3", new BigDecimal(64));
		test("256r2^3", new BigDecimal(2));
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
	 * Cleans expression of problematic cases, like whitespace and double negatives.
	 * @param expression
	 * @return
	 */
	public static String cleanup(String expression) {
		Matcher matcher;
		
		/* Strip all whitespace */
		expression = expression.replaceAll("\\s", "");
		
		/* Matches operator or beginning of line followed by double negative. 
		 * Group 2 is the actual double negatives. */
		Pattern double_neg = Pattern.compile("(^|[\\+\\-\\*/])((\\-\\-)+)");
		
		matcher = double_neg.matcher(expression);
		if (matcher.find()) {
			String before = expression.substring(0, matcher.start(2));
			String after = expression.substring(matcher.end(2));
			
			if (DEBUG) {
				System.err.println("Found double negative, removing.");
			}
			
			expression = before + after;
		}
		
		/* ')(' implies multiplication */
		expression = expression.replaceAll("\\)\\(", ")*(");
		
		return expression;
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
		
		expression = cleanup(expression);
		
		expression = handle_parens(expression);
		
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
	 * Simplifies all parentheses out of expression so that simplify() only
	 * has to deal with operators, not parentheses.  Finds first set of parentheses,
	 * simplifies that, and recursively calls handle_parens to deal with other parens
	 * in expression.
	 * 
	 * WARNING: This function has horrific runtime!  It is O(n) in the best case, and
	 * O(n^parens) in the worst case. This needs to be improved.
	 * @param expression
	 * @return expression without parentheses
	 */
	private static String handle_parens(String expression) {
		byte[] exp = expression.getBytes();
		
		int openParens = 0;
		int closeParens = 0;
		int firstParen = 0;
		
		for (int i = 0; i < expression.length(); i++) {
			if (exp[i] == '(') {
				if (openParens == 0) {
					firstParen = i;
				}
				openParens++;
			}
			else if (exp[i] == ')') {
				closeParens++;
				if ((openParens == closeParens) && firstParen != i) {
					if (DEBUG) {
						System.err.println("Found paren contents: " + expression.substring(firstParen+1, i));
					}
					BigDecimal simple = simplify(expression.substring(firstParen+1, i));
					if (DEBUG) {
						System.err.println("Simplified parens contents: " + simple);
					}
					
					/* Recurvisely handle other parens*/
					String concat = expression.substring(0, firstParen) + simple + expression.substring(i+1);
					if (DEBUG) {
						System.err.println("Concatenated string: " + concat);
					}
					return handle_parens(concat);
				}
			}
		}
		
		if (openParens != closeParens) {
			throw new NumberFormatException("Unmatched parentheses");
		}
		
		return expression;
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
		
		return operator.operate(simple_before, simple_after, operation.charAt(0));
	}
	
	/**
	 * Computes power of BigDecimals.
	 * Brought to you by http://stackoverflow.com/a/3590314
	 * @param a Base
	 * @param b Exponent
	 * @return a^b
	 */
	private static BigDecimal power(BigDecimal a, BigDecimal b) {
		BigDecimal result = null;
	    int power_sign = b.signum();
	    
        // Perform X^(A+B)=X^A*X^B (B = remainder)
        double base = a.doubleValue();
        
        if (a.compareTo(new BigDecimal(base)) != 0) {
        	// Cannot convert n1 to double
            throw new NumberFormatException("Unable to maintain precision in exponentiation");
        }
        
        b = b.multiply(new BigDecimal(power_sign)); // b is now positive
        BigDecimal power_remainder = b.remainder(BigDecimal.ONE);
        BigDecimal power_int = b.subtract(power_remainder);
        
        // Calculate big part of the power using context -
        // bigger range and performance but lower accuracy
        BigDecimal intPow = a.pow(power_int.intValueExact());
        BigDecimal doublePow = new BigDecimal(Math.pow(base, power_remainder.doubleValue()));
        result = intPow.multiply(doublePow);

	    // Fix negative power
	    if (power_sign == -1) {
	        result = BigDecimal.ONE.divide(result, precision, BigDecimal.ROUND_HALF_UP);
	    }
	    
	    return result;
	}
}