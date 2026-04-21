package com.br.food.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PromotionExpirationScheduler {

	private final PromotionService promotionService;

	public PromotionExpirationScheduler(PromotionService promotionService) {
		this.promotionService = promotionService;
	}

	@Scheduled(cron = "0 0 0 * * *", zone = "America/Sao_Paulo")
	public void expirePromotionsDaily() {
		promotionService.disableExpiredPromotions();
	}
}
