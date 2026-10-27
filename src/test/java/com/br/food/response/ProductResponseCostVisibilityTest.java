package com.br.food.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.br.food.models.Product;
import com.br.food.models.User;

class ProductResponseCostVisibilityTest {

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void costIsOmittedForAnonymousVisitors() {
		ProductResponse response = new ProductResponse(productWithCost());

		assertThat(response.getCostPrice()).isNull();
		assertThat(response.getPrice()).isEqualByComparingTo("25.00");
	}

	@Test
	void costIsShownToTheStaffEvenWhenTheObjectWasBuiltBeforeTheLogin() {
		ProductResponse response = new ProductResponse(productWithCost());
		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken(new User(), null, List.of()));

		assertThat(response.getCostPrice()).isEqualByComparingTo("9.00");
	}

	private Product productWithCost() {
		Product product = new Product();
		product.setPrice(new BigDecimal("25.00"));
		product.setCostPrice(new BigDecimal("9.00"));
		return product;
	}
}
