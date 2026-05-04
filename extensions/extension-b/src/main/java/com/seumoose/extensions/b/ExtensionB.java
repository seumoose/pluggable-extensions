package com.seumoose.extensions.b;

import com.seumoose.core.spi.AbstractPredicateExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

public class ExtensionB extends AbstractPredicateExtension<String> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionB.class);

	private final ExtensionBConfiguration configuration;
	private final Pattern compiledPattern;

	protected ExtensionB(ExtensionBConfiguration configuration) {
		super(String.class);
		this.configuration = configuration;
		this.compiledPattern = Pattern.compile(configuration.getPattern());
	}

	/**
	 * {@inheritDoc}
	 */
	// @Override
	public boolean process(String input) {
		boolean matches = compiledPattern.matcher(input).matches();
		LOGGER.info("Predicate test with pattern {} against input '{}' = {}",
				configuration.getPattern(), input, matches);

		return matches;
	}
}
