package com.br.food.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.br.food.authentication.token.TenantClaim;

/**
 * Signs and reads the token that proves the customer of the digital menu owns a registration (CMD-015). The portal
 * has no login: the token comes back when the customer registers or identifies with phone and CPF, and travels in the
 * {@code X-Customer-Token} header. It is bound to the tenant because all tenants share the signing secret.
 */
@Service
public class CustomerAccessTokenService {

	private static final String ISSUER = "comandas-customer";
	private static final long VALIDITY_DAYS = 180;

	private final Algorithm algorithm;

	public CustomerAccessTokenService(@Value("${api.security.token.secret}") String secret) {
		this.algorithm = Algorithm.HMAC256(secret);
	}

	public String generate(Long customerId) {
		return JWT.create().withIssuer(ISSUER).withSubject(customerId.toString())
				.withClaim(TenantClaim.NAME, TenantClaim.current())
				.withExpiresAt(Instant.now().plus(VALIDITY_DAYS, ChronoUnit.DAYS)).sign(algorithm);
	}

	/** Id of the customer the token belongs to; refuses a missing, forged, expired or foreign-tenant token. */
	public Long resolve(String token) {
		if (token == null || token.isBlank()) {
			throw new AccessDeniedException("Identifique-se para continuar.");
		}
		try {
			DecodedJWT decoded = JWT.require(algorithm).withIssuer(ISSUER).build().verify(token);
			TenantClaim.requireCurrent(decoded);
			return Long.valueOf(decoded.getSubject());
		} catch (JWTVerificationException | NumberFormatException exception) {
			throw new AccessDeniedException("Identificacao do cliente invalida ou expirada.");
		}
	}
}
