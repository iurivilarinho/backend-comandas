package com.br.food.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.com.gloomy.platform.tenant.TenantDirectory;

@Component
public class PromotionExpirationScheduler {

	private final PromotionService promotionService;
	private final TenantDirectory tenants;

	public PromotionExpirationScheduler(PromotionService promotionService, TenantDirectory tenants) {
		this.promotionService = promotionService;
		this.tenants = tenants;
	}

	/** Runs once per active tenant (CMD-016); a suspended tenant gets no automation. */
	@Scheduled(cron = "0 0 0 * * *", zone = "America/Sao_Paulo")
	public void expirePromotionsDaily() {
		tenants.forEachActiveTenant(promotionService::disableExpiredPromotions);
	}
}
