package com.seumoose.core.stubs.runnable;

import com.seumoose.core.interfaces.IRunnableExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StubRunnableExtension implements IRunnableExtension {
	private static final Logger LOGGER = LoggerFactory.getLogger(StubRunnableExtension.class);

	private final StubRunnableConfiguration configuration;

	public StubRunnableExtension(StubRunnableConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public void run() {
		LOGGER.info("Running from stub runnable implementation");
	}

	public StubRunnableConfiguration getConfiguration() {
		return configuration;
	}
}
