package ch.obermuhlner.salesman.distance;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import ch.obermuhlner.salesman.distance.DistanceCalculator;
import ch.obermuhlner.salesman.model.City;

public class DistanceCalculatorTest {

	private static final double epsilon = 0.00001;

	private final DistanceCalculator constantDistanceCalculator = new DistanceCalculator() {
		@Override
		public double distance(City city1, City city2) {
			return 1;
		}
	};
	
	@Test
	public void test_distance_constant() {
		assertEquals(1.0, constantDistanceCalculator.distance(null, null), epsilon);
	}
	
	@Test
	public void test_distance_empty_list() {
		assertEquals(0.0, constantDistanceCalculator.distance(Arrays.asList()), epsilon);
	}
	
	@Test
	public void test_distance_list_one_city() {
		City city = new City("A", 0, 0);
		assertEquals(0.0, constantDistanceCalculator.distance(Arrays.asList(city)), epsilon);
	}
	
	@Test
	public void test_distance_list_two_cities() {
		City city = new City("A", 0, 0);
		assertEquals(2.0, constantDistanceCalculator.distance(Arrays.asList(city, city)), epsilon);
	}
	
	@Test
	public void test_distance_list_three_cities() {
		City city = new City("A", 0, 0);
		assertEquals(3.0, constantDistanceCalculator.distance(Arrays.asList(city, city, city)), epsilon);
	}
}
