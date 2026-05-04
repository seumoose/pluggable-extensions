package com.seumoose.core.spi;

import com.seumoose.core.interfaces.IPredicatePlugin;
import com.seumoose.core.utility.TypeGuard;

/**
 * A plugin that accepts an input and produces a boolean result.
 *
 * @param <T> the type of input accepted by the plugin.
 */
public abstract class AbstractPredicatePlugin<T> implements IPredicatePlugin<T> {
	private final Class<T> inputType;

	protected AbstractPredicatePlugin(Class<T> inputType) {
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
