package com.seumoose.core.stubs.external;

import com.seumoose.core.interfaces.IExtension;
import com.seumoose.core.interfaces.IExtensionProvider;

public class StubSkippedProvider implements IExtensionProvider<StubSkippedConfiguration> {
	private static final String FAMILY = "StubSkipped";

	@Override
	public String getFamily() {
		return FAMILY;
	}

	@Override
	public Class<StubSkippedConfiguration> configurationType() {
		return StubSkippedConfiguration.class;
	}

	@Override
	public IExtension create(StubSkippedConfiguration configuration) {
		return new StubSkippedExtension(configuration);
	}
}
