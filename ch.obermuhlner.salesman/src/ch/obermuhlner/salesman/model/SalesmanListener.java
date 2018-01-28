package ch.obermuhlner.salesman.model;

import java.util.Arrays;
import java.util.List;

public interface SalesmanListener {

	default void improvedSolution(List<City> improved) {
		improvedSolutions(Arrays.asList(improved));
	}

	void improvedSolutions(List<List<City>> improved);
	

	default void discardedSolution(List<City> discarded) {
		discardedSolutions(Arrays.asList(discarded));
	}

	void discardedSolutions(List<List<City>> discarded);

}
