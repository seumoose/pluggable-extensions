package com.seumoose.core.interfaces;

import java.util.function.Predicate;

/**
 * A extension that accepts an input and produces a boolean result.
 *
 * @param <T> the type of input accepted by the extension.
 */
public interface IPredicateExtension<T> extends IExtension, Predicate<T> {
}
