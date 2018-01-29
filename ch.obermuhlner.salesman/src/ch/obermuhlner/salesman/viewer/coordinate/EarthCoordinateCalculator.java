package ch.obermuhlner.salesman.viewer.coordinate;

public class EarthCoordinateCalculator extends StandardSphereCoordinateCalculator {

	public EarthCoordinateCalculator(int width, int height) {
		super(width, height);
	}
	
	@Override
	public double toScreenX(double x) {
		return (x + 180) / 360 * width;
	}
	
	@Override
	public double getStartX() {
		return -180.0;
	}
	
	@Override
	public double getEndX() {
		return 180;
	}
}
