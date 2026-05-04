package com.seumoose.extensions.b;

import com.seumoose.core.interfaces.IExtension;
import com.seumoose.core.interfaces.IExtensionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtensionBProvider implements IExtensionProvider<ExtensionBConfiguration> {
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
	public IExtension create(ExtensionBConfiguration configuration) {
		return new ExtensionB(configuration);
	}
}
