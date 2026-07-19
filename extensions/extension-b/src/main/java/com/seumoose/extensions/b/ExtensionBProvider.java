package com.seumoose.extensions.b;

import com.seumoose.core.interfaces.IExtension;
import com.seumoose.core.interfaces.IExtensionProvider;

public class ExtensionBProvider implements IExtensionProvider<ExtensionBConfiguration> {
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
