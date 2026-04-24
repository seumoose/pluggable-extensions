package com.seumoose.core.services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.seumoose.core.ModuleConstants;
import com.seumoose.core.interfaces.IPlugin;
import com.seumoose.core.interfaces.IPluginConfiguration;
import com.seumoose.core.interfaces.IPluginProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Stream;

public class PluginRegistration {
	private static final Logger LOGGER = LoggerFactory.getLogger(PluginRegistration.class);
	private static final Path CONFIGURATION_PATH_ROOT = resolveConfigurationRoot();
	private static final String DEFAULT_CONFIGURATION_FILE = "default.json";

	private final Map<String, IPluginProvider<?>> pluginFamilyProviderMapping = new HashMap<>();
	private final Map<String, Map<String, IPlugin>> pluginFamilyVariantMapping = new HashMap<>();
	private final Map<String, ObjectNode> pluginFamilyDefaults = new HashMap<>();
	private final ObjectMapper mapper = new ObjectMapper()
			.findAndRegisterModules()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	private PluginRegistration() {
		registerPluginProviders(Collections.emptySet());

		for (IPluginProvider<?> provider : pluginFamilyProviderMapping.values()) {
			registerPluginVariants(provider);
		}
	}

	private static class Holder {
		static final PluginRegistration INSTANCE = new PluginRegistration();
	}

	public static PluginRegistration getInstance() {
		return Holder.INSTANCE;
	}

	public synchronized Optional<IPlugin> getPlugin(String family, String variant) {
		// lazy provider discovery
		if (!pluginFamilyProviderMapping.containsKey(family)) {
			LOGGER.warn("No provider for {} — attempting reconciliation", family);
			registerPluginProviders(Set.of(family));
		}

		// lazy variant registration
		if (pluginFamilyProviderMapping.containsKey(family)) {
			Map<String, IPlugin> variants = pluginFamilyVariantMapping.get(family);

			if (variants == null || !variants.containsKey(variant)) {
				LOGGER.warn("Variant {} not found for {} — reconciling plugin variant configurations", variant, family);
				registerPluginVariants(pluginFamilyProviderMapping.get(family));
			}
		}

		return Optional.ofNullable(pluginFamilyVariantMapping.get(family))
				.map((Map<String, IPlugin> variants) -> variants.get(variant));
	}

	@SuppressWarnings("rawtypes")
	private void registerPluginProviders(Set<String> filteredProviders) {
		ServiceLoader<IPluginProvider> pluginProviderLoader = ServiceLoader.load(IPluginProvider.class);
		Iterator<IPluginProvider> iterator = pluginProviderLoader.iterator();

		while (iterator.hasNext()) {
			IPluginProvider<?> pluginProvider;

			try {
				pluginProvider = iterator.next();
			} catch (ServiceConfigurationError error) {
				LOGGER.error(
						"Failed to load plugin provider - check META-INF/services registrations for stale class references - failed with error: {}",
						error.getMessage());

				continue;
			}

			// skip plugin provider registration if the set of filtered providers is not
			// empty and the current found plugin provider is not contained within the set
			// or if the plugin provider mapping already contains the current provider
			if ((!filteredProviders.isEmpty() && !filteredProviders.contains(pluginProvider.getFamily()))
					|| pluginFamilyProviderMapping.containsKey(pluginProvider.getFamily())) {
				continue;
			}

			pluginFamilyProviderMapping.put(pluginProvider.getFamily(), pluginProvider);
			LOGGER.info("Successfully registered plugin provider for {}",
					pluginProvider.getFamily());
		}
	}

