package com.seumoose.core.services;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.seumoose.core.ModuleConstants;
import com.seumoose.core.interfaces.IConsumerExtension;
import com.seumoose.core.interfaces.IExtension;
import com.seumoose.core.interfaces.IFunctionExtension;
import com.seumoose.core.interfaces.IPredicateExtension;
import com.seumoose.core.interfaces.IRunnableExtension;
import com.seumoose.core.interfaces.ISupplierExtension;
import com.seumoose.core.stubs.external.StubExternalProvider;
import com.seumoose.core.stubs.external.StubSkippedProvider;
import com.seumoose.core.stubs.runnable.StubRunnableExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.properties.SystemProperties;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SystemStubsExtension.class)
public class ExtensionRegistrationTest {
	private static ExtensionRegistration extensionRegistration;

	private Logger logger;
	private ListAppender<ILoggingEvent> listAppender;

	@SystemStub
	private EnvironmentVariables stubEnvironment = new EnvironmentVariables();

	@SystemStub
	private SystemProperties stubProperties = new SystemProperties();

	@BeforeEach
	public void setUp() {
		logger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		listAppender = new ListAppender<>();
		listAppender.start();
		logger.addAppender(listAppender);

		stubEnvironment.set(ModuleConstants.CONFIGURATION_ROOT_PATH,
				Path.of("src/test/resources/config").toAbsolutePath().toString());

		extensionRegistration = ExtensionRegistration.createInstance();
	}

	@AfterEach
	public void tearDown() throws Exception {
		listAppender.list.clear();
		logger.detachAppender(listAppender);

		stubProperties.teardown();
		stubEnvironment.teardown();
	}

	@Nested
	class InstanceCreation {
		@Test
		void createInstance_whenNoPathConfigured_resolvesConfigurationRootFromUserHome() {
			stubEnvironment.set(ModuleConstants.CONFIGURATION_ROOT_PATH, null);
			System.clearProperty(ModuleConstants.CONFIGURATION_ROOT_PATH);

			stubProperties.set("user.home",
					Path.of("src/test/resources/").toAbsolutePath().toString());

			extensionRegistration = ExtensionRegistration.createInstance();
			Optional<IExtension> extension = extensionRegistration.getExtension("StubRunnable", "DefaultVariant");

			assertTrue(extension.isPresent());
		}

		@Test
		public void createInstance_whenEnvironmentVariableSet_resolvesConfigurationRootFromEnvironmentVariable() {
			stubEnvironment.set(ModuleConstants.CONFIGURATION_ROOT_PATH,
					Path.of("src/test/resources/config").toAbsolutePath().toString());

			extensionRegistration = ExtensionRegistration.createInstance();
			Optional<IExtension> extension = extensionRegistration.getExtension("StubRunnable", "DefaultVariant");

			assertTrue(extension.isPresent());
		}

		@Test
		public void createInstance_whenSystemPropertyAndEnvironmentVariableSet_resolvesConfigurationRootFromSystemProperty() {
			stubEnvironment.set(ModuleConstants.CONFIGURATION_ROOT_PATH,
					Path.of("src/test/resources/invalid").toAbsolutePath().toString());
			stubProperties.set(ModuleConstants.CONFIGURATION_ROOT_PATH,
					Path.of("src/test/resources/config").toAbsolutePath().toString());

			extensionRegistration = ExtensionRegistration.createInstance();
			Optional<IExtension> extension = extensionRegistration.getExtension("StubRunnable", "DefaultVariant");

			assertTrue(extension.isPresent());
		}

		@Test
		void createInstance_whenDefaultConfigurationPathDoesNotExist_throwsIllegalState() {
			stubEnvironment.set(ModuleConstants.CONFIGURATION_ROOT_PATH, null);
			System.clearProperty(ModuleConstants.CONFIGURATION_ROOT_PATH);

			IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
				ExtensionRegistration.createInstance();
			});

