package com.seumoose.core.spi;

import com.seumoose.core.interfaces.ISupplierPlugin;
import com.seumoose.core.utility.TypeGuard;

import java.util.Optional;

/**
 * A plugin that produces an output with no input.
 *
 * @param <R> the type of result supplied by the plugin.
 */
public abstract class AbstractSupplierPlugin<R> implements ISupplierPlugin<R> {
	private final Class<R> responseType;

	protected AbstractSupplierPlugin(Class<R> responseType) {
		this.responseType = responseType;
	}

	@Override
	public final R get() {
		return Optional.ofNullable(process())
				.flatMap((R response) -> TypeGuard.validate(response, responseType))
				.orElse(null);
	}

	protected abstract R process();
}
