package ch.obermuhlner.salesman.strategies;

import java.util.ArrayList;
import java.util.List;

import ch.obermuhlner.salesman.distance.DistanceCalculator;
import ch.obermuhlner.salesman.model.City;
import ch.obermuhlner.salesman.model.Salesman;
import ch.obermuhlner.salesman.model.SalesmanListener;
import ch.obermuhlner.salesman.util.PermutationUtil;

/**
 * Solves the traveling salesman problem using brute force.
 * 
 * It goes through all possible solutions and compares them all.
 * 
 * This algorithm is O(n!).
 */
public class BruteForceSalesman implements Salesman {

	private SalesmanListener listener;

	public void setListener(SalesmanListener listener) {
		this.listener = listener;
	}

	@Override
	public List<City> bestPath(List<City> cities, DistanceCalculator distanceCalulator) {
		BruteForceSearch bruteForceSearch = new BruteForceSearch(cities, distanceCalulator, listener);
		return bruteForceSearch.findBestPermutation();
	}
	
	private static class BruteForceSearch {
		private List<City> cities;
		private final DistanceCalculator distanceCalulator;
		private final SalesmanListener listener;

		private double bestDistance = Double.MAX_VALUE;
		private List<City> bestPath = null;

		public BruteForceSearch(List<City> cities, DistanceCalculator distanceCalulator, SalesmanListener listener) {
			this.cities = cities;
			this.distanceCalulator = distanceCalulator;
			this.listener = listener;
		}

		public List<City> findBestPermutation() {
			PermutationUtil.permutations(cities, (permutation) -> {
				checkPath(permutation);
			});

			return bestPath;
		}
		
		private void checkPath(List<City> permutation) {
			double distance = distanceCalulator.distance(permutation);
			if (distance < bestDistance) {
				bestDistance = distance;
				bestPath = new ArrayList<>(permutation);
				
				if (listener != null) {
					listener.improvedSolution(bestPath);
				}
			} else {
				if (listener != null) {
					listener.discardedSolution(permutation);
				}				
			}
		}
	}
}
