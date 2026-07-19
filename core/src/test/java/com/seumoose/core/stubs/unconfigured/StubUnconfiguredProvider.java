package com.seumoose.core.stubs.unconfigured;

import com.seumoose.core.interfaces.IExtension;
import com.seumoose.core.interfaces.IExtensionProvider;

public class StubUnconfiguredProvider implements IExtensionProvider<StubUnconfiguredConfiguration> {
	private static final String FAMILY = "StubUnconfigured";

	@Override
	public String getFamily() {
		return FAMILY;
	}

	@Override
	public Class<StubUnconfiguredConfiguration> configurationType() {
		return StubUnconfiguredConfiguration.class;
	}

	@Override
	public IExtension create(StubUnconfiguredConfiguration configuration) {
		return new StubUnconfiguredExtension(configuration);
	}
}
