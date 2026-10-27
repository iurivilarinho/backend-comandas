package com.br.food.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import br.com.gloomy.platform.entitlements.Entitlements;
import br.com.gloomy.platform.exception.PlanLimitReachedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.br.food.models.Product;
import com.br.food.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

	@Mock
	private ProductRepository productRepository;

	@Mock
	private DocumentService documentService;

	@Mock
	private Entitlements entitlements;

	@InjectMocks
	private ProductService productService;

	@Test
	void createShouldStopAtThePlanLimitBeforeTouchingTheCatalog() throws java.io.IOException {
		when(productRepository.count()).thenReturn(60L);
		org.mockito.Mockito.doThrow(new PlanLimitReachedException("products.max", 60, 60)).when(entitlements)
				.requireBelow("products.max", 60L);

		assertThrows(PlanLimitReachedException.class, () -> productService.create(null, null));

		org.mockito.Mockito.verify(productRepository, org.mockito.Mockito.never()).save(org.mockito.ArgumentMatchers.any());
	}

	@Test
	void addComplementShouldLinkProvidedComplementId() {
		Product product = new Product();
		Product complement = new Product();
		complement.setComplement(true);
		complement.setActive(true);

		when(productRepository.findById(1L)).thenReturn(Optional.of(product));
		when(productRepository.findById(2L)).thenReturn(Optional.of(complement));

		productService.addComplement(1L, 2L);

		assertEquals(1, product.getComplements().size());
		assertEquals(complement, product.getComplements().get(0));
	}

	@Test
	void addComplementShouldRejectProductWithoutComplementFlag() {
		Product product = new Product();
		Product complement = new Product();
		complement.setComplement(false);

		when(productRepository.findById(1L)).thenReturn(Optional.of(product));
		when(productRepository.findById(3L)).thenReturn(Optional.of(complement));

		assertThrows(org.springframework.dao.DataIntegrityViolationException.class,
				() -> productService.addComplement(1L, 3L));
	}
}

