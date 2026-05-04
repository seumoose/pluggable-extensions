package com.seumoose.extensions.a;

import com.seumoose.core.interfaces.IRunnableExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtensionA implements IRunnableExtension {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionA.class);

	private final ExtensionAConfiguration configuration;

	protected ExtensionA(ExtensionAConfiguration configuration) {
		this.configuration = configuration;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		LOGGER.info("Executing inside of extension A concrete implementation with example configuration {}",
				configuration.toString());
	}
}
