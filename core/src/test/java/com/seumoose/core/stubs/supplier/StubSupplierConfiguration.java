package com.seumoose.core.stubs.supplier;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.seumoose.core.interfaces.IExtensionConfiguration;

public class StubSupplierConfiguration implements IExtensionConfiguration {
	private final String message;

	private static final String FAMILY = "StubSupplier";

	@JsonCreator
	public StubSupplierConfiguration(@JsonProperty("message") String message) {
		this.message = message;
	}

	@Override
	public String getFamily() {
		return FAMILY;
	}

	public String getMessage() {
		return message;
	}
}
