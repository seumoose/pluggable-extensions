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

	protected AbstractConsumerExtension(Class<T> inputType) {
		this.inputType = inputType;
	}

	@Override
	public final void accept(Object input) {
		TypeGuard.validate(input, inputType)
				.ifPresent(this::process);
	}

	protected abstract void process(T input);
}
