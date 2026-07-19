package com.seumoose.core.stubs.external;

import com.seumoose.core.spi.AbstractConsumerExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StubExternalExtension extends AbstractConsumerExtension<String> {
	private static final Logger LOGGER = LoggerFactory.getLogger(StubExternalExtension.class);

	private final StubExternalConfiguration configuration;

	public StubExternalExtension(StubExternalConfiguration configuration) {
		super(String.class);
		this.configuration = configuration;
	}

	@Override
	protected void process(String input) {
		LOGGER.info("Stub external consumer logging: {}{}", configuration.getPrefix(), input);
	}
}
