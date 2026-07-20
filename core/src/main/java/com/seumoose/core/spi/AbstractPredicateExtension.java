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

	/**
	 * Constructs a new predicate extension bound to the given input type used to
	 * validate values at runtime.
	 *
	 * @param inputType the {@link Class} of the input type {@link T}.
	 */
	protected AbstractPredicateExtension(Class<T> inputType) {
		this.inputType = inputType;
	}

	// TODO: do we actually want to throw an error instead of returning false by
	// default?
	/**
	 * Validates that {@code input} is an instance of {@link T} before
	 * delegating to {@link #process(Object)}.
	 *
	 * If {@code input} is not an instance of {@link T}, {@code false} is
	 * returned instead of throwing.
	 *
	 * @param input the input to test, expected to be an instance of {@link T}.
	 * 
	 * @return the result of {@link #process(Object)} if {@code input} matched
	 *         the expected type, otherwise {@code false}.
	 */
	@Override
	public final boolean test(Object input) {
		return TypeGuard.validate(input, inputType)
				.map(this::process)
				.orElse(false);
	}

	/**
	 * Processes the validated input and produces a boolean result.
	 *
	 * @param input the input to test, guaranteed to be an instance of {@link T}.
	 * 
	 * @return {@code true} if {@code input} satisfies the predicate, {@code false}
	 *         otherwise.
	 */
	protected abstract boolean process(T input);
}
