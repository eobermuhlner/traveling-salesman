package ch.obermuhlner.salesman.distance;

import ch.obermuhlner.salesman.model.City;

/**
 * Calculates distances between cities on a cartesian plane. 
 */
public class CartesianDistanceCalculator implements DistanceCalculator {

	@Override
	public double distance(City city1, City city2) {
		double deltaX = city1.x - city2.x;
		double deltaY = city1.y - city2.y;
		
		return Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
	}

}
