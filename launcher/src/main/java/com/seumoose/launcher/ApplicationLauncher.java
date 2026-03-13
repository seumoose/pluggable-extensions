package com.seumoose.launcher;

import com.seumoose.core.interfaces.IPlugin;
import com.seumoose.core.services.PluginRegistration;

import java.io.IOException;
import java.util.Optional;

public class ApplicationLauncher {
	public static void main(String[] args) throws IOException {
		PluginRegistration pluginRegistration = PluginRegistration.getInstance();

		Optional<IPlugin> extensionAPluginVariant1 = pluginRegistration.getPlugin("ExtensionA", "Variant1");
		Optional<IPlugin> extensionAPluginVariant2 = pluginRegistration.getPlugin("ExtensionA", "Variant2");

		extensionAPluginVariant1.ifPresent(IPlugin::execute);
		extensionAPluginVariant2.ifPresent(IPlugin::execute);
	}
}
