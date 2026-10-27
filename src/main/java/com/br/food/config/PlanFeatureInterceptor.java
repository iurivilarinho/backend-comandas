package com.br.food.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import br.com.gloomy.platform.entitlements.Entitlements;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Blocks the financial module and the profitability report (403 {@code /problems/feature-not-in-plan}) for a tenant
 * whose plan does not include the {@link PlanFeatures#FINANCIAL} feature (CMD-016). Registered for those routes only.
 */
@Component
public class PlanFeatureInterceptor implements HandlerInterceptor {

	private final Entitlements entitlements;

	public PlanFeatureInterceptor(Entitlements entitlements) {
		this.entitlements = entitlements;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		entitlements.require(PlanFeatures.FINANCIAL);
		return true;
	}
}
