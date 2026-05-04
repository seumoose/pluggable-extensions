package com.seumoose.core.interfaces;

import java.util.function.Consumer;

/**
 * A extension that accepts an input and produces no output.
 *
 * @param <T> the type of input accepted by the extension.
 */
public interface IConsumerExtension<T> extends IExtension, Consumer<T> {
}
