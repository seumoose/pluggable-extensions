package com.seumoose.core.spi;

import com.seumoose.core.interfaces.IConsumerExtension;
import com.seumoose.core.utility.TypeGuard;

/**
 * A extension that accepts an input and produces no output.
 *
 * @param <T> the type of input accepted by the extension.
 */
public abstract class AbstractConsumerExtension<T> implements IConsumerExtension<T> {
	private final Class<T> inputType;

	/**
	 * Constructs a new consumer extension bound to the given input type used to
	 * validate values at runtime.
	 *
	 * @param inputType the {@link Class} of the input type {@link T}.
	 */
	protected AbstractConsumerExtension(Class<T> inputType) {
		this.inputType = inputType;
	}

	// TODO: have some sort of callback that can be used to check process state
	/**
	 * Validates that {@code input} is an instance of {@link T} before
	 * delegating to {@link #process(Object)}.
	 *
	 * If {@code input} is not an instance of {@link T}, this method does
	 * nothing instead of throwing.
	 *
	 * @param input the input to consume, expected to be an instance of {@link T}.
	 */
	@Override
	public final void accept(Object input) {
		TypeGuard.validate(input, inputType)
				.ifPresent(this::process);
	}

	/**
	 * Processes the validated input.
	 *
	 * @param input the input to process, guaranteed to be an instance of {@link T}.
	 */
	protected abstract void process(T input);
}
