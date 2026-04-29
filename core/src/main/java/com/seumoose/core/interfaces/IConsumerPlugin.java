package com.seumoose.core.interfaces;

import java.util.function.Consumer;

/**
 * A plugin that accepts an input and produces no output.
 *
 * @param <T> the type of input accepted by the plugin.
 */
public interface IConsumerPlugin<T> extends IPlugin, Consumer<T> {
}
