package ch.obermuhlner.salesman.distance;

import ch.obermuhlner.salesman.model.City;

/**
 * Calculates distances between cities on a sphere.
 * 
 * The x and z coordinates of the {@link City} are interpreted
 * as longitude (-180.0 to 180.0) and latitude (-90.0 to 90.0) respectively.
 * 
 * @see <a href="https://en.wikipedia.org/wiki/Great-circle_distance">Wikipedia: Great-circle distance</a>  
 */
public class SphericalDistanceCalculator implements DistanceCalculator {

	private static final double RADIUS_EARTH_KILOMETERS = 6371;
	private static final double RADIUS_MOON_KILOMETERS = 1737;
	private static final double RADIUS_MARS_KILOMETERS = 3390;
	
	private static final double MILES_PER_KILOMETER = 0.621371;
	
	private double radius;

	/**
	 * Constructs a spherical distance calculator using the specified radius.
	 * 
	 * @param radius the radius of the sphere
	 */
	public SphericalDistanceCalculator(double radius) {
		this.radius = radius;
	}

	@Override
	public double distance(City city1, City city2) {
		double city1Latitude = Math.toRadians(city1.y);
		double city2Latitude = Math.toRadians(city2.y);

		double city1Longitude = Math.toRadians(city1.x);
		double city2Longitude = Math.toRadians(city2.x);
		
		double deltaLongitude = city1Longitude - city2Longitude;
		double deltaLatitude = city1Latitude - city2Latitude;
		
		double a = Math.sin(deltaLatitude / 2);
		double b = Math.sin(deltaLongitude / 2);
		
		double c = a*a + Math.cos(city1Latitude) * Math.cos(city2Latitude) * b*b;
		
		double distance = 2 * Math.asin(Math.sqrt(c));
		
		return distance * radius;
	}
	
	/**
	 * Creates a spherical distance calculator on Earth in kilometers.
	 */
	public static SphericalDistanceCalculator earthKilometers() {
		return new SphericalDistanceCalculator(RADIUS_EARTH_KILOMETERS);
	}

	/**
	 * Creates a spherical distance calculator on Earth in miles.
	 */
	public static SphericalDistanceCalculator earthMiles() {
		return new SphericalDistanceCalculator(RADIUS_EARTH_KILOMETERS * MILES_PER_KILOMETER);
	}

	/**
	 * Creates a spherical distance calculator on the Moon in kilometers.
	 */
	public static SphericalDistanceCalculator moonKilometers() {
		return new SphericalDistanceCalculator(RADIUS_MOON_KILOMETERS);
	}

	/**
	 * Creates a spherical distance calculator on the Moon in miles.
	 */
	public static SphericalDistanceCalculator moonMiles() {
		return new SphericalDistanceCalculator(RADIUS_MOON_KILOMETERS * MILES_PER_KILOMETER);
	}

	/**
	 * Creates a spherical distance calculator on Mars in kilometers.
	 */
	public static SphericalDistanceCalculator marsKilometers() {
		return new SphericalDistanceCalculator(RADIUS_MARS_KILOMETERS);
	}

	/**
	 * Creates a spherical distance calculator on Mars in miles.
	 */
	public static SphericalDistanceCalculator marsMiles() {
		return new SphericalDistanceCalculator(RADIUS_MARS_KILOMETERS * MILES_PER_KILOMETER);
	}
}
