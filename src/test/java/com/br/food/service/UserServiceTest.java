package com.br.food.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.br.food.authentication.models.Role;
import com.br.food.config.PlanFeatures;
import com.br.food.models.User;
import com.br.food.repository.UserRepository;

import br.com.gloomy.platform.entitlements.Entitlements;
import br.com.gloomy.platform.exception.PlanLimitReachedException;
import jakarta.persistence.EntityManager;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private DocumentService documentService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private EntityManager entityManager;

	@Mock
	private Entitlements entitlements;

	private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	private UserService service() {
		return new UserService(documentService, userRepository, entityManager, passwordEncoder, entitlements);
	}

	@Test
	void resetPasswordStoresTheHashAndNeverThePlainPassword() {
		User user = new User();

		service().resetPassword(user, "New@Password1");

		assertThat(user.getPassword()).isNotEqualTo("New@Password1");
		assertThat(passwordEncoder.matches("New@Password1", user.getPassword())).isTrue();
		verify(userRepository).save(user);
	}

	@Test
	void anInvitedUserHasAnUnguessablePasswordAndTheGivenRole() {
		Role role = new Role();
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		User invited = service().createInvited("Maria", "maria@example.com", role);

		assertThat(invited.getLogin()).isEqualTo("maria@example.com");
		assertThat(invited.getRoles()).containsExactly(role);
		assertThat(invited.getPassword()).startsWith("$2");
		assertThat(passwordEncoder.matches("maria@example.com", invited.getPassword())).isFalse();
	}

	@Test
	void creatingAUserStopsAtThePlanLimit() {
		when(userRepository.count()).thenReturn(3L);
		doThrow(new PlanLimitReachedException(PlanFeatures.USERS_MAX, 3, 3)).when(entitlements)
				.requireBelow(PlanFeatures.USERS_MAX, 3L);

		assertThatThrownBy(() -> service().create(null, null)).isInstanceOf(PlanLimitReachedException.class);

		ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
		verify(userRepository, never()).save(saved.capture());
	}
}
