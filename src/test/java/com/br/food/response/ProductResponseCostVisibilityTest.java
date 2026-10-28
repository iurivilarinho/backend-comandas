package com.br.food.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.br.food.enums.Types.StaffRole;
import com.br.food.models.Product;
import com.br.food.models.User;
import com.br.food.platform.TestUsers;

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
	void costIsShownToAdministratorAndManagerEvenWhenTheObjectWasBuiltBeforeTheLogin() {
		ProductResponse response = new ProductResponse(productWithCost());

		for (StaffRole role : new StaffRole[] { StaffRole.ADMIN, StaffRole.MANAGER }) {
			login(role);
			assertThat(response.getCostPrice()).as(role.name()).isEqualByComparingTo("9.00");
		}
	}

	@Test
	void costIsHiddenFromCashierWaiterAndKitchen() {
		ProductResponse response = new ProductResponse(productWithCost());

		for (StaffRole role : new StaffRole[] { StaffRole.CASHIER, StaffRole.WAITER, StaffRole.KITCHEN }) {
			login(role);
			assertThat(response.getCostPrice()).as(role.name()).isNull();
		}
	}

	private void login(StaffRole role) {
		User user = TestUsers.with(role);
		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
	}

	private Product productWithCost() {
		Product product = new Product();
		product.setPrice(new BigDecimal("25.00"));
		product.setCostPrice(new BigDecimal("9.00"));
		return product;
	}
}
