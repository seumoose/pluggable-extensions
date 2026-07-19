package com.seumoose.core.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Utility class for safely validating and casting objects to an expected type
 * at runtime.
 * 
 * This is intended for scenarios where an object's type cannot be guaranteed at
 * compile time (e.g. values loaded from configuration, deserialized data, or
 * plugin/extension inputs), providing a null-safe, exception-free alternative
 * to a direct cast.
 */
public final class TypeGuard {
	private static final Logger LOGGER = LoggerFactory.getLogger(TypeGuard.class);

	/**
	 * Private initialiser - class to be used via static method.
	 */
	private TypeGuard() {
	}

	/**
	 * Validates that the given input is an instance of the expected type {@link T}
	 * returning an {@link Optional} value.
	 *
	 * @param <T>          the expected type.
	 * @param input        the object to validate.
	 * @param expectedType the {@link Class} type the input is expected to be an
	 *                     instance of.
	 * 
	 * @return an {@link Optional} containing the cast input of type {@link T}
	 *         otherwise an {@link Optional#empty()}.
	 */
	public static <T> Optional<T> validate(Object input, Class<T> expectedType) {
		if (input != null && !expectedType.isInstance(input)) {
			LOGGER.error("Type mismatch: expected {} but received {}",
					expectedType.getSimpleName(),
					input.getClass().getSimpleName(),
					new Throwable("Type mismatch stack trace"));

			return Optional.empty();
		}

		return Optional.ofNullable(expectedType.cast(input));
	}
}
