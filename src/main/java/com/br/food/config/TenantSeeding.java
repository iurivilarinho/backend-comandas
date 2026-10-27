package com.br.food.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.gloomy.platform.tenant.TenantDirectory;

/**
 * Base data of every tenant, applied once per tenant at startup (CMD-016). The own instance has the single fixed
 * tenant, so a standalone database is seeded exactly as before. A tenant created later gets the same data from
 * {@code ComandasTenantAdminProvisioner}.
 */
@Configuration
public class TenantSeeding {

	@Bean
	CommandLineRunner seedEveryTenant(TenantDirectory tenants, DataInitializer baseData) {
		return args -> tenants.forEachActiveTenant(baseData::seedCurrentTenant);
	}
}
