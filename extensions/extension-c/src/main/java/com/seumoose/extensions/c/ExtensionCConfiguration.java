package com.seumoose.extensions.c;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.seumoose.core.interfaces.IExtensionConfiguration;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ExtensionCConfiguration implements IExtensionConfiguration {
	private final String greeting;
	private final String target;

	@JsonCreator
	public ExtensionCConfiguration(
			@JsonProperty("greeting") String greeting,
			@JsonProperty("target") String target) {
		this.greeting = greeting;
		this.target = target;
	}

	@Override
	public String getFamily() {
		return ModuleConstants.FAMILY_NAME;
	}
}
