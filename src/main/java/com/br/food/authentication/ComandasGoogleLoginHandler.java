package com.br.food.authentication;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.br.food.models.User;

import br.com.gloomy.platform.spi.GoogleIdentity;
import br.com.gloomy.platform.spi.GoogleLoginHandler;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Plugs the app into the platform Google sign-in (PLT-038): the platform runs the OAuth round trip and hands over the
 * verified Google account; this turns it into a registered user and answers exactly like the password sign-in.
 */
@Component
public class ComandasGoogleLoginHandler implements GoogleLoginHandler {

	private final GoogleAuthenticationService googleAuthenticationService;
	private final AuthenticatedSessionResponder sessionResponder;

	public ComandasGoogleLoginHandler(GoogleAuthenticationService googleAuthenticationService,
			AuthenticatedSessionResponder sessionResponder) {
		this.googleAuthenticationService = googleAuthenticationService;
		this.sessionResponder = sessionResponder;
	}

	@Override
	public ResponseEntity<?> login(GoogleIdentity identity, HttpServletRequest request) {
		User user = googleAuthenticationService.authenticate(identity);
		return sessionResponder.respond(user, request);
	}
}