	private <T extends IPluginConfiguration> void registerPluginVariants(
			IPluginProvider<T> pluginProvider) {
		String pluginFamily = pluginProvider.getFamily();
		Path familyDirectory = CONFIGURATION_PATH_ROOT.resolve(pluginFamily);

		if (!Files.isDirectory(familyDirectory)) {
			LOGGER.warn("No configuration directory found for plugin family {}", pluginFamily);

			return;
		}

		Map<String, IPlugin> pluginVariants = pluginFamilyVariantMapping
				.computeIfAbsent(pluginFamily, k -> new HashMap<>());

		// load family default configuration once and cache it
		if (!pluginFamilyDefaults.containsKey(pluginFamily)) {
			Path defaultConfigPath = familyDirectory.resolve(DEFAULT_CONFIGURATION_FILE);

			if (Files.isRegularFile(defaultConfigPath)) {
				try {
					ObjectNode defaultNode = (ObjectNode) mapper.readTree(defaultConfigPath.toFile());
					pluginFamilyDefaults.put(pluginFamily, defaultNode);
					LOGGER.info("Loaded default configuration for plugin family {}", pluginFamily);
				} catch (IOException e) {
					LOGGER.error("Failed to read default configuration for plugin family {} - skipping defaults",
							pluginFamily);
				}
			}
		}

		try (Stream<Path> paths = Files.list(familyDirectory)) {
			List<Path> filteredPaths = paths
					.filter(PluginRegistration::isJsonFile)
					.filter(p -> !p.getFileName().toString().equals(DEFAULT_CONFIGURATION_FILE))
					.toList();

			for (Path path : filteredPaths) {
				String fileName = path.getFileName().toString();
				String variantName = fileName.substring(0, fileName.lastIndexOf("."));
				T pluginConfiguration;

				// skip re-registering a plugin variation if configuration already exists
				if (pluginVariants.containsKey(variantName)) {
					continue;
				}

				try {
					ObjectNode variantNode = (ObjectNode) mapper.readTree(path.toFile());
					ObjectNode mergedNode = pluginFamilyDefaults.containsKey(pluginFamily)
							? deepMerge(pluginFamilyDefaults.get(pluginFamily).deepCopy(), variantNode)
							: variantNode;

					pluginConfiguration = mapper.treeToValue(mergedNode, pluginProvider.configurationType());
				} catch (IOException exception) {
					String errorMessage = MessageFormat.format(
							"Configuration type mismatch for plugin provider {0} when reading in {1} - skipping variant registration",
							pluginFamily,
							fileName);

					LOGGER.error(errorMessage);
					continue;
				}

				IPlugin plugin = pluginProvider.create(pluginConfiguration);
				pluginVariants.put(variantName, plugin);
			}
		} catch (IOException error) {
			String errorMessage = MessageFormat.format(
					"Failed to read directory {0} when discovering configuration for plugin provider {1} - skipping provider variants registration",
					familyDirectory,
					pluginFamily);

			LOGGER.error(errorMessage);
			throw new IllegalStateException(errorMessage);
		}

		LOGGER.info("Successfully registered plugin variants {} for plugin provider {}",
				pluginVariants.keySet(),
				pluginFamily);
	}

	private static ObjectNode deepMerge(ObjectNode base, ObjectNode override) {
		override.fields().forEachRemaining((Entry<String, JsonNode> entry) -> {
			String fieldName = entry.getKey();
			JsonNode overrideValue = entry.getValue();
			JsonNode baseValue = base.get(fieldName);

			// prevent overwriting complex base values with a primitive overrides
			if (baseValue != null && baseValue.isObject() && !overrideValue.isObject()) {
				LOGGER.warn("Cannot replace complex base value object with a scalar/array - skipping {} override",
						fieldName);
				return;
			}

			// recursively call until base value is null - set value to override
			if (baseValue != null && baseValue.isObject() && overrideValue.isObject()) {
				deepMerge((ObjectNode) baseValue, (ObjectNode) overrideValue);
			} else {
				base.set(fieldName, overrideValue);
			}
		});

		return base;
	}

	private static Path resolveConfigurationRoot() {
		String envPath = System.getenv(ModuleConstants.CONFIGURATION_PATH_ROOT_ENV);
		Path configPath = Path.of(System.getProperty("user.home"), "config");

		if (envPath != null) {
			configPath = Path.of(envPath);
		}

		if (!Files.isDirectory(configPath)) {
			String errorMessage = MessageFormat.format("Configuration root `{0}` is not a directory", configPath);

			LOGGER.error(errorMessage);
			throw new IllegalStateException(errorMessage);
		}

		return configPath;
	}

	private static boolean isJsonFile(Path path) {
		String fileName = path.getFileName().toString();

		return Files.isRegularFile(path)
				&& fileName.toLowerCase().endsWith(".json")
				&& fileName.length() > ".json".length();
	}
}
