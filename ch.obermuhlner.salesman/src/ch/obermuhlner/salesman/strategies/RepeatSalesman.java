package ch.obermuhlner.salesman.strategies;

import java.util.List;

import ch.obermuhlner.salesman.distance.DistanceCalculator;
import ch.obermuhlner.salesman.model.City;
import ch.obermuhlner.salesman.model.Salesman;
import ch.obermuhlner.salesman.model.SalesmanListener;
import ch.obermuhlner.salesman.util.ThreadUtil;

/**
 * Decorator around another implementation of {@link Salesman} that is calls repeatedly, returning the best result.
 * 
 * To be useful the underlying {@link Salesman} should return different results with every call.
 */
public class RepeatSalesman implements Salesman {

	private final Salesman underlying;

	private SalesmanListener listener;

	private int repeatCount = 10000;

	public RepeatSalesman(Salesman underlying) {
		this.underlying = underlying;
	}
	
	public void setListener(SalesmanListener listener) {
		this.listener = listener;
	}
	
	public void setRepeatCount(int repeatCount) {
		this.repeatCount = repeatCount;
	}

	@Override
	public List<City> bestPath(List<City> cities, DistanceCalculator distanceCalulator) {
		double bestDistance = Double.MAX_VALUE;
		List<City> bestPath = null;

		for (int i = 0; i < repeatCount; i++) {
			List<City> path = underlying.bestPath(cities, distanceCalulator);
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
			} else {
				if (listener != null) {
					listener.discardedSolution(path);
				}
			}
			
			ThreadUtil.checkThreadInterrupted();
		}
		
		return bestPath;
	}

}
