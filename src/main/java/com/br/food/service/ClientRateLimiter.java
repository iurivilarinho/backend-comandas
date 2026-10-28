package com.br.food.service;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.br.food.authentication.token.TenantClaim;
import com.br.food.config.TooManyRequestsException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Fixed window limit of attempts per client address and tenant (CMD-017), for the public routes that answer yes or no
 * about private data, such as {@code POST /customers/identify}. The window opens at the first attempt and the counters
 * live in memory, which is enough for one container.
 */
@Service
public class ClientRateLimiter {

	private final int maxAttempts;
	private final Duration window;
	private final Cache<String, AtomicInteger> attempts;

	public ClientRateLimiter(@Value("${comandas.rate-limit.identify.per-minute:10}") int maxAttempts) {
		this.maxAttempts = maxAttempts;
		this.window = Duration.ofMinutes(1);
		this.attempts = Caffeine.newBuilder().expireAfterWrite(window).maximumSize(50_000).build();
	}

	/** Counts one attempt of the caller and refuses it when the window already holds the maximum. */
	public void check(String route, HttpServletRequest request) {
		String key = route + "|" + TenantClaim.current() + "|" + clientAddress(request);
		int used = attempts.get(key, ignored -> new AtomicInteger()).incrementAndGet();
		if (used > maxAttempts) {
			throw new TooManyRequestsException("Muitas tentativas. Aguarde um minuto e tente de novo.",
					window.toSeconds());
		}
	}

	/**
	 * The caller address. The container only listens behind nginx, which puts the real address in
	 * {@code X-Forwarded-For}; that header is believed only when the connection itself comes from the same machine.
	 */
	static String clientAddress(HttpServletRequest request) {
		String remote = request.getRemoteAddr();
		boolean fromProxy = "127.0.0.1".equals(remote) || "0:0:0:0:0:0:0:1".equals(remote) || "::1".equals(remote);
		String forwarded = request.getHeader("X-Forwarded-For");
		if (fromProxy && forwarded != null && !forwarded.isBlank()) {
			return forwarded.split(",")[0].trim();
		}
		return remote;
	}
}
