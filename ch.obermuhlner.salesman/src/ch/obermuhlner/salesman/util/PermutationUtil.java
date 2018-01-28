package ch.obermuhlner.salesman.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PermutationUtil {

	/**
	 * Creates all permutations of the specified list and calls the {@link Consumer} with each of them.
	 * 
	 * @param elements the elements to be permuted
	 * @param consumer the {@link Consumer} to accept all permutations
	 */
	public static <T> void permutations(List<T> elements, Consumer<List<T>> consumer) {
		RecursivePermutation<T> recursivePermutation = new RecursivePermutation<>(elements, consumer);
		recursivePermutation.searchAllPermutations();
	}

	/**
	 * Returns a list containing all permutations of the specified list.
	 * 
	 * Because the resulting list may become very large and exhaust available memory it is generally recommended
	 * to call {@link #permutations(List, Consumer)} instead and process the permutations one by one.
	 * 
	 * @param elements the elements to be permuted
	 * @return the created {@link List} containing all permutations of the input list 
	 * @see #permutations(List, Consumer)
	 */
	public static <T> List<List<T>> permutations(List<T> elements) {
		List<List<T>> results = new ArrayList<>();

		permutations(elements, (permutation) -> results.add(permutation));
		
		return results;
	}

	private static class RecursivePermutation<T> {
		
		private final Consumer<List<T>> consumer;

		private final List<T> remainingElements;

		private final List<T> currentPermutation = new ArrayList<>();

		public RecursivePermutation(List<T> elements, Consumer<List<T>> consumer) {
			this.remainingElements = new ArrayList<T>(elements);
			this.consumer = consumer;
		}
		
		private void searchAllPermutations() {
			if (currentPermutation.size() == remainingElements.size()) {
				consumer.accept(new ArrayList<>(currentPermutation));
				return;
			}

			for (int i = 0; i < remainingElements.size(); i++) {
				T currentElement = remainingElements.get(i);
				if (currentElement != null) {
					currentPermutation.add(currentElement);
					remainingElements.set(i, null);

					searchAllPermutations();

					remainingElements.set(i, currentElement);
					currentPermutation.remove(currentPermutation.size() - 1);
				}

				ThreadUtil.checkThreadInterrupted();
			}
		}		
	}
}
