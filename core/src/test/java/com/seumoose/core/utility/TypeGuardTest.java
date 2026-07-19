package com.seumoose.core.utility;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TypeGuardTest {

	@Test
	public void validate_withValidInput_returnsOptionalWithCastValue() {
		Optional<String> result = TypeGuard.validate("Lorem ipsum dolor sit amet", String.class);

		assertTrue(result.isPresent());
		assertEquals("Lorem ipsum dolor sit amet", result.get());
	}

	@Test
	public void validate_withNullInput_returnsEmptyOptional() {
		Optional<String> result = TypeGuard.validate(null, String.class);

		assertFalse(result.isPresent());
	}

	@Test
	public void validate_withTypeMismatch_returnsEmptyOptional() {
		Optional<Integer> result = TypeGuard.validate("Lorem ipsum dolor sit amet", Integer.class);

		assertFalse(result.isPresent());
	}

	@Test
	public void validate_withSubtype_returnsOptionalWithCastValue() {
		Object input = "Lorem ipsum dolor sit amet";
		Optional<CharSequence> result = TypeGuard.validate(input,
				CharSequence.class);

		assertTrue(result.isPresent());
		assertEquals("Lorem ipsum dolor sit amet", result.get());
	}
}
