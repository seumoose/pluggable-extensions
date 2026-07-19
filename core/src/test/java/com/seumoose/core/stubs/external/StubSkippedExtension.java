package com.seumoose.core.stubs.external;

import com.seumoose.core.spi.AbstractConsumerExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StubSkippedExtension extends AbstractConsumerExtension<String> {
	private static final Logger LOGGER = LoggerFactory.getLogger(StubSkippedExtension.class);

	private final StubSkippedConfiguration configuration;

	public StubSkippedExtension(StubSkippedConfiguration configuration) {
		super(String.class);
		this.configuration = configuration;
	}

	@Override
	protected void process(String input) {
		LOGGER.info("Stub external consumer logging: {}{}", configuration.getPrefix(), input);
	}
}
