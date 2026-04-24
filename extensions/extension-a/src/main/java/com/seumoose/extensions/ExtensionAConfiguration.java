package com.seumoose.extensions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.seumoose.core.interfaces.IPluginConfiguration;
import lombok.Getter;
import lombok.ToString;

import java.net.URI;

@Getter
@ToString
public class ExtensionAConfiguration implements IPluginConfiguration {
	private final URI baseEndpoint;
	private final int retryLimit;

	/**
	 * Class constructor.
	 * 
	 * @param baseEndpoint base URI used for outgoing requests for Extension A.
	 * @param retryLimit   maximum number of retry attempts for failed requests.
	 */
	@JsonCreator
	public ExtensionAConfiguration(
			@JsonProperty("baseEndpoint") URI baseEndpoint,
			@JsonProperty("retryLimit") int retryLimit) {
		this.baseEndpoint = baseEndpoint;
		this.retryLimit = retryLimit;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFamily() {
		return ModuleConstants.FAMILY_NAME;
	}
}