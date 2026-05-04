package com.seumoose.core.spi;

import com.seumoose.core.interfaces.ISupplierExtension;
import com.seumoose.core.utility.TypeGuard;

import java.util.Optional;

/**
 * A extension that produces an output with no input.
 *
 * @param <R> the type of result supplied by the extension.
 */
public abstract class AbstractSupplierExtension<R> implements ISupplierExtension<R> {
	private final Class<R> responseType;

	protected AbstractSupplierExtension(Class<R> responseType) {
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
