package ch.obermuhlner.salesman.strategies;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import ch.obermuhlner.salesman.distance.DistanceCalculator;
import ch.obermuhlner.salesman.model.City;
import ch.obermuhlner.salesman.model.Salesman;
import ch.obermuhlner.salesman.model.SalesmanListener;
import ch.obermuhlner.salesman.util.MathUtil;
import ch.obermuhlner.salesman.util.ThreadUtil;

public class GeneticSalesman implements Salesman {

	private SalesmanListener listener;
	
	private boolean initialSimpleSalesmanPopulation = true;
	private int initialRandomPopulationCount = 10;
	private int evolutionStepCount = 50000;
	private int evolutionGenerationCount = 20;
	private int mutationCount = 2;

	public void setInitialSimpleSalesmanPopulation(boolean initialSimpleSalesmanPopulation) {
		this.initialSimpleSalesmanPopulation = initialSimpleSalesmanPopulation;
	}
	
	public void setInitialRandomPopulationCount(int initialRandomPopulationCount) {
		this.initialRandomPopulationCount = initialRandomPopulationCount;
	}

	public void setMutationCount(int mutationCount) {
		this.mutationCount = mutationCount;
	}
	
	public void setEvolutionStepCount(int evolutionStepCount) {
		this.evolutionStepCount = evolutionStepCount;
	}
	
	public void setEvolutionGenerationCount(int evolutionGenerationCount) {
		this.evolutionGenerationCount = evolutionGenerationCount;
	}
	
	public void setListener(SalesmanListener listener) {
		this.listener = listener;
	}

	@Override
	public List<City> bestPath(List<City> cities, DistanceCalculator distanceCalulator) {
		GeneticSearch geneticSearch = new GeneticSearch(cities, distanceCalulator, new Random(), mutationCount, listener);
		
		if (initialSimpleSalesmanPopulation) {
			geneticSearch.addSimpleSalesmanPopulation(cities);
		}
		geneticSearch.addRandomPopulation(cities, initialRandomPopulationCount);
		
		for (int i = 0; i < evolutionStepCount && !geneticSearch.exhaustedAllPossibleSolutions; i++) {
			geneticSearch.runEvolution(evolutionGenerationCount);
		}
		
		return geneticSearch.getBest();
	}
	
	private static class GeneticSearch {
		private final DistanceCalculator distanceCalulator;
		private final Random random;
		
		private final int mutationCount;
		private final SalesmanListener listener;

		private final List<List<City>> population = new ArrayList<>();

		public boolean exhaustedAllPossibleSolutions = false;

		private GeneticSearch(List<City> cities, DistanceCalculator distanceCalulator, Random random, int mutationCount, SalesmanListener listener) {
			this.distanceCalulator = distanceCalulator;
			this.random = random;
			this.mutationCount = mutationCount;
			this.listener = listener;
		}
		
		public void addSimpleSalesmanPopulation(List<City> cities) {
			for (int i = 0; i < cities.size(); i++) {
				NearestSalesman simpleSalesman = new NearestSalesman(i);
				List<City> path = simpleSalesman.bestPath(cities, distanceCalulator);
				population.add(path);
			}
		}
		
		public void addRandomPopulation(List<City> cities, int count) {
			for (int i = 0; i < count; i++) {
				population.add(randomShuffle(cities, cities.size()));
			}
			
			sortPopulation();
		}

		/**
		 * Let the population grow for several generations before reducing the population to the original size.
		 * 
		 * @param generationCount the number of generations
		 */
		public void runEvolution(int generationCount) {
			int n = population.size();
			Set<List<City>> newPopulation = new HashSet<>(population); 

			int cityCount = population.get(0).size();
			long maxPossibleCombinations = cityCount > 10 ? Long.MAX_VALUE : MathUtil.factorial(cityCount);

			List<List<City>> currentGeneration = population;
			List<List<City>> nextGeneration = new ArrayList<>();
			for (int generationStep = 0; generationStep < generationCount && !exhaustedAllPossibleSolutions; generationStep++) {
				for (int i = 0; i < currentGeneration.size() && !exhaustedAllPossibleSolutions; i++) {
					List<City> parent = currentGeneration.get(i);
					if (newPopulation.size() < maxPossibleCombinations) {
						List<City> child = null;
						int attempt = 0;
						do {
							child = randomShuffle(parent, mutationCount);
							if (attempt++ > 1000) {
								child = null;
							}

							ThreadUtil.checkThreadInterrupted();
						} while (child != null && newPopulation.contains(child));
						
						if (child != null) {
							nextGeneration.add(child);
							newPopulation.add(child);
						}
					} else {
						exhaustedAllPossibleSolutions = true;
					}
				}
				
				currentGeneration = nextGeneration;
				nextGeneration = new ArrayList<>();
			}
			
			population.clear();
			population.addAll(newPopulation);
			
			sortPopulation();

			while (population.size() > n) {
				population.remove(population.size()-1);
			}
			
			if (listener != null) {
				listener.improvedSolutions(population);
			}
		}

		private List<City> randomShuffle(List<City> cities, int mutationCount) {
			List<City> shuffled = new ArrayList<>(cities);
			
			for (int i = 0; i < mutationCount; i++) {
				int index1 = random.nextInt(shuffled.size());
				int index2 = random.nextInt(shuffled.size());
				
				City tmp = shuffled.get(index1);
				shuffled.set(index1, shuffled.get(index2));
				shuffled.set(index2, tmp);
			}
			
			return shuffled;
		}

		public List<City> getBest() {
			return population.get(0);
		}

		private void sortPopulation() {
			population.sort(new Comparator<List<City>>() {
				@Override
				public int compare(List<City> o1, List<City> o2) {
					return Double.compare(distanceCalulator.distance(o1), distanceCalulator.distance(o2));
				}
			});
		}
	}
}
