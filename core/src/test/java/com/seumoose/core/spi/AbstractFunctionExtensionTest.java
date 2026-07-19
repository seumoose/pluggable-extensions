package com.seumoose.core.spi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AbstractFunctionExtensionTest {

	private static class StringCounterFunction extends
			AbstractFunctionExtension<String, Integer> {
		protected StringCounterFunction() {
			super(String.class, Integer.class);
		}

		@Override
		protected Integer process(String input) {
			return input.length();
		}
	}

	@Test
	public void apply_withValidInput_returnsProcessedResult() {
		StringCounterFunction function = new StringCounterFunction();

		Integer result = function.apply("Lorem ipsum dolor sit amet");

		assertEquals(Integer.valueOf(26), result);
	}

	@Test
	public void apply_withInvalidInputType_returnsNull() {
		StringCounterFunction function = new StringCounterFunction();

		Integer result = function.apply(123);

		assertNull(result);
	}

	@Test
	public void apply_withNullInput_returnsNull() {
		StringCounterFunction function = new StringCounterFunction();

		Integer result = function.apply(null);

		assertNull(result);
	}
}
