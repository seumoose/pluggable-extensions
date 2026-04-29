package com.seumoose.core.interfaces;

import java.util.function.Predicate;

/**
 * A plugin that accepts an input and produces a boolean result.
 *
 * @param <T> the type of input accepted by the plugin.
 */
public interface IPredicatePlugin<T> extends IPlugin, Predicate<T> {
}
