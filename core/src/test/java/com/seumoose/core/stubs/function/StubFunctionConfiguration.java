package com.seumoose.core.stubs.function;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.seumoose.core.interfaces.IExtensionConfiguration;

public class StubFunctionConfiguration implements IExtensionConfiguration {
	private final String suffix;

	private static final String FAMILY = "StubFunction";

	@JsonCreator
	public StubFunctionConfiguration(@JsonProperty("suffix") String suffix) {
		this.suffix = suffix;
	}

	@Override
	public String getFamily() {
		return FAMILY;
	}

	public String getSuffix() {
		return suffix;
	}
}
