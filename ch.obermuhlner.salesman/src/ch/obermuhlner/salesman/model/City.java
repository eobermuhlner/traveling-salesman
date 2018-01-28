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
	
	/**
	 * Constructs a {@link City}.
	 * 
	 * @param name
	 * @param x
	 * @param y
	 */
	public City(String name, double x, double y) {
		this.name = name;
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString() {
		return name;
	}
}
