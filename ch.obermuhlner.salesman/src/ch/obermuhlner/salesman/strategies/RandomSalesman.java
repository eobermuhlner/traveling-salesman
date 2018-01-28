package ch.obermuhlner.salesman.strategies;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ch.obermuhlner.salesman.distance.DistanceCalculator;
import ch.obermuhlner.salesman.model.City;
import ch.obermuhlner.salesman.model.Salesman;

/**
 * Implementation of {@link Salesman} that returns the input in random order.
 * 
 * Since the result will be different with every call, this is the ideal implementation to be used in the {@link RepeatSalesman}.
 */
public class RandomSalesman implements Salesman {

	private Random random;

	public RandomSalesman() {
		this(new Random());
	}
	
	public RandomSalesman(Random random) {
		this.random = random;
	}
	
	@Override
	public List<City> bestPath(List<City> cities, DistanceCalculator distanceCalulator) {
		List<City> remainingCities = new ArrayList<>(cities);
		List<City> path = new ArrayList<>();

		while (!remainingCities.isEmpty()) {
			int index = random.nextInt(remainingCities.size());
			path.add(remainingCities.remove(index));
		}
		
		return path;
	}
}
