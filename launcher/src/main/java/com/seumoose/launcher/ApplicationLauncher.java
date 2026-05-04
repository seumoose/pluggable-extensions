package com.seumoose.launcher;

import com.seumoose.core.interfaces.IPredicatePlugin;
import com.seumoose.core.interfaces.IRunnablePlugin;
import com.seumoose.core.interfaces.ISupplierPlugin;
import com.seumoose.core.services.PluginRegistration;

import java.io.IOException;
import java.util.Optional;

public class ApplicationLauncher {
	public static void main(String[] args) throws IOException {
		PluginRegistration pluginRegistration = PluginRegistration.getInstance();

		Optional<IRunnablePlugin> extensionAPluginVariant1 = pluginRegistration.getRunnablePlugin("ExtensionA",
				"Variant1");
		Optional<IRunnablePlugin> extensionAPluginVariant2 = pluginRegistration.getRunnablePlugin("ExtensionA",
				"Variant2");

		extensionAPluginVariant1.ifPresent(IRunnablePlugin::run);
		extensionAPluginVariant2.ifPresent(IRunnablePlugin::run);

		Optional<IPredicatePlugin<String>> emailValidator = pluginRegistration.getPredicatePlugin("ExtensionB",
				"EmailValidator");
		Optional<IPredicatePlugin<String>> urlValidator = pluginRegistration.getPredicatePlugin("ExtensionB",
				"UrlValidator");

		emailValidator.ifPresent((IPredicatePlugin<String> plugin) -> plugin.test("user@example.com"));
		urlValidator.ifPresent((IPredicatePlugin<String> plugin) -> plugin.test("https://example.com"));

		// {@link AbstractPredicatePlugin} implementation protects against
		// ClassCastException errors
		Optional<IPredicatePlugin<Integer>> invalidEmailValidator = pluginRegistration.getPredicatePlugin("ExtensionB",
				"EmailValidator");

		invalidEmailValidator.ifPresent((IPredicatePlugin<Integer> plugin) -> plugin.test(1));

		// extension C not on the classpath — triggers external plugin discovery
		Optional<ISupplierPlugin<String>> userGreeter = pluginRegistration.getSupplierPlugin("ExtensionC", "User");

		userGreeter.ifPresent((ISupplierPlugin<String> plugin) -> plugin.get());
	}
}
