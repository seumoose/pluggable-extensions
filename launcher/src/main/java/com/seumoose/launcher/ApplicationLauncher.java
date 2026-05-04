package com.seumoose.launcher;

import com.seumoose.core.interfaces.IPredicateExtension;
import com.seumoose.core.interfaces.IRunnableExtension;
import com.seumoose.core.interfaces.ISupplierExtension;
import com.seumoose.core.services.ExtensionRegistration;

import java.io.IOException;
import java.util.Optional;

public class ApplicationLauncher {
	public static void main(String[] args) throws IOException {
		ExtensionRegistration extensionRegistration = ExtensionRegistration.getInstance();

		Optional<IRunnableExtension> extensionAExtensionVariant1 = extensionRegistration.getRunnableExtension(
				"ExtensionA",
				"Variant1");
		Optional<IRunnableExtension> extensionAExtensionVariant2 = extensionRegistration.getRunnableExtension(
				"ExtensionA",
				"Variant2");

		extensionAExtensionVariant1.ifPresent(IRunnableExtension::run);
		extensionAExtensionVariant2.ifPresent(IRunnableExtension::run);

		Optional<IPredicateExtension<String>> emailValidator = extensionRegistration.getPredicateExtension("ExtensionB",
				"EmailValidator");
		Optional<IPredicateExtension<String>> urlValidator = extensionRegistration.getPredicateExtension("ExtensionB",
				"UrlValidator");

		emailValidator.ifPresent((IPredicateExtension<String> extension) -> extension.test("user@example.com"));
		urlValidator.ifPresent((IPredicateExtension<String> extension) -> extension.test("https://example.com"));

		// {@link AbstractPredicateExtension} implementation protects against
		// ClassCastException errors
		Optional<IPredicateExtension<Integer>> invalidEmailValidator = extensionRegistration.getPredicateExtension(
				"ExtensionB",
				"EmailValidator");

		invalidEmailValidator.ifPresent((IPredicateExtension<Integer> extension) -> extension.test(1));

		// extension C not on the classpath — triggers external extension discovery
		Optional<ISupplierExtension<String>> userGreeter = extensionRegistration.getSupplierExtension("ExtensionC",
				"User");

		userGreeter.ifPresent((ISupplierExtension<String> extension) -> extension.get());
	}
}
