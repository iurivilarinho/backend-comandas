package com.br.food.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.br.food.models.StockEntry;
import com.br.food.repository.StockConsumptionRepository;
import com.br.food.repository.StockEntryRepository;

@ExtendWith(MockitoExtension.class)
class StockEntryServiceTest {

	@Mock
	private StockEntryRepository stockEntryRepository;

	@Mock
	private StockConsumptionRepository stockConsumptionRepository;

	@Mock
	private ProductService productService;

	@InjectMocks
	private StockEntryService stockEntryService;

	@Test
	void retainExpiredEntriesShouldRetainOnlyExpiredReleasedEntries() {
		StockEntry expiredEntry = new StockEntry();
		expiredEntry.setAvailableQuantity(new BigDecimal("2.000"));
		expiredEntry.setRetained(false);

		when(stockEntryRepository.findAllByRetainedFalseAndExpirationDateBefore(LocalDate.of(2026, 4, 16)))
				.thenReturn(List.of(expiredEntry));

		int retainedCount = stockEntryService.retainExpiredEntries(LocalDate.of(2026, 4, 16));

		assertEquals(1, retainedCount);
		assertEquals(true, expiredEntry.isRetained());
		verify(stockEntryRepository).saveAll(List.of(expiredEntry));
	}
}
