package com.seumoose.core.stubs.consumer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.seumoose.core.interfaces.IExtensionConfiguration;

public class StubConsumerConfiguration implements IExtensionConfiguration {
	private final String prefix;

	private static final String FAMILY = "StubConsumer";

	@JsonCreator
	public StubConsumerConfiguration(@JsonProperty("prefix") String prefix) {
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
