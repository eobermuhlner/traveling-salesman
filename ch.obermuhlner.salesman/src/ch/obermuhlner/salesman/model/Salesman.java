package ch.obermuhlner.salesman.model;

import java.util.List;

import ch.obermuhlner.salesman.distance.DistanceCalculator;

/**
 * The traveling salesman needs to visit all cities in the list and return to the starting city.
 * The goal is to find the solution with the shortest total distance.  
 */
public interface Salesman {

	/**
	 * Returns the best solution that could be found (it might not be the perfect solution).
	 * 
	 * @param cities the list of cities to visit
	 * @param distanceCalculator the {@link DistanceCalculator} used to calculate the distance between cities
	 * @return the solution with the cities in the order that has the shortest total distance this algorithm could find
	 */
	List<City> bestPath(List<City> cities, DistanceCalculator distanceCalculator);
}
