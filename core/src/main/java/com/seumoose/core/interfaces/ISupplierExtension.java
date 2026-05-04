package com.seumoose.core.interfaces;

import java.util.function.Supplier;

/**
 * A extension that produces an output with no input.
 *
 * @param <R> the type of result supplied by the extension.
 */
public interface ISupplierExtension<R> extends IExtension, Supplier<R> {
}
