package com.seumoose.core.interfaces;

/**
 * Marker interface for all extension implementations. Concrete extensions
 * should implement one of the functional sub-interfaces (e.g.
 * {@link IRunnableExtension}, {@link IConsumerExtension},
 * {@link ISupplierExtension}, {@link IFunctionExtension},
 * {@link IPredicateExtension}).
 */
public interface IExtension {
	// TODO: do we need this additional typing? why would a consumer ever need to
	// introspect the implementation configuration..?
	// public interface IExtension<C extends IExtensionConfiguration> {
	/**
	 * Returns the {@link IExtensionConfiguration} implementation for extension
	 * introspection.
	 * 
	 * @return the {@link IExtensionConfiguration} implementation for the given
	 *         extension implementation.
	 */
	// public C getConfiguration();
}
