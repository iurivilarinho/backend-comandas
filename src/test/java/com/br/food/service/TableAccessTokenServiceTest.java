package com.br.food.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.br.food.authentication.token.TableAccessClaims;
import com.br.food.platform.TestTenants;

import br.com.gloomy.platform.tenant.TenantContext;

class TableAccessTokenServiceTest {

	private static final String SECRET = "test-only-jwt-secret-not-for-production-0123456789";

	private final TableAccessTokenService service = new TableAccessTokenService(SECRET);
	private TenantContext.Scope scope;

	@BeforeEach
	void setUp() {
		scope = TenantContext.bind(TestTenants.tenant("zil"));
	}

	@AfterEach
	void tearDown() {
		scope.close();
	}

	@Test
	void theTokenCarriesTheTableAndTheVersionOfItsQr() {
		TableAccessClaims claims = service.resolve(service.generate("10", 3));

		assertThat(claims.tableNumber()).isEqualTo("10");
		assertThat(claims.version()).isEqualTo(3);
	}

	@Test
	void aTokenOfAnotherTenantIsRefused() {
		String token = service.generate("10", 1);

		try (TenantContext.Scope other = TenantContext.bind(TestTenants.tenant("jantinha"))) {
			assertThatThrownBy(() -> service.resolve(token)).isInstanceOf(AccessDeniedException.class);
		}
	}

	@Test
	void missingForgedAndStaffTokensAreRefused() {
		String staffToken = JWT.create().withIssuer("comandas").withSubject("1").withClaim("tenant", "zil")
				.sign(Algorithm.HMAC256(SECRET));

		assertThatThrownBy(() -> service.resolve(null)).isInstanceOf(AccessDeniedException.class);
		assertThatThrownBy(() -> service.resolve("forged")).isInstanceOf(AccessDeniedException.class);
		assertThatThrownBy(() -> service.resolve(staffToken)).isInstanceOf(AccessDeniedException.class);
	}
}
