package com.seumoose.extensions.c;

import com.seumoose.core.spi.AbstractSupplierPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtensionC extends AbstractSupplierPlugin<String> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionC.class);

	private final ExtensionCConfiguration configuration;

	protected ExtensionC(ExtensionCConfiguration configuration) {
		super(String.class);
		this.configuration = configuration;
	}

	@Override
	public String process() {
		String message = configuration.getGreeting() + ", " + configuration.getTarget() + "!";
		LOGGER.info("ExtensionC supplying message: {}", message);
		return message;
	}
}
