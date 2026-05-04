package com.seumoose.core.services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.seumoose.core.ModuleConstants;
import com.seumoose.core.interfaces.IConsumerExtension;
import com.seumoose.core.interfaces.IExtension;
import com.seumoose.core.interfaces.IExtensionConfiguration;
import com.seumoose.core.interfaces.IExtensionProvider;
import com.seumoose.core.interfaces.IFunctionExtension;
import com.seumoose.core.interfaces.IPredicateExtension;
import com.seumoose.core.interfaces.IRunnableExtension;
import com.seumoose.core.interfaces.ISupplierExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
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

public class ExtensionRegistration {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionRegistration.class);
	private static final Path CONFIGURATION_PATH_ROOT = resolveConfigurationRoot();
	private static final String USER_HOME = "user.home";
	private static final String CONFIG_PATH = "config";
	private static final String EXTENSION_PATH = "runtime-extensions";
	private static final String DEFAULT_CONFIGURATION_FILE = "defaults.json";

	private final Map<String, IExtensionProvider<?>> extensionFamilyProviderMapping = new HashMap<>();
	private final Map<String, Map<String, IExtension>> extensionFamilyVariantMapping = new HashMap<>();
	private final Map<String, ObjectNode> extensionFamilyDefaults = new HashMap<>();
	private final ObjectMapper mapper = new ObjectMapper()
			.findAndRegisterModules()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	private ExtensionRegistration() {
		// discover bundled extensions from the application classpath by default at
		// start
		registerExtensionProviders(
				ServiceLoader.load(IExtensionProvider.class),
				Collections.emptySet());
	}

	private static class Holder {
		static final ExtensionRegistration INSTANCE = new ExtensionRegistration();
	}

	public static ExtensionRegistration getInstance() {
		return Holder.INSTANCE;
	}

	public synchronized Optional<IExtension> getExtension(String family, String variant) {
		// lazy provider discovery
		if (!extensionFamilyProviderMapping.containsKey(family)) {
			LOGGER.warn("No provider for {} — attempting reconciliation", family);

			URLClassLoader externalClassLoader = createExternalExtensionClassLoader();

			if (externalClassLoader != null) {
				registerExtensionProviders(
						ServiceLoader.load(IExtensionProvider.class, externalClassLoader),
						Set.of(family));
			}
		}

		// lazy variant registration
		if (extensionFamilyProviderMapping.containsKey(family)) {
			Map<String, IExtension> variants = extensionFamilyVariantMapping.get(family);

			if (variants == null || !variants.containsKey(variant)) {
				LOGGER.warn("Variant {} not found for {} — reconciling extension variant configurations", variant,
						family);
				registerExtensionVariants(extensionFamilyProviderMapping.get(family));
			}
		}

		return Optional.ofNullable(extensionFamilyVariantMapping.get(family))
				.map((Map<String, IExtension> variants) -> variants.get(variant));
	}

	public synchronized Optional<IRunnableExtension> getRunnableExtension(String family, String variant) {
		return getExtension(family, variant)
				.filter(IRunnableExtension.class::isInstance)
				.map(IRunnableExtension.class::cast);
	}

	@SuppressWarnings("unchecked")
	public synchronized <T> Optional<IConsumerExtension<T>> getConsumerExtension(String family, String variant) {
		return getExtension(family, variant)
				.filter(IConsumerExtension.class::isInstance)
				.map((IExtension extension) -> (IConsumerExtension<T>) extension);
	}

	@SuppressWarnings("unchecked")
	public synchronized <R> Optional<ISupplierExtension<R>> getSupplierExtension(String family, String variant) {
		return getExtension(family, variant)
				.filter(ISupplierExtension.class::isInstance)
				.map((IExtension extension) -> (ISupplierExtension<R>) extension);
	}

	@SuppressWarnings("unchecked")
	public synchronized <T, R> Optional<IFunctionExtension<T, R>> getFunctionExtension(String family, String variant) {
		return getExtension(family, variant)
				.filter(IFunctionExtension.class::isInstance)
				.map((IExtension extension) -> (IFunctionExtension<T, R>) extension);
	}

	@SuppressWarnings("unchecked")
	public synchronized <T> Optional<IPredicateExtension<T>> getPredicateExtension(String family, String variant) {
		return getExtension(family, variant)
				.filter(IPredicateExtension.class::isInstance)
				.map((IExtension extension) -> (IPredicateExtension<T>) extension);
	}

	@SuppressWarnings("rawtypes")
	private void registerExtensionProviders(ServiceLoader<IExtensionProvider> serviceLoader,
			Set<String> filteredProviders) {
		Iterator<IExtensionProvider> iterator = serviceLoader.iterator();

		while (iterator.hasNext()) {
			IExtensionProvider<?> extensionProvider;

			if (!filteredProviders.isEmpty()
					&& extensionFamilyProviderMapping.keySet().containsAll(filteredProviders)) {
				break;
			}

			try {
				extensionProvider = iterator.next();
			} catch (ServiceConfigurationError error) {
				LOGGER.error(
						"Failed to load extension provider - check the extension's `resources/META-INF/services` registrations for stale class references - failed with error: {}",
						error.getMessage());

				continue;
			}

			// skip extension provider registration if the set of filtered providers is not
			// empty and the current found extension provider is not contained within the
			// set
			// or if the extension provider mapping already contains the current provider
			if ((!filteredProviders.isEmpty() && !filteredProviders.contains(extensionProvider.getFamily()))
					|| extensionFamilyProviderMapping.containsKey(extensionProvider.getFamily())) {
				continue;
			}

			extensionFamilyProviderMapping.put(extensionProvider.getFamily(), extensionProvider);
			LOGGER.info("Successfully registered extension provider for {}",
					extensionProvider.getFamily());
		}
	}

	private URLClassLoader createExternalExtensionClassLoader() {
		String envPath = System.getenv(ModuleConstants.EXTENSION_ROOT_PATH);
		String propertyPath = System.getProperty(ModuleConstants.EXTENSION_ROOT_PATH);
		Path extensionDirectory = Path.of(System.getProperty(USER_HOME), EXTENSION_PATH);

		if (envPath != null) {
			extensionDirectory = Path.of(envPath);
		}

		if (propertyPath != null) {
			extensionDirectory = Path.of(propertyPath);
		}

		if (extensionDirectory == null || !Files.isDirectory(extensionDirectory)) {
			LOGGER.info(
					"No external extension directory configured or directory does not exist — skipping external extension discovery");

			return null;
		}

		try (Stream<Path> jars = Files.list(extensionDirectory)) {
			URL[] jarUrls = jars
					.filter((Path path) -> path.toString().toLowerCase().endsWith(".jar"))
					.map(ExtensionRegistration::toUrl)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.toArray(URL[]::new);

			LOGGER.info("External extension classloader created with {} jar(s) from {}", jarUrls.length,
					extensionDirectory);

			return new URLClassLoader(jarUrls, ExtensionRegistration.class.getClassLoader());
		} catch (IOException exception) {
			LOGGER.error("Failed to scan external extension directory {} - skipping external extension discovery",
					extensionDirectory);

			return null;
		}
	}

	private <T extends IExtensionConfiguration> void registerExtensionVariants(
			IExtensionProvider<T> extensionProvider) {
		String extensionFamily = extensionProvider.getFamily();
		Path familyDirectory = CONFIGURATION_PATH_ROOT.resolve(extensionFamily);

		if (!Files.isDirectory(familyDirectory)) {
			LOGGER.warn("No configuration directory found for extension family {}", extensionFamily);

			return;
		}

		Map<String, IExtension> extensionVariants = extensionFamilyVariantMapping
				.computeIfAbsent(extensionFamily, (String key) -> new HashMap<>());

		// load family default configuration once and cache it
		if (!extensionFamilyDefaults.containsKey(extensionFamily)) {
			Path defaultConfigPath = familyDirectory.resolve(DEFAULT_CONFIGURATION_FILE);

			if (Files.isRegularFile(defaultConfigPath)) {
				try {
					ObjectNode defaultNode = (ObjectNode) mapper.readTree(defaultConfigPath.toFile());
					extensionFamilyDefaults.put(extensionFamily, defaultNode);
					LOGGER.info("Loaded default configuration for extension family {}", extensionFamily);
				} catch (IOException e) {
					LOGGER.error("Failed to read default configuration for extension family {} - skipping defaults",
							extensionFamily);
				}
			}
		}

		try (Stream<Path> paths = Files.list(familyDirectory)) {
			List<Path> filteredPaths = paths
					.filter(ExtensionRegistration::isJsonFile)
					.filter(path -> !path.getFileName().toString().equals(DEFAULT_CONFIGURATION_FILE))
					.toList();

			for (Path path : filteredPaths) {
				String fileName = path.getFileName().toString();
				String variantName = fileName.substring(0, fileName.lastIndexOf("."));
				T extensionConfiguration;

				// skip re-registering a extension variation if configuration already exists
				if (extensionVariants.containsKey(variantName)) {
					continue;
				}

				try {
					// attempt to merge variant config values with default (if available) &
					// create config instance of the merged node
					ObjectNode variantNode = (ObjectNode) mapper.readTree(path.toFile());
					ObjectNode mergedNode = extensionFamilyDefaults.containsKey(extensionFamily)
							? deepMerge(extensionFamilyDefaults.get(extensionFamily).deepCopy(), variantNode)
							: variantNode;

					extensionConfiguration = mapper.treeToValue(mergedNode, extensionProvider.configurationType());
				} catch (IOException exception) {
					String errorMessage = MessageFormat.format(
							"Configuration type mismatch for extension provider {0} when reading in {1} - skipping variant registration",
							extensionFamily,
							fileName);

					LOGGER.error(errorMessage);
					continue;
				}

				IExtension extension = extensionProvider.create(extensionConfiguration);
				extensionVariants.put(variantName, extension);
			}
		} catch (IOException error) {
			String errorMessage = MessageFormat.format(
					"Failed to read directory {0} when discovering configuration for extension provider {1} - skipping provider variants registration",
					familyDirectory,
					extensionFamily);

			LOGGER.error(errorMessage);
			throw new IllegalStateException(errorMessage);
		}

		LOGGER.info("Successfully registered extension variants {} for extension provider {}",
				extensionVariants.keySet(),
				extensionFamily);
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
		String envPath = System.getenv(ModuleConstants.CONFIGURATION_PATH_ROOT);
		String propertyPath = System.getProperty(ModuleConstants.CONFIGURATION_PATH_ROOT);
		Path configPath = Path.of(System.getProperty(USER_HOME), CONFIG_PATH);

		if (envPath != null) {
			configPath = Path.of(envPath);
		}

		if (propertyPath != null) {
			configPath = Path.of(propertyPath);
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

	private static Optional<URL> toUrl(Path path) {
		try {
			return Optional.of(path.toUri().toURL());
		} catch (MalformedURLException exception) {
			LOGGER.warn("Skipping malformed jar path: {}", path);

			return Optional.empty();
		}
	}
}
