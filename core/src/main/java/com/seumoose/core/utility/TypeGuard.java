package com.seumoose.core.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public final class TypeGuard {
	private static final Logger LOGGER = LoggerFactory.getLogger(TypeGuard.class);

	private TypeGuard() {
	}

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
