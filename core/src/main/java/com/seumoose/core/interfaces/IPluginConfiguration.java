package com.seumoose.core.interfaces;

/**
 * Marker interface that all configuration implementations must implement - used
 * by {@link IPluginProvider} implementations to provide required config values
 * to each plugin.
 */
public interface IPluginConfiguration {
	/**
	 * The plugin configuration's registered family (type), used for variant
	 * registration & retrieval.
	 *
	 * @return a {@link String} value of the plugin configuration's family (type).
	 */
	public String getFamily();
}
