package com.seumoose.core.stubs.external;

import com.seumoose.core.interfaces.IExtension;
import com.seumoose.core.interfaces.IExtensionProvider;

public class StubExternalProvider implements IExtensionProvider<StubExternalConfiguration> {
	private static final String FAMILY = "StubExternal";

	@Override
	public String getFamily() {
		return FAMILY;
	}

	@Override
	public Class<StubExternalConfiguration> configurationType() {
		return StubExternalConfiguration.class;
	}

	@Override
	public IExtension create(StubExternalConfiguration configuration) {
		return new StubExternalExtension(configuration);
	}
}
