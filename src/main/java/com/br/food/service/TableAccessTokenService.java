package com.br.food.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;

@Service
public class TableAccessTokenService {

	private final Algorithm algorithm;

	public TableAccessTokenService(@Value("${api.jwt.secret}") String secret) {
		this.algorithm = Algorithm.HMAC256(secret);
	}

	public String generate(String tableNumber) {
		return JWT.create().withIssuer("food-table-access").withSubject("table-access")
				.withClaim("tableNumber", tableNumber).sign(algorithm);
	}

	public String resolve(String token) {
		try {
			return JWT.require(algorithm).withIssuer("food-table-access").withSubject("table-access").build()
					.verify(token).getClaim("tableNumber").asString();
		} catch (JWTVerificationException exception) {
			throw new DataIntegrityViolationException("Invalid table access token.");
		}
	}
}