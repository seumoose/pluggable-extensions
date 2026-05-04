package com.seumoose.extensions.b;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.seumoose.core.interfaces.IPluginConfiguration;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ExtensionBConfiguration implements IPluginConfiguration {
	private final String pattern;

	/**
	 * Class constructor.
	 *
	 * @param pattern regex pattern used for predicate evaluation.
	 */
	@JsonCreator
	public ExtensionBConfiguration(
			@JsonProperty("pattern") String pattern) {
		this.pattern = pattern;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFamily() {
		return ModuleConstants.FAMILY_NAME;
	}
}
