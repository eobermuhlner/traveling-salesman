package ch.obermuhlner.salesman.viewer;

import ch.obermuhlner.salesman.model.City;

public interface ScreenCoordinateCalculator {

	double toScreenX(City city);
	double toScreenY(City city);
}
