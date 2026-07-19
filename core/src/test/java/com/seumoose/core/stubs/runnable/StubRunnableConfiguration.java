package com.seumoose.core.stubs.runnable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.seumoose.core.interfaces.IExtensionConfiguration;

public class StubRunnableConfiguration implements IExtensionConfiguration {
	private final String value;

	private static final String FAMILY = "StubRunnable";

	@JsonCreator
	public StubRunnableConfiguration(@JsonProperty("value") String value) {
		this.value = value;
	}

	@Override
	public String getFamily() {
		return FAMILY;
	}

	public String getValue() {
		return value;
	}
}
