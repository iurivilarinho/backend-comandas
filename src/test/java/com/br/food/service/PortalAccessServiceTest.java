package com.br.food.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.br.food.enums.Types.StaffRole;
import com.br.food.models.User;
import com.br.food.platform.TestUsers;
import com.br.food.platform.TestTenants;

import br.com.gloomy.platform.tenant.TenantContext;
import jakarta.persistence.EntityNotFoundException;

class PortalAccessServiceTest {

	private static final String SECRET = "test-only-jwt-secret-not-for-production-0123456789";

	private final CustomerAccessTokenService tokens = new CustomerAccessTokenService(SECRET);
	private final PortalAccessService portal = new PortalAccessService(tokens);
	private TenantContext.Scope scope;

	@BeforeEach
	void setUp() {
		scope = TenantContext.bind(TestTenants.tenant("zil"));
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
		scope.close();
	}

	@Test
	void theCustomerPassesOnlyForTheOwnRegistration() {
		String token = tokens.generate(5L);

		assertThatCode(() -> portal.requireStaffOrCustomer(token, 5L)).doesNotThrowAnyException();
		assertThatThrownBy(() -> portal.requireStaffOrCustomer(token, 6L)).isInstanceOf(AccessDeniedException.class);
		assertThatThrownBy(() -> portal.requireStaffOrCustomer(token, null)).isInstanceOf(AccessDeniedException.class);
		assertThatThrownBy(() -> portal.requireStaffOrCustomer(null, 5L)).isInstanceOf(AccessDeniedException.class);
		assertThatThrownBy(() -> portal.requireStaffOrCustomer("forged", 5L)).isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void theStaffPassesWithoutAToken() {
		loginAs(StaffRole.CASHIER);

		assertThatCode(() -> portal.requireStaffOrCustomer(null, 5L)).doesNotThrowAnyException();
		assertThatCode(() -> portal.requireStaffOrCustomer(null, null)).doesNotThrowAnyException();
		assertThatCode(portal::requireStaff).doesNotThrowAnyException();
	}

	@Test
	void theKitchenIsStaffButNotFrontOfHouse() {
		loginAs(StaffRole.KITCHEN);

		assertThat(portal.isStaff()).isTrue();
		assertThat(portal.isFrontOfHouse()).isFalse();
		assertThatCode(portal::requireStaff).doesNotThrowAnyException();
		assertThatThrownBy(() -> portal.requireStaffOrCustomer(null, 5L)).isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void everyOtherRoleIsFrontOfHouse() {
		for (StaffRole role : new StaffRole[] { StaffRole.ADMIN, StaffRole.MANAGER, StaffRole.CASHIER, StaffRole.WAITER }) {
			loginAs(role);
			assertThat(portal.isFrontOfHouse()).as(role.name()).isTrue();
		}
	}

	@Test
	void anonymousIsNotStaff() {
		assertThat(portal.isStaff()).isFalse();
		assertThatThrownBy(portal::requireStaff).isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void aTokenOfAnotherTenantIsRefused() {
		String token = tokens.generate(5L);

		try (TenantContext.Scope other = TenantContext.bind(TestTenants.tenant("jantinha"))) {
			assertThatThrownBy(() -> portal.requireStaffOrCustomer(token, 5L)).isInstanceOf(AccessDeniedException.class);
		}
	}

	@Test
	void theListingFilterIsForcedToTheCustomerAndFreeForTheStaff() {
		String token = tokens.generate(5L);

		assertThat(portal.resolveCustomerFilter(token, null)).isEqualTo(5L);
		assertThat(portal.resolveCustomerFilter(token, 5L)).isEqualTo(5L);
		assertThatThrownBy(() -> portal.resolveCustomerFilter(token, 6L)).isInstanceOf(AccessDeniedException.class);
		assertThatThrownBy(() -> portal.resolveCustomerFilter(null, null)).isInstanceOf(AccessDeniedException.class);

		loginAs(StaffRole.CASHIER);
		assertThat(portal.resolveCustomerFilter(null, null)).isNull();
		assertThat(portal.resolveCustomerFilter(null, 9L)).isEqualTo(9L);
	}

	@Test
	void aMissingRecordAnswersLikeAForeignOneAndTheTokenIsCheckedFirst() {
		String token = tokens.generate(5L);

		assertThat(portal.loadOwned(token, () -> "order", order -> 5L)).isEqualTo("order");
		assertThatThrownBy(() -> portal.loadOwned(token, () -> "order", order -> 6L))
				.isInstanceOf(AccessDeniedException.class);
		assertThatThrownBy(() -> portal.loadOwned(token, () -> {
			throw new EntityNotFoundException("no such order");
		}, order -> 5L)).isInstanceOf(AccessDeniedException.class);
		assertThatThrownBy(() -> portal.loadOwned(null, () -> {
			throw new EntityNotFoundException("no such order");
		}, order -> 5L)).isInstanceOf(AccessDeniedException.class);
	}

	private void loginAs(StaffRole role) {
		User user = TestUsers.with(role);
		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
	}
}
