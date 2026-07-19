package com.seumoose.core.stubs.consumer;

import com.seumoose.core.interfaces.IExtension;
import com.seumoose.core.interfaces.IExtensionProvider;

public class StubConsumerProvider implements IExtensionProvider<StubConsumerConfiguration> {
	private static final String FAMILY = "StubConsumer";

	@Override
	public String getFamily() {
		return FAMILY;
	}

	@Override
	public Class<StubConsumerConfiguration> configurationType() {
		return StubConsumerConfiguration.class;
	}

	@Override
	public IExtension create(StubConsumerConfiguration configuration) {
		return new StubConsumerExtension(configuration);
	}
}
