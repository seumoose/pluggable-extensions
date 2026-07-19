package com.seumoose.core.stubs.predicate;

import com.seumoose.core.spi.AbstractPredicateExtension;

public class StubPredicateExtension extends AbstractPredicateExtension<String> {
	private final StubPredicateConfiguration configuration;

	public StubPredicateExtension(StubPredicateConfiguration configuration) {
		super(String.class);
		this.configuration = configuration;
	}

	@Override
	protected boolean process(String input) {
		return input.matches(configuration.getPattern());
	}
}
