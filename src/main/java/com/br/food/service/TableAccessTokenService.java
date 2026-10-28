package com.br.food.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.br.food.authentication.token.TableAccessClaims;
import com.br.food.authentication.token.TenantClaim;

/**
 * Signs and reads the token printed in the QR code of a table (CMD-015). The token has no expiration, because the QR
 * is printed and stuck to the table; instead it carries the version of the table's QR, so regenerating the QR makes
 * every token printed before it worthless. It also carries the tenant, since all tenants share the signing secret.
 */
@Service
public class TableAccessTokenService {

	private static final String ISSUER = "comandas-table-access";
	private static final String SUBJECT = "table-access";
	private static final String TABLE_CLAIM = "tableNumber";
	private static final String VERSION_CLAIM = "version";

	private final Algorithm algorithm;

	public TableAccessTokenService(@Value("${api.security.token.secret}") String secret) {
		this.algorithm = Algorithm.HMAC256(secret);
	}

	public String generate(String tableNumber, int version) {
		return JWT.create().withIssuer(ISSUER).withSubject(SUBJECT).withClaim(TABLE_CLAIM, tableNumber)
				.withClaim(VERSION_CLAIM, version).withClaim(TenantClaim.NAME, TenantClaim.current()).sign(algorithm);
	}

	public TableAccessClaims resolve(String token) {
		if (token == null || token.isBlank()) {
			throw new AccessDeniedException("Token de acesso da mesa ausente.");
		}
		try {
			DecodedJWT decoded = JWT.require(algorithm).withIssuer(ISSUER).withSubject(SUBJECT).build().verify(token);
			TenantClaim.requireCurrent(decoded);
			String tableNumber = decoded.getClaim(TABLE_CLAIM).asString();
			Integer version = decoded.getClaim(VERSION_CLAIM).asInt();
			if (tableNumber == null || version == null) {
				throw new AccessDeniedException("Token de acesso da mesa inválido.");
			}
			return new TableAccessClaims(tableNumber, version);
		} catch (JWTVerificationException exception) {
			throw new AccessDeniedException("Token de acesso da mesa inválido.");
		}
	}
}
