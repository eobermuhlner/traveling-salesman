package ch.obermuhlner.salesman.strategies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ch.obermuhlner.salesman.distance.DistanceCalculator;
import ch.obermuhlner.salesman.model.City;
import ch.obermuhlner.salesman.model.Salesman;
import ch.obermuhlner.salesman.model.SalesmanListener;

/**
 * Decorator around a collection of {@link Salesman} instances returning the best result.
 */
public class BestSalesman implements Salesman {

	private final Collection<Salesman> underlyings = new ArrayList<>();

	private SalesmanListener listener;

	public void setListener(SalesmanListener listener) {
		this.listener = listener;
	}

	public void add(Salesman salesman) {
		underlyings.add(salesman);
	}
	
	@Override
	public List<City> bestPath(List<City> cities, DistanceCalculator distanceCalulator) {
		double bestDistance = Double.MAX_VALUE;
		List<City> bestPath = null;
		
		for (Salesman underling : underlyings) {
			List<City> path = underling.bestPath(cities, distanceCalulator);
			double distance = distanceCalulator.distance(path);
			
			if (distance < bestDistance) {
				if (listener != null) {
					listener.discardedSolution(path);
				}

				bestDistance = distance;
				bestPath = path;
	
				if (listener != null) {
					listener.improvedSolution(path);
				}
			}
		}
		
		return bestPath;
	}

}
