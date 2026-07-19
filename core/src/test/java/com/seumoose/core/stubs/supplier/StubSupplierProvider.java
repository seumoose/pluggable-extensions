package com.seumoose.core.stubs.supplier;

import com.seumoose.core.interfaces.IExtension;
import com.seumoose.core.interfaces.IExtensionProvider;

public class StubSupplierProvider implements IExtensionProvider<StubSupplierConfiguration> {
	private static final String FAMILY = "StubSupplier";

	@Override
	public String getFamily() {
		return FAMILY;
	}

	@Override
	public Class<StubSupplierConfiguration> configurationType() {
		return StubSupplierConfiguration.class;
	}

	@Override
	public IExtension create(StubSupplierConfiguration configuration) {
		return new StubSupplierExtension(configuration);
	}
}
