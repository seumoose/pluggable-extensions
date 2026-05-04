package com.seumoose.extensions.c;

import com.seumoose.core.interfaces.IExtension;
import com.seumoose.core.interfaces.IExtensionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtensionCProvider implements IExtensionProvider<ExtensionCConfiguration> {
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
	public IExtension create(ExtensionCConfiguration configuration) {
		return new ExtensionC(configuration);
	}
}
