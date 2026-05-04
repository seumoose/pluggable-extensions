package com.seumoose.core.spi;

import com.seumoose.core.interfaces.IConsumerPlugin;
import com.seumoose.core.utility.TypeGuard;

/**
 * A plugin that accepts an input and produces no output.
 *
 * @param <T> the type of input accepted by the plugin.
 */
public abstract class AbstractConsumerPlugin<T> implements IConsumerPlugin<T> {
	private final Class<T> inputType;

	protected AbstractConsumerPlugin(Class<T> inputType) {
		this.inputType = inputType;
	}

	@Override
	public final void accept(Object input) {
		TypeGuard.validate(input, inputType)
				.ifPresent(this::process);
	}

	protected abstract void process(T input);
}
