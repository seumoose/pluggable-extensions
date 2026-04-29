package com.seumoose.extensions;

import com.seumoose.core.interfaces.IPlugin;
import com.seumoose.core.interfaces.IPluginProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtensionBProvider implements IPluginProvider<ExtensionBConfiguration> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionBProvider.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFamily() {
		return ModuleConstants.FAMILY_NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<ExtensionBConfiguration> configurationType() {
		return ExtensionBConfiguration.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IPlugin create(ExtensionBConfiguration configuration) {
		LOGGER.info("{} initialised successfully", getFamily());

		return new ExtensionB(configuration);
	}
}
