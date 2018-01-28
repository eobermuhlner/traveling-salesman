package ch.obermuhlner.salesman.distance;

import java.util.List;

import ch.obermuhlner.salesman.model.City;

/**
 * Calculates the distance between cities.
 */
public interface DistanceCalculator {

	/**
	 * Calculates the distance between two cities.
	 * 
	 * @param city1 the first city
	 * @param city2 the second city
	 * @return the distance between the two cities
	 */
	double distance(City city1, City city2);
	
	/**
	 * Calculates the total distance between a sequence of cities, including the distance back to the starting city.
	 * 
	 * @param cities the list of cities to visit in a cycle
	 * @return the total distance
	 */
	default double distance(List<City> cities) {
		if (cities.size() <= 1) {
			return 0;
		}
		
		double total = 0;
		
		City currentCity = cities.get(cities.size() - 1);
		for (City nextCity : cities) {
			double dist = distance(currentCity, nextCity);
			total += dist;
			
			currentCity = nextCity;
		}
		
		return total;
	}
}
