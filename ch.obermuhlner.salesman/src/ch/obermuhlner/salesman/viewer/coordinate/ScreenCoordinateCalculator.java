package ch.obermuhlner.salesman.viewer.coordinate;

public interface ScreenCoordinateCalculator {

	double getStartX();
	double getEndX();
	
	double getStartY();
	double getEndY();
	
	double toScreenX(double x);
	double toScreenY(double y);
}
