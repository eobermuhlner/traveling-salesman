package ch.obermuhlner.salesman.model;

public enum Cities {

	BuenosAires("Buenos Aires", lat(-36, 30), lon(60, 0)),
	Vienna("Vienna", lat(48, 12), lon(-16, 22)),
	Brasilia("Brasilia", lat(-15, 47), lon(47, 55)),
	Paris("Paris", lat(48, 50), lon(-2, 20)),
	Berlin("Berlin", lat(52, 30), lon(-13, 25)),
	Washington("Washington", lat(39, 91), lon(77, 2)),
	London("London", lat(51, 36), lon(0, 5));
	
	public final City city;

	private Cities(String name, double latitude, double longitude) {
		this.city = new City(name, latitude, longitude);
	}
	
	private static double lat(double degrees, double minutes) {
		return degrees + minutes / 60.0;
	}
	
	private static double lon(double degrees, double minutes) {
		return degrees + minutes / 60.0;
	}
}
