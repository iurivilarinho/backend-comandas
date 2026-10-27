package com.br.food.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.br.food.authentication.models.Role;
import com.br.food.models.User;
import com.br.food.repository.RoleRepository;
import com.br.food.repository.UserRepository;
import com.br.food.service.PaymentService;
import com.br.food.service.SystemSettingService;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

	@Mock
	private PaymentService paymentService;

	@Mock
	private SystemSettingService systemSettingService;

	@Mock
	private RoleRepository roleRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void setUp() {
		when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));
	}

	@Test
	void withoutAnInitialPasswordNoAdminUserIsCreated() {
		DataInitializer initializer = new DataInitializer(paymentService, systemSettingService, roleRepository,
				userRepository, passwordEncoder, "");

		Role role = initializer.seedCurrentTenant();

		assertThat(role.getName()).isEqualTo("ROLE_ADMIN");
		verify(paymentService).ensureDefaultMethods();
		verify(userRepository, never()).save(any());
	}

	@Test
	void withAnInitialPasswordTheAdminIsCreatedWithItsHash() {
		when(passwordEncoder.encode("from-the-environment")).thenReturn("hash-of-the-password");
		when(userRepository.findByLogin("admin")).thenReturn(Optional.empty());
		DataInitializer initializer = new DataInitializer(paymentService, systemSettingService, roleRepository,
				userRepository, passwordEncoder, "from-the-environment");

		Role role = initializer.seedCurrentTenant();

		ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(saved.capture());
		assertThat(saved.getValue().getLogin()).isEqualTo("admin");
		assertThat(saved.getValue().getPassword()).isEqualTo("hash-of-the-password");
		assertThat(saved.getValue().getRoles()).containsExactly(role);
	}

	@Test
	void anExistingAdminIsLeftAlone() {
		when(userRepository.findByLogin("admin")).thenReturn(Optional.of(new User()));
		DataInitializer initializer = new DataInitializer(paymentService, systemSettingService, roleRepository,
				userRepository, passwordEncoder, "from-the-environment");

		initializer.seedCurrentTenant();

		verify(userRepository, never()).save(any());
	}

	@Test
	void theLegacyRoleWithoutANameGetsItsAuthority() {
		Role legacy = new Role();
		legacy.setDescription("ROLE_ADMIN");
		when(roleRepository.findByNameIgnoreCase("ROLE_ADMIN")).thenReturn(Optional.empty());
		when(roleRepository.findByDescription("ROLE_ADMIN")).thenReturn(legacy);
		DataInitializer initializer = new DataInitializer(paymentService, systemSettingService, roleRepository,
				userRepository, passwordEncoder, "");

		Role role = initializer.seedCurrentTenant();

		assertThat(role).isSameAs(legacy);
		assertThat(role.getAuthority()).isEqualTo("ROLE_ADMIN");
	}
}
