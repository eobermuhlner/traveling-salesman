package ch.obermuhlner.salesman.strategies;

import java.util.ArrayList;
import java.util.List;

import ch.obermuhlner.salesman.distance.DistanceCalculator;
import ch.obermuhlner.salesman.model.City;
import ch.obermuhlner.salesman.model.Salesman;

/**
 * Stupid implementation of {@link Salesman} that simply returns the input in the original order. 
 */
public class StupidSalesman implements Salesman {

	@Override
	public List<City> bestPath(List<City> cities, DistanceCalculator distanceCalulator) {
		return new ArrayList<>(cities);
	}

}
