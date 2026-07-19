package com.seumoose.core.stubs.unconfigured;

import com.seumoose.core.spi.AbstractConsumerExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StubUnconfiguredExtension extends AbstractConsumerExtension<String> {
	private static final Logger LOGGER = LoggerFactory.getLogger(StubUnconfiguredExtension.class);

	private final StubUnconfiguredConfiguration configuration;

	public StubUnconfiguredExtension(StubUnconfiguredConfiguration configuration) {
		super(String.class);
		this.configuration = configuration;
	}

	@Override
	protected void process(String input) {
		LOGGER.info("Stub consumer logging: {}{}", configuration.getPrefix(), input);
	}
}
