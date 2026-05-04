package com.seumoose.core.spi;

import com.seumoose.core.interfaces.IFunctionPlugin;
import com.seumoose.core.utility.TypeGuard;

/**
 * A plugin that accepts an input and produces an output.
 *
 * @param <T> the type of input accepted by the plugin.
 * @param <R> the type of result produced by the plugin.
 */
public abstract class AbstractFunctionPlugin<T, R> implements IFunctionPlugin<T, R> {
	private final Class<T> inputType;
	private final Class<R> responseType;

	protected AbstractFunctionPlugin(Class<T> inputType, Class<R> returnType) {
		this.inputType = inputType;
		this.responseType = returnType;
	}

	@Override
	public final R apply(Object input) {
		return TypeGuard.validate(input, inputType)
				.map(this::process)
				.flatMap((R response) -> TypeGuard.validate(response, responseType))
				.orElse(null);
	}

	protected abstract R process(T input);
}
