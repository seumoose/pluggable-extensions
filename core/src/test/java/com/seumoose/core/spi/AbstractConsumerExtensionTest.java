package com.seumoose.core.spi;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AbstractConsumerExtensionTest {

	private static class StringConsumer extends AbstractConsumerExtension<String> {
		private final List<String> consumed = new ArrayList<>();

		protected StringConsumer() {
			super(String.class);
		}

		@Override
		protected void process(String input) {
			consumed.add(input);
		}

		public List<String> getConsumed() {
			return consumed;
		}
	}

	@Test
	public void accept_withValidInput_callsProcess() {
		StringConsumer consumer = new StringConsumer();

		consumer.accept("Lorem ipsum dolor sit amet");

		assertEquals(1, consumer.getConsumed().size());
		assertEquals("Lorem ipsum dolor sit amet", consumer.getConsumed().get(0));
	}

	@Test
	public void accept_withInvalidInputType_doesNotCallProcess() {
		StringConsumer consumer = new StringConsumer();

		consumer.accept(123);

		assertTrue(consumer.getConsumed().isEmpty());
	}

	@Test
	public void accept_withNullInput_doesNotCallProcess() {
		StringConsumer consumer = new StringConsumer();

		consumer.accept(null);

		assertTrue(consumer.getConsumed().isEmpty());
	}
}
