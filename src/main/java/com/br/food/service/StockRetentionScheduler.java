package com.br.food.service;

import java.time.LocalDate;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StockRetentionScheduler {

	private final StockEntryService stockEntryService;

	public StockRetentionScheduler(StockEntryService stockEntryService) {
		this.stockEntryService = stockEntryService;
	}

	@Scheduled(cron = "0 0 0 * * *", zone = "America/Sao_Paulo")
	public void retainExpiredStockEntries() {
		stockEntryService.retainExpiredEntries(LocalDate.now());
	}
}
