package ch.obermuhlner.salesman.util;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public class PermutationUtilTest {

	@Test(expected = NullPointerException.class)
	public void test_permutations_null() {
		PermutationUtil.permutations(null);
	}

	@Test
	public void test_permutations_empty() {
		assertEquals(
				Arrays.asList(
						Collections.emptyList()),
				PermutationUtil.permutations(Collections.emptyList()));
	}

	@Test
	public void test_permutations_list1() {
		assertEquals(
				Arrays.asList(
						Arrays.asList("A")),
				PermutationUtil.permutations(Arrays.asList("A")));
	}

	@Test
	public void test_permutations_list2() {
		assertEquals(
				Arrays.asList(
						Arrays.asList("A", "B"),
						Arrays.asList("B", "A")),
				PermutationUtil.permutations(Arrays.asList("A", "B")));
	}

	@Test
	public void test_permutations_list3() {
		assertEquals(
				MathUtil.factorial(3),
				PermutationUtil.permutations(Arrays.asList("A", "B", "C")).size());
		assertEquals(
				Arrays.asList(
						Arrays.asList("A", "B", "C"),
						Arrays.asList("A", "C", "B"),
						Arrays.asList("B", "A", "C"),
						Arrays.asList("B", "C", "A"),
						Arrays.asList("C", "A", "B"),
						Arrays.asList("C", "B", "A")),
				PermutationUtil.permutations(Arrays.asList("A", "B", "C")));
	}

	@Test
	public void test_permutations_list4() {
		assertEquals(
				MathUtil.factorial(4),
				PermutationUtil.permutations(Arrays.asList("A", "B", "C", "D")).size());
	}
}
