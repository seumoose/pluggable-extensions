package com.seumoose.core.interfaces;

import java.util.function.Function;

/**
 * A plugin that accepts an input and produces an output.
 *
 * @param <T> the type of input accepted by the plugin.
 * @param <R> the type of result produced by the plugin.
 */
public interface IFunctionPlugin<T, R> extends IPlugin, Function<T, R> {
}
