package ch.obermuhlner.salesman.util;

public class MathUtil {
	
	public static long factorial(int value) {
		long result = 1;
		
		for (int i = 2; i <= value; i++) {
			result *= i;
		}
		
		return result;
	}
	
	public static double factorialAsDouble(int value) {
		double result = 1;
		
		for (int i = 2; i <= value; i++) {
			result *= i;
		}
		
		return result;
	}

}
