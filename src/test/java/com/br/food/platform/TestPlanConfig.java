package com.br.food.platform;

import java.util.Map;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.gloomy.platform.entitlements.Entitlements;
import br.com.gloomy.platform.service.EntitlementService;
import br.com.gloomy.platform.tenant.TenantContext;

/**
 * Plans for tests. The Central is not reachable in these tests and the tenants are created by the owner (no
 * customer, so unmetered); this lets a test give one tenant a plan with limits and without a feature, while
 * everything else goes through the real {@link Entitlements} code.
 */
@Configuration
public class TestPlanConfig {

	/** tenant slug -> feature code -> limit */
	public static final Map<String, Map<String, Long>> LIMITS = new ConcurrentHashMap<>();

	/** "slug:feature" of boolean features the plan of that tenant does not include. */
	public static final Set<String> MISSING_FEATURES = ConcurrentHashMap.newKeySet();

	public static void reset() {
		LIMITS.clear();
		MISSING_FEATURES.clear();
	}

	@Bean
	Entitlements entitlements(ObjectProvider<EntitlementService> service) {
		return new Entitlements(false, service) {
			@Override
			public OptionalLong limit(String code) {
				Map<String, Long> limits = LIMITS.get(TenantContext.require().slug());
				if (limits != null && limits.containsKey(code)) {
					return OptionalLong.of(limits.get(code));
				}
				return super.limit(code);
			}

			@Override
			public boolean enabled(String code) {
				if (MISSING_FEATURES.contains(TenantContext.require().slug() + ":" + code)) {
					return false;
				}
				return super.enabled(code);
			}
		};
	}
}
