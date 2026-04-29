package com.seumoose.extensions;

import com.seumoose.core.interfaces.IPredicatePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

public class ExtensionB implements IPredicatePlugin<String> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionB.class);

	private final ExtensionBConfiguration configuration;
	private final Pattern compiledPattern;

	protected ExtensionB(ExtensionBConfiguration configuration) {
		this.configuration = configuration;
		this.compiledPattern = Pattern.compile(configuration.getPattern());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean test(String input) {
		boolean matches = compiledPattern.matcher(input).matches();
		LOGGER.info("Predicate test with pattern {} against input '{}' = {}",
				configuration.getPattern(), input, matches);

		return matches;
	}
}
