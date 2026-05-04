package com.seumoose.extensions.a;

import com.seumoose.core.interfaces.IExtension;
import com.seumoose.core.interfaces.IExtensionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtensionAProvider implements IExtensionProvider<ExtensionAConfiguration> {
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
	public IExtension create(ExtensionAConfiguration configuration) {
		return new ExtensionA(configuration);
	}
}
