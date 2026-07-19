package com.seumoose.core.stubs.function;

import com.seumoose.core.interfaces.IExtension;
import com.seumoose.core.interfaces.IExtensionProvider;

public class StubFunctionProvider implements IExtensionProvider<StubFunctionConfiguration> {
	private static final String FAMILY = "StubFunction";

	@Override
	public String getFamily() {
		return FAMILY;
	}

	@Override
	public Class<StubFunctionConfiguration> configurationType() {
		return StubFunctionConfiguration.class;
	}

	@Override
	public IExtension create(StubFunctionConfiguration configuration) {
		return new StubFunctionExtension(configuration);
	}
}
