package com.seumoose.core.stubs.predicate;

import com.seumoose.core.interfaces.IExtension;
import com.seumoose.core.interfaces.IExtensionProvider;

public class StubPredicateProvider implements IExtensionProvider<StubPredicateConfiguration> {
	private static final String FAMILY = "StubPredicate";

	@Override
	public String getFamily() {
		return FAMILY;
	}

	@Override
	public Class<StubPredicateConfiguration> configurationType() {
		return StubPredicateConfiguration.class;
	}

	@Override
	public IExtension create(StubPredicateConfiguration configuration) {
		return new StubPredicateExtension(configuration);
	}
}
