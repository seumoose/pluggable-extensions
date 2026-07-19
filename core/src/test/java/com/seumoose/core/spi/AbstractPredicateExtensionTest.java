package com.seumoose.core.spi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AbstractPredicateExtensionTest {

	private static class StringLengthPredicate extends
			AbstractPredicateExtension<String> {
		protected StringLengthPredicate() {
			super(String.class);
		}

		@Override
		protected boolean process(String input) {
			return input.length() > 5;
		}
	}

	@Test
	public void test_withValidInput_returnsProcessResult_true() {
		StringLengthPredicate predicate = new StringLengthPredicate();

		assertTrue(predicate.test("Lorem ipsum dolor sit amet"));
	}

	@Test
	public void test_withValidInput_returnsProcessResult_false() {
		StringLengthPredicate predicate = new StringLengthPredicate();

		assertFalse(predicate.test("Lorem"));
	}

	@Test
	public void test_withInvalidInputType_returnsFalse() {
		StringLengthPredicate predicate = new StringLengthPredicate();

		assertFalse(predicate.test(123));
	}

	@Test
	public void test_withNullInput_returnsFalse() {
		StringLengthPredicate predicate = new StringLengthPredicate();

		assertFalse(predicate.test(null));
	}
}
