package com.br.food.service;

import java.time.LocalDate;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.com.gloomy.platform.tenant.TenantDirectory;

@Component
public class StockRetentionScheduler {

	private final StockEntryService stockEntryService;
	private final TenantDirectory tenants;

	public StockRetentionScheduler(StockEntryService stockEntryService, TenantDirectory tenants) {
		this.stockEntryService = stockEntryService;
		this.tenants = tenants;
	}

	/** Runs once per active tenant (CMD-016); a suspended tenant gets no automation. */
	@Scheduled(cron = "0 0 0 * * *", zone = "America/Sao_Paulo")
	public void retainExpiredStockEntries() {
		tenants.forEachActiveTenant(() -> stockEntryService.retainExpiredEntries(LocalDate.now()));
	}
}
