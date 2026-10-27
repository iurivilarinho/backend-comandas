package com.br.food.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.br.food.models.User;
import com.br.food.service.DocumentService;
import com.br.food.service.UserService;

import br.com.gloomy.platform.spi.GoogleIdentity;

@ExtendWith(MockitoExtension.class)
class GoogleAuthenticationServiceTest {

	@Mock
	private UserService userService;

	@Mock
	private DocumentService documentService;

	@InjectMocks
	private GoogleAuthenticationService service;

	private final GoogleIdentity identity = new GoogleIdentity("sub-1", "waiter@example.com", "Waiter", null);

	@Test
	void anAccountAlreadyLinkedEntersAsThatUser() {
		User linked = activeUser();
		when(userService.findOptionalByGoogleSubject("sub-1")).thenReturn(Optional.of(linked));
		when(userService.save(linked)).thenReturn(linked);

		assertThat(service.authenticate(identity)).isSameAs(linked);
	}

	@Test
	void anActiveUserWithTheSameEmailGetsLinkedToTheAccount() {
		User registered = activeUser();
		when(userService.findOptionalByGoogleSubject("sub-1")).thenReturn(Optional.empty());
		when(userService.findOptionalByEmail("waiter@example.com")).thenReturn(Optional.of(registered));
		when(userService.save(registered)).thenReturn(registered);

		User result = service.authenticate(identity);

		assertThat(result.getGoogleSubject()).isEqualTo("sub-1");
	}

	@Test
	void anUnknownEmailNeverBecomesAUser() {
		when(userService.findOptionalByGoogleSubject("sub-1")).thenReturn(Optional.empty());
		when(userService.findOptionalByEmail("waiter@example.com")).thenReturn(Optional.empty());
		when(userService.findOptionalByLogin("waiter@example.com")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.authenticate(identity)).isInstanceOf(AccessDeniedException.class)
				.hasMessageContaining("não está cadastrada");

		verify(userService, never()).save(any());
	}

	@Test
	void anInactiveUserIsRefused() {
		User inactive = activeUser();
		inactive.setActive(false);
		when(userService.findOptionalByGoogleSubject("sub-1")).thenReturn(Optional.of(inactive));
		when(userService.save(inactive)).thenReturn(inactive);

		assertThatThrownBy(() -> service.authenticate(identity)).isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void aUserLinkedToAnotherGoogleAccountIsRefused() {
		User registered = activeUser();
		registered.setGoogleSubject("another-sub");
		when(userService.findOptionalByGoogleSubject("sub-1")).thenReturn(Optional.empty());
		when(userService.findOptionalByEmail("waiter@example.com")).thenReturn(Optional.of(registered));

		assertThatThrownBy(() -> service.authenticate(identity)).isInstanceOf(AccessDeniedException.class);
	}

	private User activeUser() {
		User user = new User();
		user.setActive(true);
		user.setEmail("waiter@example.com");
		return user;
	}
}
