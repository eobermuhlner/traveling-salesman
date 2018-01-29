package ch.obermuhlner.salesman.viewer.coordinate;

public class StandardSphereCoordinateCalculator implements ScreenCoordinateCalculator {

	protected int width;
	protected int height;

	public StandardSphereCoordinateCalculator(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	@Override
	public double toScreenX(double x) {
		double xx = x + 180;
		if (xx > 360) {
			xx -= 360;
		}
		return xx / 360 * width;
	}
	
	@Override
	public double toScreenY(double y) {
		return (180 - (y + 90)) / 180 * height;
	}

	@Override
	public double getStartX() {
		return 0;
	}

	@Override
	public double getEndX() {
		return 360;
	}

	@Override
	public double getStartY() {
		return -90;
	}

	@Override
	public double getEndY() {
		return 90;
	}
}
