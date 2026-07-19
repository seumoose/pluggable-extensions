package com.seumoose.core.stubs.function;

import com.seumoose.core.spi.AbstractFunctionExtension;

public class StubFunctionExtension extends AbstractFunctionExtension<String, String> {
	private final StubFunctionConfiguration configuration;

	public StubFunctionExtension(StubFunctionConfiguration configuration) {
		super(String.class, String.class);
		this.configuration = configuration;
	}

	@Override
	protected String process(String input) {
		return input + configuration.getSuffix();
	}
}
