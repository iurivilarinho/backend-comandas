package com.br.food.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.br.food.enums.Types.SupplyInvoiceStatus;
import com.br.food.models.SupplyInvoice;
import com.br.food.repository.SupplyInvoiceRepository;

@ExtendWith(MockitoExtension.class)
class SupplyInvoiceServiceTest {

	@Mock
	private SupplyInvoiceRepository supplyInvoiceRepository;

	@Mock
	private ProductService productService;

	@Mock
	private DocumentService documentService;

	@InjectMocks
	private SupplyInvoiceService supplyInvoiceService;

	@Test
	void updateStatusShouldAllowConfiguredTransition() {
		SupplyInvoice invoice = new SupplyInvoice();
		invoice.setStatus(SupplyInvoiceStatus.ALLOCATED);

		when(supplyInvoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

		supplyInvoiceService.updateStatus(1L, SupplyInvoiceStatus.IN_CONSUMPTION);

		assertEquals(SupplyInvoiceStatus.IN_CONSUMPTION, invoice.getStatus());
	}

	@Test
	void updateStatusShouldRejectInvalidTransition() {
		SupplyInvoice invoice = new SupplyInvoice();
		invoice.setStatus(SupplyInvoiceStatus.ALLOCATED);

		when(supplyInvoiceRepository.findById(2L)).thenReturn(Optional.of(invoice));

		assertThrows(IllegalStateException.class,
				() -> supplyInvoiceService.updateStatus(2L, SupplyInvoiceStatus.CONSUMED));
	}
}

