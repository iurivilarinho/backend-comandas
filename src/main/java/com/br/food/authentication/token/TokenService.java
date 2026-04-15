package com.br.food.authentication.token;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.br.food.authentication.models.Role;
import com.br.food.models.User;
import com.br.food.repository.UserRepository;

import io.swagger.v3.oas.annotations.media.Schema;

@Service
@Schema(description = "Service responsible for generating and validating JWT tokens (authentication and password recovery).")
public class TokenService {

	@Value("${api.security.token.secret}")
	private String secret;

	@Value("${api.jwt.expiration}")
	@Schema(description = "Access token expiration time in days (configured property).")
	private String expiration;

	@Value("${api.jwt.expiration.recovery.password}")
	@Schema(description = "Password recovery token expiration time in hours (configured property).")
	private String expirationRecoveryPassword;

	private final UserRepository userRepository;

	public TokenService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Schema(description = "Generates an access JWT for the given user.")
	public String generateToken(User user) {
		try {
			List<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toList());

			Algorithm algorithm = Algorithm.HMAC256(secret);

			return JWT.create().withIssuer("API Authentication").withSubject(user.getId().toString())
					.withClaim("roles", roles).withExpiresAt(accessTokenExpiration()).sign(algorithm);
		} catch (JWTCreationException exception) {
			throw new RuntimeException("Error generating JWT token.", exception);
		}
	}

	@Schema(description = "Generates a password recovery JWT for the given user.")
	public String generatePasswordRecoveryToken(User user) {
		try {
			Algorithm algorithm = Algorithm.HMAC256(secret);

			return JWT.create().withIssuer("API Authentication").withSubject(user.getId().toString())
					.withExpiresAt(passwordRecoveryTokenExpiration()).sign(algorithm);
		} catch (JWTCreationException exception) {
			throw new RuntimeException("Error generating JWT token.", exception);
		}
	}

	@Schema(description = "Returns the subject (user id) from the JWT.")
	public String getSubject(String jwtToken) {
		Algorithm algorithm = Algorithm.HMAC256(secret);
		return JWT.require(algorithm).withIssuer("API Authentication").build().verify(jwtToken).getSubject();
	}

	@Schema(description = "Validates a JWT token signature, issuer and expiration.")
	public boolean isTokenValid(String jwtToken) {
		try {
			Algorithm algorithm = Algorithm.HMAC256(secret);
			JWTVerifier verifier = JWT.require(algorithm).withIssuer("API Authentication").build();
			verifier.verify(jwtToken);
			return true;
		} catch (JWTVerificationException e) {
			return false;
		}
	}

	@Schema(description = "Loads a user by subject (user id) contained in the JWT.")
	public User getUserFromToken(String jwtToken) {
		try {
			Algorithm algorithm = Algorithm.HMAC256(secret);
			JWTVerifier verifier = JWT.require(algorithm).withIssuer("API Authentication").build();
			DecodedJWT decodedJWT = verifier.verify(jwtToken);

			String id = decodedJWT.getSubject();
			return userRepository.findById(Long.parseLong(id)).orElseThrow();
		} catch (JWTDecodeException e) {
			throw new DataIntegrityViolationException("Invalid token.");
		}
	}

	private Instant accessTokenExpiration() {
		return LocalDateTime.now().plusDays(Integer.parseInt(expiration)).toInstant(ZoneOffset.of("-03:00"));
	}

	private Instant passwordRecoveryTokenExpiration() {
		return LocalDateTime.now().plusHours(Integer.parseInt(expirationRecoveryPassword))
				.toInstant(ZoneOffset.of("-03:00"));
	}
}

