package com.seumoose.core.interfaces;

import java.util.function.Function;

/**
 * A extension that accepts an input and produces an output.
 *
 * @param <T> the type of input accepted by the extension.
 * @param <R> the type of result produced by the extension.
 */
public interface IFunctionExtension<T, R> extends IExtension, Function<T, R> {
}
