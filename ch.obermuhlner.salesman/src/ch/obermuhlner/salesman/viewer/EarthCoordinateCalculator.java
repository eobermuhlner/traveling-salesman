package ch.obermuhlner.salesman.viewer;

import ch.obermuhlner.salesman.model.City;

public class EarthCoordinateCalculator implements ScreenCoordinateCalculator {

	private int width;
	private int height;

	public EarthCoordinateCalculator(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	@Override
	public double toScreenX(City city) {
		return (city.x + 180) / 360 * width;
	}
	
	@Override
	public double toScreenY(City city) {
		return (180 - (city.y + 90)) / 180 * height;
	}
}
