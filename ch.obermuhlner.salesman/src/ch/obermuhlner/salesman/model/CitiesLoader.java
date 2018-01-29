package ch.obermuhlner.salesman.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * https://simplemaps.com/data/world-cities
 * https://planetarynames.wr.usgs.gov/nomenclature/AdvancedSearch
 */
public class CitiesLoader {

	public static List<City> load(String filename) {
		try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
			reader.readLine(); // ignore header
			return reader.lines()
				.map(line -> line.split(","))
				.map(fields -> {
					String name = fields[0];
					double lat = Double.parseDouble(fields[1]);
					double lon = Double.parseDouble(fields[2]);
					int population = (int) Double.parseDouble(fields[3]);
					return new City(name, lon, lat, population);
				})
				.collect(Collectors.toList());
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static List<City> load(String filename, Random random, int count) {
		List<City> cities = load(filename);
		if (cities.size() < count) {
			return cities;
		}
		
		int totalPopulation = cities.stream()
			.mapToInt(city -> city.population)
			.sum();
		
		Set<City> selectedCities = new HashSet<>();
		City randomCity = null;
		for (int i = 0; i < count; i++) {
			do {
				int r = random.nextInt(totalPopulation);
				randomCity = pickCity(cities, r);
			} while(selectedCities.contains(randomCity));
			selectedCities.add(randomCity);
		}
		
		return new ArrayList<>(selectedCities);
	}

	private static City pickCity(List<City> cities, int r) {
		int total = 0;
		for (City city : cities) {
			int nextTotal = total + city.population;
			if (r >= total && r < nextTotal) {
				return city;
			}
			total = nextTotal;
		}
		throw new IllegalArgumentException("Illegal value " + r + " when total is " + total);
	}
}
