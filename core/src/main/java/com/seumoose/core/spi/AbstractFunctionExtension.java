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

	/**
	 * Constructs a new function extension bound to the given input and response
	 * types used to validate values at runtime.
	 *
	 * @param inputType    the {@link Class} of the input type {@link T}.
	 * @param responseType the {@link Class} of the response type {@link R}.
	 */
	protected AbstractFunctionExtension(Class<T> inputType, Class<R> responseType) {
		this.inputType = inputType;
		this.responseType = responseType;
	}

	// TODO: return optional?
	/**
	 * Validates that {@code input} is an instance of {@link T} before
	 * delegating to {@link #process(Object)}, then validates that the
	 * resulting response is an instance of {@link R}.
	 *
	 * If either validation fails (including when {@code input} is not an
	 * instance of {@link T}), {@code null} is returned instead of throwing.
	 *
	 * @param input the input to process, expected to be an instance of {@link T}.
	 * 
	 * @return the processed result of type {@link R}, or {@code null} if the
	 *         input or the produced result did not match the expected type.
	 */

	@Override
	public final R apply(Object input) {
		return TypeGuard.validate(input, inputType)
				.map(this::process)
				.flatMap((R response) -> TypeGuard.validate(response, responseType))
				.orElse(null);
	}

	/**
	 * Processes the validated input and produces a result.
	 *
	 * @param input the input to process, guaranteed to be an instance of {@link T}.
	 * 
	 * @return the result of processing {@code input}.
	 */
	protected abstract R process(T input);
}
