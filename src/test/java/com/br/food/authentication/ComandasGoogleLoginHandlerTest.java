package com.br.food.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import com.br.food.models.User;

import br.com.gloomy.platform.spi.GoogleIdentity;
import jakarta.servlet.http.HttpServletRequest;

class ComandasGoogleLoginHandlerTest {

	@Test
	void answersLikeThePasswordSignInForTheUserTheServiceResolved() {
		GoogleAuthenticationService service = mock(GoogleAuthenticationService.class);
		AuthenticatedSessionResponder responder = mock(AuthenticatedSessionResponder.class);
		HttpServletRequest request = mock(HttpServletRequest.class);
		GoogleIdentity identity = new GoogleIdentity("sub-1", "waiter@example.com", "Waiter", null);
		User user = new User();
		ResponseEntity<com.br.food.authentication.response.UserProfileResponse> signedIn = ResponseEntity.ok().build();
		when(service.authenticate(identity)).thenReturn(user);
		when(responder.respond(user, request)).thenReturn(signedIn);

		ResponseEntity<?> response = new ComandasGoogleLoginHandler(service, responder).login(identity, request);

		assertThat(response).isSameAs(signedIn);
	}
}
