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

	/**
	 * Constructs a new supplier extension bound to the given response type used to
	 * validate the produced value at runtime.
	 *
	 * @param responseType the {@link Class} of the response type {@link R}.
	 */
	protected AbstractSupplierExtension(Class<R> responseType) {
		this.responseType = responseType;
	}

	/**
	 * Delegates to {@link #process()} and validates that the result is an
	 * instance of {@link R}.
	 *
	 * If {@link #process()} returns {@code null} or a value that is not an
	 * instance of {@link R}, {@code null} is returned instead of throwing.
	 *
	 * @return the result of {@link #process()} if it matched the expected
	 *         type, otherwise {@code null}.
	 */
	@Override
	public final R get() {
		return Optional.ofNullable(process())
				.flatMap((R response) -> TypeGuard.validate(response, responseType))
				.orElse(null);
	}

	/**
	 * Produces a result.
	 *
	 * @return the supplied result of type {@link R}.
	 */
	protected abstract R process();
}
