package com.seumoose.extensions.a;

import com.seumoose.core.interfaces.IPlugin;
import com.seumoose.core.interfaces.IPluginProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtensionAProvider implements IPluginProvider<ExtensionAConfiguration> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionAProvider.class);

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
	public Class<ExtensionAConfiguration> configurationType() {
		return ExtensionAConfiguration.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IPlugin create(ExtensionAConfiguration configuration) {
		LOGGER.info("{} initialised successfully", getFamily());

		return new ExtensionA(configuration);
	}
}
