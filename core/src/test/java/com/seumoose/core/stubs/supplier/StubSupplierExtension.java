package com.seumoose.core.stubs.supplier;

import com.seumoose.core.spi.AbstractSupplierExtension;

public class StubSupplierExtension extends AbstractSupplierExtension<String> {
	private final StubSupplierConfiguration configuration;

	public StubSupplierExtension(StubSupplierConfiguration configuration) {
		super(String.class);
		this.configuration = configuration;
	}

	@Override
	protected String process() {
		return configuration.getMessage();
	}
}
