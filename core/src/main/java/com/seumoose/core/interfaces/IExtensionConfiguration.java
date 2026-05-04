package com.seumoose.core.interfaces;

/**
 * Marker interface that all configuration implementations must implement - used
 * by {@link IExtensionProvider} implementations to provide required config
 * values
 * to each extension.
 */
public interface IExtensionConfiguration {
	/**
	 * The extension configuration's registered family (type), used for variant
	 * registration & retrieval.
	 *
	 * @return a {@link String} value of the extension configuration's family
	 *         (type).
	 */
	public String getFamily();
}
