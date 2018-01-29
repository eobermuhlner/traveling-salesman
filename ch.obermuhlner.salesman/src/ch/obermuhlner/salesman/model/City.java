package ch.obermuhlner.salesman.model;

import ch.obermuhlner.salesman.distance.DistanceCalculator;

/**
 * City for the traveling salesman problem.
 * 
 * The coordinates may be interpreted differently (see subclasses of {@link DistanceCalculator}).
 */
public class City {
	
	public final String name;
	public final double x;
	public final double y;
	public final int population;
	
	/**
	 * Constructs a {@link City}.
	 * 
	 * @param name
	 * @param x
	 * @param y
	 * @param population
	 */
	public City(String name, double x, double y) {
		this(name, x, y, 0);
	}

	/**
	 * Constructs a {@link City}.
	 * 
	 * @param name the name of the city
	 * @param x the x coordinate of the city (or longitude in spherical coordinates)
	 * @param y the y coordinate of the city (or latitude in spherical coordinates)
	 * @param population the population of the city, or 0 if unknown
	 */
	public City(String name, double x, double y, int population) {
		this.name = name;
		this.x = x;
		this.y = y;
		this.population = population;
	}

	@Override
	public String toString() {
		return name;
	}
}
