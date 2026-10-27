package com.br.food.authentication.token;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import br.com.gloomy.platform.tenant.TenantContext;

/**
 * Binds every token the application signs to the tenant that issued it. All tenants share one signing secret, so the
 * tenant claim is what stops a token of one restaurant from working on another (ids repeat between tenants).
 */
public final class TenantClaim {

	public static final String NAME = "tenant";

	private TenantClaim() {
	}

	/** Slug of the tenant bound to the current thread. */
	public static String current() {
		return TenantContext.require().slug();
	}

	/** Refuses a token that carries another tenant, or none. */
	public static void requireCurrent(DecodedJWT decoded) {
		String claim = decoded.getClaim(NAME).asString();
		if (claim == null || !claim.equals(current())) {
			throw new JWTVerificationException("The token was issued for another tenant.");
		}
	}
}
