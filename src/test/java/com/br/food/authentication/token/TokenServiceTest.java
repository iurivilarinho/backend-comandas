package com.br.food.authentication.token;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.Instant;
import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.br.food.models.User;
import com.br.food.platform.TestTenants;
import com.br.food.repository.UserRepository;

import br.com.gloomy.platform.tenant.TenantContext;

class TokenServiceTest {

	private static final String SECRET = "test-only-jwt-secret-not-for-production-0123456789";

	private TokenService tokenService;
	private TenantContext.Scope scope;

	@BeforeEach
	void setUp() {
		tokenService = new TokenService(mock(UserRepository.class));
		ReflectionTestUtils.setField(tokenService, "secret", SECRET);
		ReflectionTestUtils.setField(tokenService, "expiration", "7");
		ReflectionTestUtils.setField(tokenService, "expirationRecoveryPassword", "1");
		scope = TenantContext.bind(TestTenants.tenant("zil"));
	}

	@AfterEach
	void tearDown() {
		scope.close();
	}

	@Test
	void aTokenIssuedInATenantIsValidThereAndCarriesTheUserId() {
		String token = tokenService.generateToken(user(7L));

		assertThat(tokenService.isTokenValid(token)).isTrue();
		assertThat(tokenService.getSubject(token)).isEqualTo("7");
	}

	@Test
	void aTokenIssuedInATenantIsRefusedInAnother() {
		String token = tokenService.generateToken(user(7L));

		try (TenantContext.Scope other = TenantContext.bind(TestTenants.tenant("jantinha"))) {
			assertThat(tokenService.isTokenValid(token)).isFalse();
		}
	}

	@Test
	void tokensOfOtherSystemsThatShareTheSecretAreRefused() {
		Algorithm algorithm = Algorithm.HMAC256(SECRET);
		String otherIssuer = JWT.create().withIssuer("API Authentication").withSubject("1")
				.withClaim("tenant", "zil").sign(algorithm);
		String withoutTenant = JWT.create().withIssuer("comandas").withSubject("1").sign(algorithm);

		assertThat(tokenService.isTokenValid(otherIssuer)).isFalse();
		assertThat(tokenService.isTokenValid(withoutTenant)).isFalse();
	}

	@Test
	void anExpiredOrForgedTokenIsRefused() {
		String expired = JWT.create().withIssuer("comandas").withSubject("1").withClaim("tenant", "zil")
				.withExpiresAt(Date.from(Instant.now().minusSeconds(60))).sign(Algorithm.HMAC256(SECRET));
		String forged = JWT.create().withIssuer("comandas").withSubject("1").withClaim("tenant", "zil")
				.sign(Algorithm.HMAC256("another-secret-another-secret-another-secret"));

		assertThat(tokenService.isTokenValid(expired)).isFalse();
		assertThat(tokenService.isTokenValid(forged)).isFalse();
	}

	private User user(Long id) {
		User user = new User();
		ReflectionTestUtils.setField(user, "id", id);
		return user;
	}
}
