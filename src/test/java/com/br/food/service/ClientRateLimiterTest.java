package com.br.food.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.br.food.config.TooManyRequestsException;
import com.br.food.platform.TestTenants;

import br.com.gloomy.platform.tenant.TenantContext;

class ClientRateLimiterTest {

	private final ClientRateLimiter limiter = new ClientRateLimiter(3);
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
	void theAttemptsOverTheLimitInTheWindowAreRefusedWithTheTimeToWait() {
		MockHttpServletRequest request = from("203.0.113.5");

		for (int attempt = 0; attempt < 3; attempt++) {
			limiter.check("identify", request);
		}

		assertThatThrownBy(() -> limiter.check("identify", request)).isInstanceOfSatisfying(TooManyRequestsException.class,
				exception -> assertThat(exception.getRetryAfterSeconds()).isEqualTo(60));
	}

	@Test
	void eachAddressRouteAndTenantHasItsOwnCounter() {
		for (int attempt = 0; attempt < 3; attempt++) {
			limiter.check("identify", from("203.0.113.5"));
		}

		assertThatCode(() -> limiter.check("identify", from("203.0.113.6"))).doesNotThrowAnyException();
		assertThatCode(() -> limiter.check("another-route", from("203.0.113.5"))).doesNotThrowAnyException();
		try (TenantContext.Scope other = TenantContext.bind(TestTenants.tenant("jantinha"))) {
			assertThatCode(() -> limiter.check("identify", from("203.0.113.5"))).doesNotThrowAnyException();
		}
	}

	@Test
	void theForwardedAddressIsBelievedOnlyWhenTheConnectionComesFromTheProxyOnTheSameMachine() {
		MockHttpServletRequest behindNginx = from("127.0.0.1");
		behindNginx.addHeader("X-Forwarded-For", "198.51.100.7, 127.0.0.1");
		MockHttpServletRequest spoofed = from("203.0.113.9");
		spoofed.addHeader("X-Forwarded-For", "198.51.100.7");

		assertThat(ClientRateLimiter.clientAddress(behindNginx)).isEqualTo("198.51.100.7");
		assertThat(ClientRateLimiter.clientAddress(spoofed)).isEqualTo("203.0.113.9");
	}

	private static MockHttpServletRequest from(String address) {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRemoteAddr(address);
		return request;
	}
}
