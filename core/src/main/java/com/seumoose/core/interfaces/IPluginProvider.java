package com.seumoose.core.interfaces;

/**
 * Simple interface that defines the contract for plugin provider
 * implementations.
 */
public interface IPluginProvider<T extends IPluginConfiguration> {
	/**
	 * The plugin providers's registered family (type), used for variant
	 * registration & retrieval.
	 *
	 * @return a {@link String} value of the plugin provider's family (type).
	 */
	public String getFamily();

	/**
	 * The plugin provider's registered configuration class type used to instantiate
	 * and create plugin implementation variants.
	 * 
	 * @return the registered configuration of type {@link Class<T>}.
	 */
	public Class<T> configurationType();

	/**
	 * Creates and initialises the plugin with any required class-specific values
	 * during registration of the plugin.
	 * 
	 * @param configuration plugin specific configuration of type {@link T} used
	 *                      when instantiating the plugin.
	 * 
	 * @return an instantiated plugin of type {@link IPlugin}.
	 */
	public IPlugin create(T configuration);
}
