package com.seumoose.core.stubs.external;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.seumoose.core.interfaces.IExtensionConfiguration;

public class StubExternalConfiguration implements IExtensionConfiguration {
	private final String prefix;

	private static final String FAMILY = "StubExternal";

	@JsonCreator
	public StubExternalConfiguration(@JsonProperty("prefix") String prefix) {
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
