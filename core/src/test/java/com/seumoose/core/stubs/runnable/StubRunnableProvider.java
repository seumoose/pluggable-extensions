package com.seumoose.core.stubs.runnable;

import com.seumoose.core.interfaces.IExtension;
import com.seumoose.core.interfaces.IExtensionProvider;

public class StubRunnableProvider implements IExtensionProvider<StubRunnableConfiguration> {
	private static final String FAMILY = "StubRunnable";

	@Override
	public String getFamily() {
		return FAMILY;
	}

	@Override
	public Class<StubRunnableConfiguration> configurationType() {
		return StubRunnableConfiguration.class;
	}

	@Override
	public IExtension create(StubRunnableConfiguration configuration) {
		return new StubRunnableExtension(configuration);
	}
}
