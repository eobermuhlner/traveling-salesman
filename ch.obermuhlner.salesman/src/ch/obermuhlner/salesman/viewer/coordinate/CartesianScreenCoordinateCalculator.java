package ch.obermuhlner.salesman.viewer.coordinate;

public class CartesianScreenCoordinateCalculator implements ScreenCoordinateCalculator {

	private int width;
	private int height;

	public CartesianScreenCoordinateCalculator(int width, int height) {
		this.width = width;
		this.height = height;
	}

	@Override
	public double toScreenX(double x) {
		return x;
	}
	
	@Override
	public double toScreenY(double y) {
		return y;
	}

	@Override
	public double getStartX() {
		return 0;
	}

	@Override
	public double getEndX() {
		return width;
	}

	@Override
	public double getStartY() {
		return 0;
	}

	@Override
	public double getEndY() {
		return height;
	}
}
