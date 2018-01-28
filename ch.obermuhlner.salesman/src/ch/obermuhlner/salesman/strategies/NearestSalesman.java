package ch.obermuhlner.salesman.strategies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ch.obermuhlner.salesman.distance.DistanceCalculator;
import ch.obermuhlner.salesman.model.City;
import ch.obermuhlner.salesman.model.Salesman;

public class NearestSalesman implements Salesman {

	private int startIndex;

	public NearestSalesman() {
		this(0);
	}
	
	public NearestSalesman(int startIndex) {
		this.startIndex = startIndex;
	}
	
	@Override
	public List<City> bestPath(List<City> cities, DistanceCalculator distanceCalculator) {
		Set<City> remainingCities = new LinkedHashSet<>(cities);
		List<City> path = new ArrayList<>();
		
		if (!remainingCities.isEmpty()) {
			City current = cities.get(startIndex % cities.size());
			remainingCities.remove(current);
			path.add(current);
			
			while (!remainingCities.isEmpty()) {
				City closestCity = findClosestCity(current, remainingCities, distanceCalculator);
				remainingCities.remove(closestCity);
				path.add(closestCity);
				
				current = closestCity;
			}
		}
		
		return path;
	}
	
	private City findClosestCity(City start, Collection<City> candidates, DistanceCalculator distanceCalulator) {
		City closestCity = null;
		double closestDistance = Double.MAX_VALUE;
		
		for (City candidate : candidates) {
			double distance = distanceCalulator.distance(start, candidate);
			if (distance < closestDistance) {
				closestCity = candidate;
				closestDistance = distance;
			}
		}
		
		return closestCity;
	}

}
