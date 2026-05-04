package com.seumoose.extensions.c;

import com.seumoose.core.interfaces.ISupplierPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtensionC implements ISupplierPlugin<String> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionC.class);

	private final ExtensionCConfiguration configuration;

	protected ExtensionC(ExtensionCConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public String get() {
		String message = configuration.getGreeting() + ", " + configuration.getTarget() + "!";
		LOGGER.info("ExtensionC supplying message: {}", message);
		return message;
	}
}
