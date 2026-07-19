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
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Singleton registry responsible for discovering, loading and caching pluggable
 * extensions ({@link IExtension} implementations) supplied by
 * {@link IExtensionProvider} instances registered via the {@link ServiceLoader}
 * mechanism.
 * 
 * Extension providers are discovered both from the application's own classpath
 * at startup and lazily from external jars placed in a configurable
 * "runtime-extensions" directory when a requested extension family is not yet
 * known.
 * 
 * For each provider, per-variant configuration is loaded from JSON files under
 * a configurable configuration root directory (by default
 * {@code <user.home>/config}), merged with an optional family-level
 * `defaults.json`, and used to construct concrete {@link IExtension} instances
 * on demand.
 */
public class ExtensionRegistration {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionRegistration.class);
	private static final String USER_HOME = "user.home";
	private static final String CONFIG_PATH = "config";
	private static final String EXTENSION_PATH = "runtime-extensions";
	private static final String DEFAULT_CONFIGURATION_FILE = "defaults.json";
	private static final String JSON_EXTENSION = ".json";
	private static final String JAR_EXTENSION = ".jar";

	private final Path configurationRootPath = resolveConfigurationRoot();
	private final Map<String, IExtensionProvider<?>> extensionFamilyProviderMapping = new HashMap<>();
	private final Map<String, Map<String, IExtension>> extensionFamilyVariantMapping = new HashMap<>();
	private final Map<String, ObjectNode> extensionFamilyDefaults = new HashMap<>();
	private final ObjectMapper mapper = new ObjectMapper()
			.findAndRegisterModules()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	/**
	 * Private constructor - registers bundled extension providers found on the
	 * application classpath at startup, using the default {@link ServiceLoader}.
	 */
	private ExtensionRegistration() {
		// discover bundled extensions from the application classpath by default at
		// application start
		registerExtensionProviders(
				ServiceLoader.load(IExtensionProvider.class),
				Collections.emptySet());
	}

	/**
	 * Lazy holder used to defer creation of the singleton instance until first
	 * access (initialization-on-demand holder idiom), guaranteeing thread-safe lazy
	 * initialisation without explicit synchronization.
	 */
	private static class Holder {
		static final ExtensionRegistration INSTANCE = new ExtensionRegistration();
	}

	/**
	 * Returns the application-wide singleton instance of the extension registry.
	 *
	 * @return the singleton {@link ExtensionRegistration} instance.
	 */
	public static ExtensionRegistration getInstance() {
		return Holder.INSTANCE;
	}

	/**
	 * Package-private factory to create new instances for testing.
	 *
	 * @return a newly constructed {@link ExtensionRegistration} instance,
	 *         independent of the shared singleton.
	 */
	static ExtensionRegistration createInstance() {
		return new ExtensionRegistration();
	}

	/**
	 * Resolves a registered {@link IExtension} for the given family and variant,
	 * lazily discovering the provider and/or its variant configuration if not
	 * previously registered.
	 * 
	 * If no provider is currently registered for the given {@code family}, an
	 * attempt is made to discover one from external extension jars. If a provider
	 * is registered but the requested {@code variant} has not yet been loaded, its
	 * family's variant configurations are (re-)reconciled from disk.
	 *
	 * @param family  the extension family/provider identifier.
	 * @param variant the specific configuration variant within the family.
	 * 
	 * @return an {@link Optional} containing the resolved {@link IExtension}, or
	 *         {@link Optional#empty()} if no matching provider/variant could be
	 *         found.
	 */
	public synchronized Optional<IExtension> getExtension(String family, String variant) {
		// lazy provider discovery
		if (!extensionFamilyProviderMapping.containsKey(family)) {
			LOGGER.warn("No provider for {} — attempting reconciliation", family);

			Optional<URLClassLoader> externalClassLoader = createExternalExtensionClassLoader();

			if (externalClassLoader.isPresent()) {
				registerExtensionProviders(
						ServiceLoader.load(IExtensionProvider.class, externalClassLoader.get()),
						Set.of(family));
			}
		}

		// lazy variant registration
		if (extensionFamilyProviderMapping.containsKey(family)) {
			Map<String, IExtension> variants = extensionFamilyVariantMapping.get(family);

			if (variants == null || !variants.containsKey(variant)) {
				LOGGER.warn("Variant {} not found for {} — reconciling extension variant configurations",
						variant, family);
				registerExtensionVariants(extensionFamilyProviderMapping.get(family));
			}
		}

		return Optional.ofNullable(extensionFamilyVariantMapping.get(family))
				.map((Map<String, IExtension> variants) -> variants.get(variant));
	}

	/**
	 * Resolves the extension for the given family/variant as an
	 * {@link IConsumerExtension} if it implements the interface.
	 *
	 * @param <T>     the type consumed by the extension.
	 * @param family  the extension family/provider identifier.
	 * @param variant the specific configuration variant within the family.
	 * 
	 * @return an {@link Optional} containing the extension cast to
	 *         {@link IConsumerExtension}, or {@link Optional#empty()} if not found
	 *         or not of that type.
	 */
	@SuppressWarnings("unchecked")
	public synchronized <T> Optional<IConsumerExtension<T>> getConsumerExtension(String family, String variant) {
		return getExtension(family, variant)
				.filter(IConsumerExtension.class::isInstance)
				.map((IExtension extension) -> (IConsumerExtension<T>) extension);
	}

	/**
	 * Resolves the extension for the given family/variant as an
	 * {@link IFunctionExtension} if it implements that interface.
	 *
	 * @param <T>     the input type of the function extension.
	 * @param <R>     the return type of the function extension.
	 * @param family  the extension family/provider identifier.
	 * @param variant the specific configuration variant within the family.
	 * 
	 * @return an {@link Optional} containing the extension cast to
	 *         {@link IFunctionExtension}, or {@link Optional#empty()} if not found
	 *         or not of that type.
	 */
	@SuppressWarnings("unchecked")
	public synchronized <T, R> Optional<IFunctionExtension<T, R>> getFunctionExtension(String family, String variant) {
		return getExtension(family, variant)
				.filter(IFunctionExtension.class::isInstance)
				.map((IExtension extension) -> (IFunctionExtension<T, R>) extension);
	}

	/**
	 * Resolves the extension for the given family/variant as an
	 * {@link IPredicateExtension} if it implements that interface.
	 *
	 * @param <T>     the type tested by the predicate extension.
	 * @param family  the extension family/provider identifier.
	 * @param variant the specific configuration variant within the family.
	 * 
	 * @return an {@link Optional} containing the extension cast to
	 *         {@link IPredicateExtension}, or {@link Optional#empty()} if not found
	 *         or not of that type.
	 */
	@SuppressWarnings("unchecked")
	public synchronized <T> Optional<IPredicateExtension<T>> getPredicateExtension(String family, String variant) {
		return getExtension(family, variant)
				.filter(IPredicateExtension.class::isInstance)
				.map((IExtension extension) -> (IPredicateExtension<T>) extension);
	}

	/**
	 * Resolves the extension for the given family/variant as an
	 * {@link IRunnableExtension} if it implements that interface.
	 *
	 * @param family  the extension family/provider identifier.
	 * @param variant the specific configuration variant within the family.
	 * 
	 * @return an {@link Optional} containing the extension cast to
	 *         {@link IRunnableExtension}, or {@link Optional#empty()} if not found
	 *         or not of that type.
	 */
	public synchronized Optional<IRunnableExtension> getRunnableExtension(String family, String variant) {
		return getExtension(family, variant)
				.filter(IRunnableExtension.class::isInstance)
				.map(IRunnableExtension.class::cast);
	}

	/**
	 * Resolves the extension for the given family/variant as an
	 * {@link ISupplierExtension} if it implements that interface.
	 *
	 * @param <R>     the type supplied by the extension.
	 * @param family  the extension family/provider identifier.
	 * @param variant the specific configuration variant within the family.
	 * 
	 * @return an {@link Optional} containing the extension cast to
	 *         {@link ISupplierExtension}, or {@link Optional#empty()} if not found
	 *         or not of that type.
	 */
	@SuppressWarnings("unchecked")
	public synchronized <R> Optional<ISupplierExtension<R>> getSupplierExtension(String family, String variant) {
		return getExtension(family, variant)
				.filter(ISupplierExtension.class::isInstance)
				.map((IExtension extension) -> (ISupplierExtension<R>) extension);
	}

	/**
	 * Iterates the given {@link ServiceLoader} of {@link IExtensionProvider}s
	 * registering any provider whose family is not already known and, if
	 * {@code filteredProviders} is non-empty, whose family is contained within the
	 * set. Iteration stops early once all families in {@code filteredProviders}
	 * have been registered if present.
	 *
	 * @param serviceLoader     the service loader to iterate for available
	 *                          extension providers.
	 * @param filteredProviders the set of families to restrict registration to, or
	 *                          an empty set to register all discovered providers.
	 */
	@SuppressWarnings("rawtypes")
	private void registerExtensionProviders(
			ServiceLoader<IExtensionProvider> serviceLoader,
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

			// skip extension provider registration if the extension provider mapping
			// already contains the current provider or if the set of filtered providers is
			// not empty and the current found extension provider is not contained within
			// said set
			if (extensionFamilyProviderMapping.containsKey(extensionProvider.getFamily())
					|| (!filteredProviders.isEmpty() && !filteredProviders.contains(extensionProvider.getFamily()))) {
				LOGGER.info("Skipping extension registration/re-registration for {}",
						extensionProvider.getFamily());

				continue;
			}

			extensionFamilyProviderMapping.put(extensionProvider.getFamily(), extensionProvider);
			LOGGER.info("Successfully registered extension provider for {}",
					extensionProvider.getFamily());
		}
	}

	/**
	 * Creates a {@link URLClassLoader} of all jar files found in the external
	 * extension directory - used to discover extension providers not bundled on the
	 * application's own classpath.
	 * 
	 * The directory is resolved from the
	 * {@code ModuleConstants#EXTENSION_ROOT_PATH} environment variable or system
	 * property (where the system property takes precedence), falling back to
	 * {@code <user.home>/runtime-extensions} if neither are set.
	 *
	 * @return an {@link Optional} containing {@link URLClassLoader} referencing
	 *         discovered jars, or {@link Optional#empty()} if no valid extension
	 *         directory could be resolved or read.
	 */
	private Optional<URLClassLoader> createExternalExtensionClassLoader() {
		String envPath = System.getenv(ModuleConstants.EXTENSION_ROOT_PATH);
		String propertyPath = System.getProperty(ModuleConstants.EXTENSION_ROOT_PATH);
		Path extensionDirectory = Path.of(System.getProperty(USER_HOME), EXTENSION_PATH);

		if (envPath != null) {
			extensionDirectory = Path.of(envPath);
		}

		if (propertyPath != null) {
			extensionDirectory = Path.of(propertyPath);
		}

		if (!Files.isDirectory(extensionDirectory)) {
			LOGGER.info(
					"No external extension directory configured or directory does not exist — skipping external extension discovery");

			return Optional.empty();
		}

		try (Stream<Path> jars = Files.list(extensionDirectory)) {
			URL[] jarUrls = jars
					.filter((isFileOfType(JAR_EXTENSION)))
					.map(ExtensionRegistration::toUrl)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.toArray(URL[]::new);

			LOGGER.info("External extension classloader created with {} jar(s) from {}", jarUrls.length,
					extensionDirectory);

			return Optional.of(new URLClassLoader(jarUrls, ExtensionRegistration.class.getClassLoader()));
		} catch (IOException exception) {
			LOGGER.error("Failed to scan external extension directory {} - skipping external extension discovery",
					extensionDirectory);

			return Optional.empty();
		}
	}

	/**
	 * Discovers and registers all variant configurations for the given extension
	 * provider by scanning its configuration directory (based on the provider's
	 * family) for JSON configuration files, loading and caching the family's
	 * {@code defaults.json} if present.
	 *
	 * @param <T>               the configuration type used by the extension
	 *                          provider.
	 * @param extensionProvider the provider whose variants should be discovered and
	 *                          registered.
	 * @throws IllegalStateException if the family's configuration directory cannot
	 *                               be.
	 */
	private <T extends IExtensionConfiguration> void registerExtensionVariants(
			IExtensionProvider<T> extensionProvider) {
		String extensionFamily = extensionProvider.getFamily();
		Path familyDirectory = configurationRootPath.resolve(extensionFamily);

		if (!Files.isDirectory(familyDirectory)) {
			LOGGER.warn("No configuration directory found for extension family {}", extensionFamily);

			return;
		}

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
					.filter(isFileOfType(JSON_EXTENSION))
					.filter(path -> !path.getFileName().toString().equals(DEFAULT_CONFIGURATION_FILE))
					.toList();

			for (Path path : filteredPaths) {
				registerExtensionVariant(path, extensionFamily, extensionProvider);
			}
		} catch (IOException error) {
			String errorMessage = MessageFormat.format(
					"Failed to read directory {0} when discovering configuration for extension provider {1} - skipping provider variants registration",
					familyDirectory, extensionFamily);

			LOGGER.error(errorMessage);
			throw new IllegalStateException(errorMessage);
		}
	}

	/**
	 * Registers a single extension variant from the given configuration file,
	 * merging its contents with the cached family defaults (if present) before
	 * deserialising into the provider's configuration type and creating the
	 * resulting {@link IExtension}.
	 * 
	 * The variant name is derived from the configuration file's name (without
	 * extension). Registration is skipped if a variant of that name is already
	 * registered.
	 *
	 * @param <T>               the configuration type used by the extension
	 *                          provider.
	 * @param path              the path to the variant's JSON configuration file.
	 * @param extensionFamily   the family the variant belongs to.
	 * @param extensionProvider the provider used to construct the extension from
	 *                          its configuration.
	 */
	private <T extends IExtensionConfiguration> void registerExtensionVariant(
			Path path,
			String extensionFamily,
			IExtensionProvider<T> extensionProvider) {
		Map<String, IExtension> extensionVariants = extensionFamilyVariantMapping
				.computeIfAbsent(extensionFamily, (String key) -> new HashMap<>());
		String fileName = path.getFileName().toString();
		String variantName = fileName.substring(0, fileName.lastIndexOf("."));
		T extensionConfiguration;

		// skip re-registering a extension variation if configuration already exists
		if (extensionVariants.containsKey(variantName)) {
			return;
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
					extensionFamily, fileName);

			LOGGER.error(errorMessage);
			return;
		}

		IExtension extension = extensionProvider
				.create(extensionConfiguration);
		extensionVariants.put(variantName, extension);

		LOGGER.info("Successfully registered extension variant {} for extension provider {}",
				variantName, extensionFamily);
	}

	/**
	 * Resolves the configuration root directory used to locate extension
	 * family variant configuration files.
	 * 
	 * The directory is resolved from the
	 * {@code ModuleConstants#CONFIGURATION_ROOT_PATH} environment variable or
	 * system property (where the system property takes precedence), falling back to
	 * {@code <user.home>/config} if neither are set.
	 *
	 * @return the resolved configuration root directory.
	 * 
	 * @throws IllegalStateException if the resolved path is not a directory.
	 */
	private Path resolveConfigurationRoot() {
		String envPath = System.getenv(ModuleConstants.CONFIGURATION_ROOT_PATH);
		String propertyPath = System.getProperty(ModuleConstants.CONFIGURATION_ROOT_PATH);
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

	/**
	 * Recursively merges the {@code override} JSON object into the {@code base}
	 * JSON object in place, using an explicit stack to avoid recursion.
	 * Object-valued fields are merged recursively while simple override values
	 * replace the corresponding base value directly. An override is skipped (with a
	 * warning logged) if it would replace a complex (object) base value with a
	 * non-object value.
	 *
	 * @param base     the base node to merge into; mutated and returned.
	 * @param override the node whose fields should be merged over {@code base}.
	 * 
	 * @return the {@code base} object after merging where possible.
	 */
	static ObjectNode deepMerge(ObjectNode base, ObjectNode override) {
		Deque<Map.Entry<ObjectNode, ObjectNode>> stack = new ArrayDeque<>();
		stack.push(Map.entry(base, override));

		while (!stack.isEmpty()) {
			Map.Entry<ObjectNode, ObjectNode> stackEntry = stack.pop();
			ObjectNode currentBase = stackEntry.getKey();
			ObjectNode currentOverride = stackEntry.getValue();

			currentOverride.fields().forEachRemaining((Entry<String, JsonNode> overrideEntry) -> {
				String fieldName = overrideEntry.getKey();
				JsonNode overrideValue = overrideEntry.getValue();
				JsonNode baseValue = currentBase.get(fieldName);

				// prevent overwriting complex base values with a primitive overrides
				if (baseValue != null && baseValue.isObject() && !overrideValue.isObject()) {
					LOGGER.warn("Cannot replace complex base value object with a scalar/array - skipping {} override",
							fieldName);

					return;
				}

				if (baseValue == null || !baseValue.isObject()) {
					currentBase.set(fieldName, overrideValue);
				} else {
					stack.push(Map.entry((ObjectNode) baseValue, (ObjectNode) overrideValue));
				}
			});
		}

		return base;
	}

	/**
	 * Builds a predicate matching files whose name ends with the given
	 * case-insensitive extension.
	 *
	 * @param extension the file extension to match, including the leading dot (e.g.
	 *                  {@code ".json"}).
	 * 
	 * @return a predicate that tests whether a given {@link Path} is a regular file
	 *         of the given type.
	 */
	static Predicate<Path> isFileOfType(String extension) {
		return (Path path) -> {
			String fileName = path.getFileName().toString();

			return Files.isRegularFile(path)
					&& fileName.toLowerCase().endsWith(extension)
					&& fileName.length() > extension.length();
		};
	}

	/**
	 * Converts a {@link Path} to a {@link URL} safely handling malformed paths.
	 *
	 * @param path the path to convert.
	 * 
	 * @return an {@link Optional} containing the resulting {@link URL} or,
	 *         {@link Optional#empty()} if the path could not be converted.
	 */
	static Optional<URL> toUrl(Path path) {
		try {
			return Optional.of(path.toUri().toURL());
		} catch (MalformedURLException exception) {
			LOGGER.warn("Skipping malformed jar path: {}", path);

			return Optional.empty();
		}
	}
}
