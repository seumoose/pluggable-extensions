package com.seumoose.extensions.a;

import com.seumoose.core.interfaces.IExtension;
import com.seumoose.core.interfaces.IExtensionProvider;

public class ExtensionAProvider implements IExtensionProvider<ExtensionAConfiguration> {
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
