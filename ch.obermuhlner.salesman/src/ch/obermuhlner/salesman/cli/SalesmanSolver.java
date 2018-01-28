package ch.obermuhlner.salesman.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ch.obermuhlner.salesman.distance.CartesianDistanceCalculator;
import ch.obermuhlner.salesman.distance.DistanceCalculator;
import ch.obermuhlner.salesman.distance.SphericalDistanceCalculator;
import ch.obermuhlner.salesman.model.Cities;
import ch.obermuhlner.salesman.model.City;
import ch.obermuhlner.salesman.model.Salesman;
import ch.obermuhlner.salesman.strategies.BestSalesman;
import ch.obermuhlner.salesman.strategies.BruteForceSalesman;
import ch.obermuhlner.salesman.strategies.GeneticSalesman;
import ch.obermuhlner.salesman.strategies.NearestSalesman;
import ch.obermuhlner.salesman.strategies.RandomSalesman;
import ch.obermuhlner.salesman.strategies.StupidSalesman;

/**
 * Simple command line application to solve the traveling salesman problem.
 */
public class SalesmanSolver {

	private List<City> cities = new ArrayList<>();
	
	private final DistanceCalculator distanceCalulator;

	public SalesmanSolver(DistanceCalculator distanceCalulator) {
		this.distanceCalulator = distanceCalulator;
	}
	
	public void add(City city) {
		cities.add(city);
	}
	
	public void solveProblem() {
		runSalesman("stupid", new StupidSalesman());
		System.out.println();
		
		BestSalesman bestRandomSalesman = new BestSalesman();
		for (int i = 0; i < 10; i++) {
			RandomSalesman randomSalesman = new RandomSalesman(new Random(i));
			runSalesman("random-" + i, randomSalesman);
			bestRandomSalesman.add(randomSalesman);
		}
		System.out.println();
		
		runSalesman("best-random", bestRandomSalesman);
		System.out.println();

		BestSalesman bestSimpleSalesman = new BestSalesman();
		for (int i = 0; i < cities.size(); i++) {
			NearestSalesman simpleSalesman = new NearestSalesman(i);
			bestSimpleSalesman.add(simpleSalesman);
			runSalesman("simple-" + i, simpleSalesman);
		}
		System.out.println();

		runSalesman("best-simple", bestSimpleSalesman);
		System.out.println();

		runSalesman("genetic", new GeneticSalesman());
		System.out.println();
		
		if (cities.size() <= 10) {
			runSalesman("bruteforce", new BruteForceSalesman());
			System.out.println();
		}
	}

	private void runSalesman(String name, Salesman salesman) {
		long startMillis = System.currentTimeMillis();
		List<City> solution = salesman.bestPath(cities, distanceCalulator);
		long endMillis = System.currentTimeMillis();
		long deltaMillis = endMillis - startMillis;
		
		double distance = distanceCalulator.distance(solution);
		System.out.println(String.format("%-20s %12.3f  %8d ms %s", name, distance, deltaMillis, solution.toString()));
	}

	public static void main(String[] args) {
		//solveSimpleProblem();
		solveBigProblem();
		//solveWorldProblem();
	}

	private static void solveSimpleProblem() {
		SalesmanSolver solver = new SalesmanSolver(new CartesianDistanceCalculator());
		
		solver.add(new City("a", 0, 0));
		solver.add(new City("b", 9, 3));
		solver.add(new City("c", 1, 4));
		solver.add(new City("d", 5, 5));
		solver.add(new City("e", 2, 9));
		solver.add(new City("f", 4, 0));
		solver.add(new City("g", 1, 2));
		
		solver.solveProblem();
	}

	private static void solveBigProblem() {
		SalesmanSolver solver = new SalesmanSolver(new CartesianDistanceCalculator());
	
		Random random = new Random();
		for (int i = 0; i < 20; i++) {
			solver.add(new City("City" + i, random.nextInt(1000), random.nextInt(1000)));
		}
		
		solver.solveProblem();
	}
	
	private static void solveWorldProblem() {
		SalesmanSolver solver = new SalesmanSolver(SphericalDistanceCalculator.earthKilometers());
		
		solver.add(Cities.BuenosAires.city);
		solver.add(Cities.Vienna.city);
		solver.add(Cities.Brasilia.city);
		solver.add(Cities.Paris.city);
		solver.add(Cities.Berlin.city);
		solver.add(Cities.Washington.city);
		solver.add(Cities.London.city);
		
		solver.solveProblem();
	}
}
