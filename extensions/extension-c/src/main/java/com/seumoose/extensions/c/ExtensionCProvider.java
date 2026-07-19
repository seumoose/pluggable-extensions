package com.seumoose.extensions.c;

import com.seumoose.core.interfaces.IExtension;
import com.seumoose.core.interfaces.IExtensionProvider;

public class ExtensionCProvider implements IExtensionProvider<ExtensionCConfiguration> {
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
