package com.seumoose.core.spi;

import com.seumoose.core.interfaces.IFunctionExtension;
import com.seumoose.core.utility.TypeGuard;

/**
 * A extension that accepts an input and produces an output.
 *
 * @param <T> the type of input accepted by the extension.
 * @param <R> the type of result produced by the extension.
 */
public abstract class AbstractFunctionExtension<T, R> implements IFunctionExtension<T, R> {
	private final Class<T> inputType;
	private final Class<R> responseType;

	protected AbstractFunctionExtension(Class<T> inputType, Class<R> returnType) {
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
