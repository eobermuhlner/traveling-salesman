package ch.obermuhlner.salesman.distance;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import ch.obermuhlner.salesman.distance.CartesianDistanceCalculator;
import ch.obermuhlner.salesman.model.City;

public class CartesianDistanceCalculatorTest {

	private static final double epsilon = 0.00001;

	@Test
	public void test_distance_same_city() {
		CartesianDistanceCalculator calculator = new CartesianDistanceCalculator();
		
		City cityA = new City("A", 1, 2);
		assertEquals(0.0, calculator.distance(cityA, cityA), epsilon);
	}

	@Test
	public void test_distance_0() {
		CartesianDistanceCalculator calculator = new CartesianDistanceCalculator();
		
		City cityA = new City("A", 1, 2);
		City cityB = new City("B", 1, 2);
		assertEquals(0.0, calculator.distance(cityA, cityB), epsilon);
	}

	@Test
	public void test_distance_sqrt2() {
		CartesianDistanceCalculator calculator = new CartesianDistanceCalculator();
		
		City cityA = new City("A", 0, 0);
		City cityB = new City("B", 1, 1);
		assertEquals(Math.sqrt(2), calculator.distance(cityA, cityB), epsilon);
	}

	@Test
	public void test_distance_straight_1() {
		CartesianDistanceCalculator calculator = new CartesianDistanceCalculator();
		
		City cityA = new City("A", 0, 0);
		City cityB = new City("B", 0, 1);
		assertEquals(1.0, calculator.distance(cityA, cityB), epsilon);
	}

	@Test
	public void test_distance_straight_minus_1() {
		CartesianDistanceCalculator calculator = new CartesianDistanceCalculator();
		
		City cityA = new City("A", 0, 0);
		City cityB = new City("B", 0, -1);
		assertEquals(1.0, calculator.distance(cityA, cityB), epsilon);
	}

	@Test
	public void test_distance_list_three_cities() {
		CartesianDistanceCalculator calculator = new CartesianDistanceCalculator();
		
		City cityA = new City("A", 0, 0);
		City cityB = new City("B", 0, 1);
		City cityC = new City("C", 0, 5);
		assertEquals(1.0 + 4.0 + 5.0, calculator.distance(Arrays.asList(cityA, cityB, cityC)), epsilon);
	}


}
