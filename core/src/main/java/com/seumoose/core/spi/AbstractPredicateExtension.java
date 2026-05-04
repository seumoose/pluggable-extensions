package com.seumoose.core.spi;

import com.seumoose.core.interfaces.IPredicateExtension;
import com.seumoose.core.utility.TypeGuard;

/**
 * A extension that accepts an input and produces a boolean result.
 *
 * @param <T> the type of input accepted by the extension.
 */
public abstract class AbstractPredicateExtension<T> implements IPredicateExtension<T> {
	private final Class<T> inputType;

	protected AbstractPredicateExtension(Class<T> inputType) {
		this.inputType = inputType;
	}

	@Override
	public final boolean test(Object input) {
		return TypeGuard.validate(input, inputType)
				.map(this::process)
				.orElse(false);
	}

	protected abstract boolean process(T input);
}
