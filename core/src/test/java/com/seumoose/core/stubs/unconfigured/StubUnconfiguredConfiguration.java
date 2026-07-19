package com.seumoose.core.stubs.unconfigured;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.seumoose.core.interfaces.IExtensionConfiguration;

public class StubUnconfiguredConfiguration implements IExtensionConfiguration {
	private final String prefix;

	private static final String FAMILY = "StubUnconfigured";

	@JsonCreator
	public StubUnconfiguredConfiguration(@JsonProperty("prefix") String prefix) {
		this.prefix = prefix;
	}

	@Override
	public String getFamily() {
		return FAMILY;
	}

	public String getPrefix() {
		return prefix;
	}
}
