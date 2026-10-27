package com.br.food.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import br.com.gloomy.platform.tenant.TenantDirectory;

class SchedulersTest {

	/** A directory that runs the action once, the way it does for one active tenant. */
	private static TenantDirectory directoryWithOneTenant() {
		TenantDirectory tenants = mock(TenantDirectory.class);
		doAnswer(invocation -> {
			((Runnable) invocation.getArgument(0)).run();
			return null;
		}).when(tenants).forEachActiveTenant(any());
		return tenants;
	}

	@Test
	void expiredPromotionsAreDisabledInsideTheTenantLoopOnly() {
		PromotionService promotionService = mock(PromotionService.class);
		TenantDirectory tenants = mock(TenantDirectory.class);
		PromotionExpirationScheduler scheduler = new PromotionExpirationScheduler(promotionService, tenants);

		scheduler.expirePromotionsDaily();
		verify(promotionService, never()).disableExpiredPromotions();
		verify(tenants).forEachActiveTenant(any());

		PromotionExpirationScheduler running = new PromotionExpirationScheduler(promotionService, directoryWithOneTenant());
		running.expirePromotionsDaily();
		verify(promotionService).disableExpiredPromotions();
	}

	@Test
	void expiredStockIsRetainedInsideTheTenantLoopOnly() {
		StockEntryService stockEntryService = mock(StockEntryService.class);
		TenantDirectory tenants = mock(TenantDirectory.class);

		new StockRetentionScheduler(stockEntryService, tenants).retainExpiredStockEntries();
		verify(stockEntryService, never()).retainExpiredEntries(any());

		new StockRetentionScheduler(stockEntryService, directoryWithOneTenant()).retainExpiredStockEntries();
		verify(stockEntryService).retainExpiredEntries(LocalDate.now());
	}
}
