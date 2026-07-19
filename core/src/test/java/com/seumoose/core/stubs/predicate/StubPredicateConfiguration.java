package com.seumoose.core.stubs.predicate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.seumoose.core.interfaces.IExtensionConfiguration;

public class StubPredicateConfiguration implements IExtensionConfiguration {
	private final String pattern;

	private static final String FAMILY = "StubPredicate";

	@JsonCreator
	public StubPredicateConfiguration(@JsonProperty("pattern") String pattern) {
		this.pattern = pattern;
	}

	@Override
	public String getFamily() {
		return FAMILY;
	}

	public String getPattern() {
		return pattern;
	}
}
