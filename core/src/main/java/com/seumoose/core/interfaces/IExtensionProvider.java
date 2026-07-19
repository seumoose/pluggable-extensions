package com.seumoose.core.interfaces;

/**
 * Simple interface that defines the contract for extension provider
 * implementations.
 * 
 * @param <T> the {@link IExtensionConfiguration} implementation.
 */
public interface IExtensionProvider<T extends IExtensionConfiguration> {
	/**
	 * The extension providers's registered family (type), used for variant
	 * registration & retrieval.
	 *
	 * @return a {@link String} value of the extension provider's family (type).
	 */
	public String getFamily();

	/**
	 * The extension provider's registered configuration class type used to
	 * instantiate and create extension implementation variants.
	 * 
	 * @return the {@link Class} concrete implementation of a configuration object
	 *         of type {@link T} extending {@link IExtensionConfiguration}.
	 */
	public Class<T> configurationType();

	/**
	 * Creates and initialises the extension with any required class-specific values
	 * during registration of the extension.
	 * 
	 * @param configuration extension specific configuration of type {@link T} used
	 *                      when instantiating the extension.
	 * 
	 * @return an instantiated extension of type {@link IExtension}.
	 */
	public IExtension create(T configuration);
}
