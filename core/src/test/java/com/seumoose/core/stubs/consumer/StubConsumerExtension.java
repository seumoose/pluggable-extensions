package com.seumoose.core.stubs.consumer;

import com.seumoose.core.spi.AbstractConsumerExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StubConsumerExtension extends AbstractConsumerExtension<String> {
	private static final Logger LOGGER = LoggerFactory.getLogger(StubConsumerExtension.class);

	private final StubConsumerConfiguration configuration;

	public StubConsumerExtension(StubConsumerConfiguration configuration) {
		super(String.class);
		this.configuration = configuration;
	}

	@Override
	protected void process(String input) {
		LOGGER.info("Stub consumer logging: {}{}", configuration.getPrefix(), input);
	}
}
