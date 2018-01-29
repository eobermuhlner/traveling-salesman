package ch.obermuhlner.salesman.viewer;

import ch.obermuhlner.salesman.model.City;

public class CartesianScreenCoordinateCalculator implements ScreenCoordinateCalculator {

	@Override
	public double toScreenX(City city) {
		return city.x;
	}
	
	@Override
	public double toScreenY(City city) {
		return city.y;
	}
}
