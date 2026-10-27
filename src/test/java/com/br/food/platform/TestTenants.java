package com.br.food.platform;

import br.com.gloomy.platform.enums.TenantStatus;
import br.com.gloomy.platform.model.TenantInfo;

/** Tenants for unit tests that need a tenant bound to the thread without booting the platform. */
public final class TestTenants {

	private TestTenants() {
	}

	public static TenantInfo tenant(String slug) {
		return new TenantInfo(1L, slug, "t_" + slug, slug, TenantStatus.ACTIVE, null, null, null, null, null, null, null,
				null, null);
	}
}
