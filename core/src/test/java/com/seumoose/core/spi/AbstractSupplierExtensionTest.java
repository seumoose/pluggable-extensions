package com.seumoose.core.spi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AbstractSupplierExtensionTest {

	private static class StringSupplier extends AbstractSupplierExtension<String> {
		private final String value;

		protected StringSupplier(String value) {
			super(String.class);
			this.value = value;
		}

		@Override
		protected String process() {
			return value;
		}
	}

	@Test
	public void get_withValidReturnValue_returnsProcessResult() {
		StringSupplier supplier = new StringSupplier("Lorem ipsum dolor sit amet");

		assertEquals("Lorem ipsum dolor sit amet", supplier.get());
	}

	@Test
	public void get_whenProcessReturnsNull_returnsNull() {
		StringSupplier supplier = new StringSupplier(null);

		assertNull(supplier.get());
	}
}