			assertTrue(exception.getMessage().matches("Configuration root `[^`]*/config` is not a directory"),
					"Expected message to match pattern but was: " + exception.getMessage());
		}
	}

	@Nested
	class InstanceAccess {
		@Test
		public void getInstance_whenCalledMultipleTimes_returnsSameInstance() {
			ExtensionRegistration instance1 = ExtensionRegistration.getInstance();
			ExtensionRegistration instance2 = ExtensionRegistration.getInstance();

			assertSame(instance1, instance2);
		}
	}

	@Nested
	class ProviderRegistration {
		@Test
		void registerExtensionProviders_whenProviderClassNotFound_logsError() {
			assertTrue(listAppender.list.stream()
					.anyMatch((ILoggingEvent event) -> event.getLevel().toString().equals("ERROR")
							&& event.getFormattedMessage().contains("Failed to load extension provider")
							&& event.getFormattedMessage().contains("NonExistentProvider not found")),
					"Expected ERROR log for failed extension provider reconciliation");
		}

		@Test
		void registerExtensionProviders_whenFilteredFamilyRegistered_skipsFurtherRegistration(@TempDir Path tempDir)
				throws Exception {
			Path runtimeDir = Files.createDirectory(tempDir.resolve("runtime-extensions"));
			Path jarPath = runtimeDir.resolve("stub-external.jar");

			writeServiceJar(jarPath, List.of(
					StubExternalProvider.class.getName(),
					"com.example.NonExistentProvider"));

			stubProperties.set(ModuleConstants.EXTENSION_ROOT_PATH, runtimeDir.toString());

			extensionRegistration = ExtensionRegistration.createInstance();
			extensionRegistration.getExtension("StubExternal", "DefaultVariant");

			assertTrue(listAppender.list.stream()
					.anyMatch(event -> event.getFormattedMessage()
							.contains("Successfully registered extension provider for StubExternal")));

			assertFalse(listAppender.list.stream()
					.anyMatch(event -> event.getFormattedMessage()
							.contains("Successfully registered extension provider for NonExistentProvider")));
		}

		@Test
		void registerExtensionProviders_whenNonRequestedProviderDiscovered_skipsRegistering(
				@TempDir Path tempDir) throws Exception {
			Path runtimeDir = Files.createDirectory(tempDir.resolve("runtime-extensions"));
			Path jarPath = runtimeDir.resolve("stub-external.jar");

			writeServiceJar(jarPath, List.of(
					StubSkippedProvider.class.getName(),
					StubExternalProvider.class.getName()));

			stubProperties.set(ModuleConstants.EXTENSION_ROOT_PATH, runtimeDir.toString());

			extensionRegistration = ExtensionRegistration.createInstance();
			extensionRegistration.getExtension("StubExternal", "DefaultVariant");

			assertFalse(listAppender.list.stream()
					.anyMatch(event -> event.getFormattedMessage()
							.contains("Successfully registered extension provider for StubSkipped")));

			assertTrue(listAppender.list.stream()
					.anyMatch(event -> event.getFormattedMessage()
							.contains("Successfully registered extension provider for StubExternal")));
		}

	}

	@Nested
	class VariantRegistration {
		@Test
		void registerExtensionVariants_whenVariantRequested_registersVariant() {
			extensionRegistration.getExtension("StubRunnable", "DefaultVariant");

			assertTrue(listAppender.list.stream()
					.anyMatch(event -> event.getFormattedMessage()
							.contains("Successfully registered extension variant")));
		}

		@Test
		void registerExtensionVariants_whenFamilyRegisteredForFirstTime_loadsDefaultConfiguration() {
			extensionRegistration.getExtension("StubRunnable", "DefaultVariant");

			assertTrue(listAppender.list.stream()
					.anyMatch(event -> event.getFormattedMessage()
							.contains("Loaded default configuration for extension family StubRunnable")));
		}

		@Test
		void registerExtensionVariants_whenNoDefaultConfigurationPresent_skipsLoadingDefault() {
			extensionRegistration.getExtension("StubFunction", "StubVariant");

			assertFalse(listAppender.list.stream()
					.anyMatch(event -> event.getFormattedMessage()
							.contains("Loaded default configuration for extension family StubFunction")));
		}

		@Test
		void registerExtensionVariants_whenRegisteredVariantRequested_skipsRegistration() {
			extensionRegistration.getExtension("StubSupplier", "StubVariant");
			extensionRegistration.getExtension("StubSupplier", "NonExistentVariant");

			long registrationLogCount = listAppender.list.stream()
					.filter(event -> event.getFormattedMessage()
							.contains("Successfully registered extension variant"))
					.count();

			assertEquals(1, registrationLogCount);
		}

		@Test
		void registerExtensionVariants_whenRegisteredFamilyWithRegisteredDefaultsRequested_skipsLoadingDefault() {
			extensionRegistration.getExtension("StubRunnable", "DefaultVariant");
			extensionRegistration.getExtension("StubRunnable", "NonExistentVariant");

			long defaultsLoadedCount = listAppender.list.stream()
					.filter(event -> event.getFormattedMessage()
							.contains("Loaded default configuration for extension family StubRunnable"))
					.count();

			assertEquals(1, defaultsLoadedCount);
		}

		@Test
		void registerExtensionVariants_whenFamilyConfigurationDirectoryDoesNotExist_skipsVariantRegistration() {
			extensionRegistration.getExtension("StubUnconfigured", "StubVariant");

			assertTrue(listAppender.list.stream()
					.anyMatch(event -> event.getFormattedMessage()
							.contains("No configuration directory found for extension family StubUnconfigured")));
		}

		@Test
		void registerExtensionVariants_whenVariantDefaultHasInvalidJson_skipsDefaultVariantConfiguration() {
			extensionRegistration.getExtension("StubSupplier", "StubVariant");

			assertTrue(listAppender.list.stream()
					.anyMatch((ILoggingEvent event) -> event.getLevel().toString().equals("ERROR")
							&& event.getFormattedMessage()
									.contains(
											"Failed to read default configuration for extension family StubSupplier - skipping defaults")));
		}

		@Test
		void registerExtensionVariants_whenVariantHasInvalidJson_skipsVariantRegistration() {
			extensionRegistration.getExtension("StubRunnable", "DefaultVariant");

			assertTrue(listAppender.list.stream()
					.anyMatch(event -> event.getFormattedMessage()
							.contains(
									"Configuration type mismatch for extension provider StubRunnable when reading in InvalidVariant.json - skipping variant registration")));
		}

		@Test
		@DisabledOnOs(OS.WINDOWS)
		void registerExtensionVariants_whenFamilyDirectoryIsUnreadable_throwsIllegalStateAndLogsError(
				@TempDir Path tempDir) throws IOException {
			Path configurationPath = Files.createDirectory(tempDir.resolve("config"));
			Path directoryPath = Files.createDirectory(configurationPath.resolve("StubRunnable"));
			stubEnvironment.set(ModuleConstants.CONFIGURATION_ROOT_PATH, configurationPath.toAbsolutePath().toString());

			extensionRegistration = ExtensionRegistration.createInstance();

			try {
				// remove read permission so Files.list() throws IOException
				directoryPath.toFile().setReadable(false);

				IllegalStateException exception = assertThrows(IllegalStateException.class,
						() -> extensionRegistration.getExtension("StubRunnable", "DefaultVariant"));

				assertTrue(exception.getMessage().contains("Failed to read directory"));
			} finally {
				directoryPath.toFile().setReadable(true);
			}
		}

		@Test
		@EnabledOnOs(OS.WINDOWS)
		void registerExtensionVariants_whenFamilyDirectoryIsUnreadable_throwsIllegalStateAndLogsError_windows(
				@TempDir Path tempDir) throws IOException {
			Path configurationPath = Files.createDirectory(tempDir.resolve("config"));
			Path directoryPath = Files.createDirectory(configurationPath.resolve("StubRunnable"));
			stubEnvironment.set(ModuleConstants.CONFIGURATION_ROOT_PATH, configurationPath.toAbsolutePath().toString());

			extensionRegistration = ExtensionRegistration.createInstance();

			// deny LIST_DIRECTORY via Windows ACLs
			AclFileAttributeView aclView = Files.getFileAttributeView(directoryPath, AclFileAttributeView.class);
			UserPrincipal user = directoryPath.getFileSystem().getUserPrincipalLookupService()
					.lookupPrincipalByName(System.getProperty("user.name"));
			List<AclEntry> originalAcl = aclView.getAcl();

			AclEntry denyEntry = AclEntry.newBuilder()
					.setType(AclEntryType.DENY)
					.setPrincipal(user)
					.setPermissions(AclEntryPermission.LIST_DIRECTORY, AclEntryPermission.READ_DATA)
					.build();

			// DENY entries take precedence when placed first
			List<AclEntry> restrictedAcl = new ArrayList<>(originalAcl);
			restrictedAcl.add(0, denyEntry);
			aclView.setAcl(restrictedAcl);

			try {
				IllegalStateException exception = assertThrows(IllegalStateException.class,
						() -> extensionRegistration.getExtension("StubRunnable", "DefaultVariant"));

				assertTrue(exception.getMessage().contains("Failed to read directory"));
			} finally {
				aclView.setAcl(originalAcl);
			}
		}
	}

	@Nested
	class ExtensionResolution {
		@Test
		public void getExtension_whenVariantDoesNotOverrideDefaults_returnsExtensionWithDefaultConfiguration() {
			Optional<IExtension> extension = extensionRegistration.getExtension("StubRunnable", "DefaultVariant");
			StubRunnableExtension realisedExtension = (StubRunnableExtension) extension.get();

			assertEquals("default-value",
					realisedExtension.getConfiguration().getValue());
		}

		@Test
		public void getExtension_whenVariantOverridesDefaults_returnsExtensionWithMergedConfiguration() {
			Optional<IExtension> extension = extensionRegistration.getExtension("StubRunnable", "StubVariant");
			StubRunnableExtension realisedExtension = (StubRunnableExtension) extension.get();

			assertEquals("merged-value",
					realisedExtension.getConfiguration().getValue());
		}

		@Test
		void getExtension_whenVariantAlreadyRegistered_returnsSameCachedInstance() {
			IExtension first = extensionRegistration.getExtension("StubRunnable",
					"DefaultVariant").get();

			IExtension second = extensionRegistration.getExtension("StubRunnable",
					"DefaultVariant").get();

			assertSame(first, second);
		}

		@Test
		public void getExtension_whenFamilyIsUnknown_returnsEmpty() {
			Optional<IExtension> extension = extensionRegistration.getExtension("UnknownFamily", "StubVariant");

			assertFalse(extension.isPresent());
		}

		@Test
		public void getExtension_whenVariantIsUnknown_returnsEmpty() {
			Optional<IExtension> extension = extensionRegistration.getExtension("StubRunnable", "NonExistentVariant");

			assertFalse(extension.isPresent());
		}

		@Test
		public void getExtension_whenVariantHasInvalidJson_returnsEmpty() {
			// InvalidVariant.json has invalid JSON - should be skipped during registration
			Optional<IExtension> extension = extensionRegistration.getExtension("StubRunnable", "InvalidVariant");

			assertFalse(extension.isPresent());
		}

		@Test
		void getExtension_whenNoExtensionPathConfigured_resolvesExternalDirectoryFromUserHome(
				@TempDir Path tempDir) throws IOException {
			Files.createDirectory(tempDir.resolve("runtime-extensions"));
			stubProperties.set("user.home", tempDir.toAbsolutePath().toString());
			Files.createFile(tempDir.resolve("test.jar"));

			extensionRegistration = ExtensionRegistration.createInstance();
			Optional<IExtension> extension = extensionRegistration.getExtension("ExternalFamily", "DefaultVariant");

			assertFalse(extension.isPresent());
		}

		@Test
		void getExtension_whenEnvironmentVariableSet_resolvesExternalDirectoryFromEnvironmentVariable(
				@TempDir Path tempDir) throws IOException {
			stubEnvironment.set(ModuleConstants.EXTENSION_ROOT_PATH,
					tempDir.resolve("runtime-extensions").toAbsolutePath().toString());

			Files.createDirectory(tempDir.resolve("runtime-extensions"));
			Files.createFile(tempDir.resolve("test.jar"));

			extensionRegistration = ExtensionRegistration.createInstance();
			Optional<IExtension> extension = extensionRegistration.getExtension("ExternalFamily", "DefaultVariant");

			assertFalse(extension.isPresent());
		}

		@Test
		void getExtension_whenSystemPropertyAndEnvironmentVariableSet_resolvesExternalDirectoryFromSystemProperty(
				@TempDir Path tempDir) throws IOException {
			stubEnvironment.set(ModuleConstants.EXTENSION_ROOT_PATH,
					tempDir.resolve("invalid").toAbsolutePath().toString());
			stubProperties.set(ModuleConstants.EXTENSION_ROOT_PATH,
					tempDir.resolve("runtime-extensions").toAbsolutePath().toString());

			Files.createDirectory(tempDir.resolve("runtime-extensions"));
			Files.createFile(tempDir.resolve("test.jar"));

			extensionRegistration = ExtensionRegistration.createInstance();
			Optional<IExtension> extension = extensionRegistration.getExtension("ExternalFamily", "DefaultVariant");

			assertFalse(extension.isPresent());
		}

		@Test
		void getExtension_whenExternalPathIsNotADirectory_skipsExternalDiscovery(
				@TempDir Path tempDir) throws IOException {
			Files.createFile(tempDir.resolve("runtime-extensions"));
			stubProperties.set("user.home", tempDir.toAbsolutePath().toString());

			extensionRegistration.getExtension("ExternalFamily", "DefaultVariant");

			assertTrue(listAppender.list.stream()
					.anyMatch((ILoggingEvent event) -> event.getFormattedMessage()
							.contains(
									"No external extension directory configured or directory does not exist — skipping external extension discovery")));
		}

		@Test
		@DisabledOnOs(OS.WINDOWS)
		void getExtension_whenExternalDirectoryIsUnreadable_returnsNullAndLogsError(
				@TempDir Path tempDir) throws IOException {
			Path directoryPath = Files.createDirectory(tempDir.resolve("runtime-extensions"));
			stubProperties.set(ModuleConstants.EXTENSION_ROOT_PATH, directoryPath.toAbsolutePath().toString());

			try {
				// remove read permission so Files.list() throws IOException
				directoryPath.toFile().setReadable(false);

				extensionRegistration = ExtensionRegistration.createInstance();
				Optional<IExtension> extension = extensionRegistration.getExtension("ExternalFamily", "DefaultVariant");

				assertFalse(extension.isPresent());
				assertTrue(listAppender.list.stream()
						.anyMatch((ILoggingEvent event) -> event.getLevel().toString().equals("ERROR")
								&& event.getFormattedMessage()
										.contains("Failed to scan external extension directory")));
			} finally {
				directoryPath.toFile().setReadable(true);
			}
		}

		@Test
		@EnabledOnOs(OS.WINDOWS)
		void getExtension_whenExternalDirectoryIsUnreadable_returnsNullAndLogsError_windows(
				@TempDir Path tempDir) throws IOException {
			Path directoryPath = Files.createDirectory(tempDir.resolve("runtime-extensions"));
			stubProperties.set(ModuleConstants.EXTENSION_ROOT_PATH,
					directoryPath.toAbsolutePath().toString());

			// deny LIST_DIRECTORY via Windows ACLs
			AclFileAttributeView aclView = Files.getFileAttributeView(directoryPath,
					AclFileAttributeView.class);
			UserPrincipal user = directoryPath.getFileSystem().getUserPrincipalLookupService()
					.lookupPrincipalByName(System.getProperty("user.name"));
			List<AclEntry> originalAcl = aclView.getAcl();

			AclEntry denyEntry = AclEntry.newBuilder()
					.setType(AclEntryType.DENY)
					.setPrincipal(user)
					.setPermissions(AclEntryPermission.LIST_DIRECTORY,
							AclEntryPermission.READ_DATA)
					.build();

			List<AclEntry> restrictedAcl = new ArrayList<>(originalAcl);
			// DENY entries take precedence when placed first
			restrictedAcl.add(0, denyEntry);
			aclView.setAcl(restrictedAcl);

			try {
				extensionRegistration = ExtensionRegistration.createInstance();
				Optional<IExtension> extension = extensionRegistration.getExtension("ExternalFamily", "Variant");

				assertFalse(extension.isPresent());
				assertTrue(listAppender.list.stream()
						.anyMatch((ILoggingEvent event) -> event.getLevel().toString().equals("ERROR")
								&& event.getFormattedMessage()
										.contains("Failed to scan external extension directory")));
			} finally {
				aclView.setAcl(originalAcl);
			}
		}

		@Test
		public void getConsumerExtension_whenFamilyImplementsConsumer_returnsConsumerExtension() {
			Optional<IConsumerExtension<String>> extension = extensionRegistration.getConsumerExtension(
					"StubConsumer", "StubVariant");

			extension.get().accept("Lorem ipsum");

			assertTrue(listAppender.list.stream()
					.anyMatch((ILoggingEvent event) -> event.getFormattedMessage()
							.contains("Stub consumer logging: test-Lorem ipsum")));
		}

		@Test
		public void getConsumerExtension_whenFamilyDoesNotImplementConsumer_returnsEmpty() {
			Optional<IConsumerExtension<String>> extension = extensionRegistration.getConsumerExtension(
					"StubRunnable", "StubVariant");

			assertFalse(extension.isPresent());
		}

		@Test
		public void getFunctionExtension_whenFamilyImplementsFunction_returnsFunctionExtension() {
			Optional<IFunctionExtension<String, String>> extension = extensionRegistration.getFunctionExtension(
					"StubFunction", "StubVariant");

			String result = extension.get().apply("Lorem ipsum");

			assertEquals("Lorem ipsum-processed", result);
		}

		@Test
		public void getFunctionExtension_whenFamilyDoesNotImplementFunction_returnsEmpty() {
			Optional<IFunctionExtension<String, String>> extension = extensionRegistration.getFunctionExtension(
					"StubRunnable", "StubVariant");

			assertFalse(extension.isPresent());
		}

		@Test
		public void getPredicateExtension_whenFamilyImplementsPredicate_returnsPredicateExtension() {
			Optional<IPredicateExtension<String>> extension = extensionRegistration.getPredicateExtension(
					"StubPredicate", "StubVariant");

			assertTrue(extension.get().test("lorem"));
		}

		@Test
		public void getPredicateExtension_whenFamilyDoesNotImplementPredicate_returnsEmpty() {
			Optional<IPredicateExtension<String>> extension = extensionRegistration.getPredicateExtension(
					"StubRunnable", "StubVariant");

			assertFalse(extension.isPresent());
		}

		@Test
		public void getRunnableExtension_whenFamilyImplementsRunnable_returnsRunnableExtension() {
			Optional<IRunnableExtension> extension = extensionRegistration.getRunnableExtension(
					"StubRunnable", "StubVariant");

			extension.get().run();

			assertTrue(listAppender.list.stream()
					.anyMatch((ILoggingEvent event) -> event.getFormattedMessage()
							.contains("Running from stub runnable implementation")));
		}

		@Test
		public void getRunnableExtension_whenFamilyDoesNotImplementRunnable_returnsEmpty() {
			Optional<IRunnableExtension> extension = extensionRegistration.getRunnableExtension(
					"StubConsumer", "StubVariant");

			assertFalse(extension.isPresent());
		}

		@Test
		public void getSupplierExtension_whenFamilyImplementsSupplier_returnsSupplierExtension() {
			Optional<ISupplierExtension<String>> extension = extensionRegistration.getSupplierExtension(
					"StubSupplier", "StubVariant");

			String result = extension.get().get();

			assertEquals("Lorem ipsum dolor sit amet", result);
		}

		@Test
		public void getSupplierExtension_whenFamilyDoesNotImplementSupplier_returnsEmpty() {
			Optional<ISupplierExtension<String>> extension = extensionRegistration.getSupplierExtension(
					"StubRunnable", "StubVariant");

			assertFalse(extension.isPresent());
		}
	}

	// TODO: move out of service class & therefore tests?
	@Test
	void deepMerge_whenOverrideContainsNestedObjects_mergesRecursivelyAndPreservesBaseKeys() {
		ObjectMapper mapper = new ObjectMapper();

		// base node value e.g. defaults.json with the structure:
		/**
		 * {
		 * "sharedValue": "default-value",
		 * "sharedNestedObject": {
		 * "nestedSharedValue":"default-nested",
		 * "exclusiveBaseNestedValue":"default-nested"
		 * },
		 * "exclusiveBaseValue": "default-value"
		 * }
		 */
		ObjectNode base = mapper.createObjectNode();

		base.put("sharedValue", "default-value");

		ObjectNode baseNested = mapper.createObjectNode();
		baseNested.put("nestedSharedValue", "default-nested");
		baseNested.put("exclusiveBaseNestedValue", "default-nested");
		base.set("sharedNestedObject", baseNested);

		base.put("exclusiveBaseValue", "default-value");

		// override node value e.g. variant config with the structure:
		/**
		 * {
		 * "sharedValue": "override-value",
		 * "sharedNestedObject": {
		 * "nestedSharedValue":"override-nested",
		 * "exclusiveOverrideNestedValue":"override-nested"
		 * },
		 * "exclusiveOverrideValue": "override-value"
		 * }
		 */
		ObjectNode override = mapper.createObjectNode();

		override.put("sharedValue", "override-value");

		ObjectNode overrideNested = mapper.createObjectNode();
		overrideNested.put("nestedSharedValue", "override-nested");
		overrideNested.put("exclusiveOverrideNestedValue", "override-nested");
		override.set("sharedNestedObject", overrideNested);

		override.put("exclusiveOverrideValue", "override-value");

		ObjectNode result = ExtensionRegistration.deepMerge(base.deepCopy(), override);

		// expected resultant merged node value
		/**
		 * {
		 * "sharedValue": "override-value",
		 * "sharedNestedObject": {
		 * "nestedSharedValue":"override-nested",
		 * "exclusiveBaseNestedValue":"default-nested",
		 * "exclusiveOverrideNestedValue":"override-nested"
		 * },
		 * "exclusiveBaseValue": "default-value",
		 * "exclusiveOverrideValue": "override-value"
		 * }
		 */
		assertEquals("override-value", result.get("sharedValue").asText());
		assertEquals("override-nested", result.get("sharedNestedObject").get("nestedSharedValue").asText());
		assertEquals("default-nested", result.get("sharedNestedObject").get("exclusiveBaseNestedValue").asText());
		assertEquals("override-nested", result.get("sharedNestedObject").get("exclusiveOverrideNestedValue").asText());
		assertEquals("default-value", result.get("exclusiveBaseValue").asText());
		assertEquals("override-value", result.get("exclusiveOverrideValue").asText());
	}

	// TODO: move out of service class & therefore tests?
	@Test
	void deepMerge_whenScalarOverridesObjectNode_preservesOriginalObject() {
		ObjectMapper mapper = new ObjectMapper();

		// base node value e.g. defaults.json with the structure:
		/**
		 * {
		 * "sharedValue": "default-value",
		 * "sharedNestedObject": {
		 * "nestedSharedValue":"default-nested"
		 * }
		 * }
		 */
		ObjectNode base = mapper.createObjectNode();

		base.put("sharedValue", "default-value");

		ObjectNode baseNested = mapper.createObjectNode();
		baseNested.put("nestedSharedValue", "default-nested");
		base.set("sharedNestedObject", baseNested);

		// override node value e.g. variant config with the structure:
		/**
		 * {
		 * "sharedValue": "override-value",
		 * "sharedNestedObject": override-nested"
		 * }
		 */
		ObjectNode override = mapper.createObjectNode();

		override.put("sharedValue", "override-value");
		override.put("sharedNestedObject", "override-nested");

		ObjectNode result = ExtensionRegistration.deepMerge(base.deepCopy(), override);

		// expected resultant merged node value
		/**
		 * {
		 * "sharedValue": "override-value",
		 * "sharedNestedObject": {
		 * "nestedSharedValue":"default-nested"
		 * }
		 * }
		 */
		assertEquals("override-value", result.get("sharedValue").asText());
		assertTrue(result.get("sharedNestedObject").isObject());
		assertEquals("default-nested", result.get("sharedNestedObject").get("nestedSharedValue").asText());
	}

	// TODO: move out of service class & therefore tests?
	@Test
	public void isFileOfType_whenFileHasMatchingExtension_returnsTrue(@TempDir Path tempDir) throws Exception {
		Path filePath = Files.createFile(tempDir.resolve("test.json"));

		Predicate<Path> predicate = ExtensionRegistration.isFileOfType(".json");
		boolean result = predicate.test(filePath);

		assertTrue(result);
	}

	// TODO: move out of service class & therefore tests?
	@Test
	public void isFileOfType_whenFileHasDifferentExtension_returnsFalse(@TempDir Path tempDir) throws Exception {
		Path filePath = Files.createFile(tempDir.resolve("test.json"));

		Predicate<Path> predicate = ExtensionRegistration.isFileOfType(".txt");
		boolean result = predicate.test(filePath);

		assertFalse(result);
	}

	// TODO: move out of service class & therefore tests?
	@Test
	void isFileOfType_whenFileNameIsOnlyExtension_returnsFalse(@TempDir Path tempDir) throws Exception {
		Path filePath = Files.createFile(tempDir.resolve(".json"));

		Predicate<Path> predicate = ExtensionRegistration.isFileOfType(".json");
		boolean result = predicate.test(filePath);

		assertFalse(result);
	}

	// TODO: move out of service class & therefore tests?
	@Test
	public void isFileOfType_whenPathIsDirectory_returnsFalse(@TempDir Path tempDir) throws Exception {
		Predicate<Path> predicate = ExtensionRegistration.isFileOfType(".json");
		boolean result = predicate.test(tempDir);

		assertFalse(result);
	}

	// TODO: move out of service class & therefore tests?
	@Test
	public void toUrl_whenPathIsValid_returnsUrl(@TempDir Path tempDir) throws Exception {
		Path filePath = Files.createFile(tempDir.resolve("test.jar"));
		Optional<URL> result = ExtensionRegistration.toUrl(filePath);

		assertTrue(result.isPresent());
	}

	// TODO: move out of service class & therefore tests?
	@Test
	public void toUrl_whenUriSchemeIsMalformed_returnsEmpty() {
		Path mockPath = mock(Path.class);
		URI badUri = URI.create("nonsense-scheme:///some/path");

		when(mockPath.toUri()).thenReturn(badUri);

		Optional<URL> result = ExtensionRegistration.toUrl(mockPath);

		assertFalse(result.isPresent());
	}

	private static void writeServiceJar(Path jarPath, List<String> providerClassNames) throws IOException {
		String serviceFile = "META-INF/services/com.seumoose.core.interfaces.IExtensionProvider";
		String body = String.join("\n", providerClassNames) + "\n";

		try (JarOutputStream jos = new JarOutputStream(
				Files.newOutputStream(jarPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
			jos.putNextEntry(new JarEntry("META-INF/"));
			jos.closeEntry();

			jos.putNextEntry(new JarEntry("META-INF/services/"));
			jos.closeEntry();

			jos.putNextEntry(new JarEntry(serviceFile));
			jos.write(body.getBytes(StandardCharsets.UTF_8));
			jos.closeEntry();
		}
	}
}
