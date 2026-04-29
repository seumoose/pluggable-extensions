package com.seumoose.core.interfaces;

import java.util.function.Supplier;

/**
 * A plugin that produces an output with no input.
 *
 * @param <R> the type of result supplied by the plugin.
 */
public interface ISupplierPlugin<R> extends IPlugin, Supplier<R> {
}
