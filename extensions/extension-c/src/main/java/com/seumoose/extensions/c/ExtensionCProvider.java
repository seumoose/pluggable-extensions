package com.seumoose.extensions.c;

import com.seumoose.core.interfaces.IPlugin;
import com.seumoose.core.interfaces.IPluginProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtensionCProvider implements IPluginProvider<ExtensionCConfiguration> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionCProvider.class);

	@Override
	public String getFamily() {
		return ModuleConstants.FAMILY_NAME;
	}

	@Override
	public Class<ExtensionCConfiguration> configurationType() {
		return ExtensionCConfiguration.class;
	}

	@Override
	public IPlugin create(ExtensionCConfiguration configuration) {
		LOGGER.info("{} initialised successfully", getFamily());
		return new ExtensionC(configuration);
	}
}
