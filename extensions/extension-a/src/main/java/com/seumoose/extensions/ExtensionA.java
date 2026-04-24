package com.seumoose.extensions;

import com.seumoose.core.interfaces.IPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtensionA implements IPlugin {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionA.class);

	private final ExtensionAConfiguration configuration;

	protected ExtensionA(ExtensionAConfiguration configuration) {
		this.configuration = configuration;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute() {
		LOGGER.info("Executing inside of extension A concrete implementation with example configuration {}",
				configuration.toString());
	}
}
