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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import com.br.food.authentication.PasswordRecoveryService;
import com.br.food.authentication.models.Role;
import com.br.food.authentication.service.RoleService;
import com.br.food.config.PlanFeatures;
import com.br.food.enums.Types.StaffRole;
import com.br.food.models.User;
import com.br.food.repository.UserRepository;
import com.br.food.response.UserAdminResponse;

import br.com.gloomy.platform.entitlements.Entitlements;
import br.com.gloomy.platform.exception.PlanLimitReachedException;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private UserService userService;

	@Mock
	private RoleService roleService;

	@Mock
	private PasswordRecoveryService passwordRecoveryService;

	@Mock
	private Entitlements entitlements;

	private UserAdminService service() {
		return new UserAdminService(userRepository, userService, roleService, passwordRecoveryService, entitlements);
	}

	@Test
	void inviteCreatesTheUserWithTheRoleAndSendsTheLink() {
		Role waiter = role(StaffRole.WAITER);
		User created = user(5L, waiter);
		when(userRepository.count()).thenReturn(2L);
		when(roleService.findByName("ROLE_WAITER")).thenReturn(waiter);
		when(userService.createInvited("Maria", "maria@example.com", waiter)).thenReturn(created);

		UserAdminResponse response = service().invite(" Maria ", "Maria@Example.com", StaffRole.WAITER,
				"https://comandas.gloomy.com.br/zil");

		assertThat(response.role()).isEqualTo(StaffRole.WAITER);
		verify(entitlements).requireBelow(PlanFeatures.USERS_MAX, 2L);
		verify(passwordRecoveryService).sendInviteLink(created, "https://comandas.gloomy.com.br/zil");
	}

	@Test
	void inviteStopsAtThePlanLimitBeforeCreatingAnything() {
		when(userRepository.count()).thenReturn(3L);
		doThrow(new PlanLimitReachedException(PlanFeatures.USERS_MAX, 3, 3)).when(entitlements)
				.requireBelow(PlanFeatures.USERS_MAX, 3L);

		assertThatThrownBy(() -> service().invite("Maria", "maria@example.com", StaffRole.WAITER, "https://x"))
				.isInstanceOf(PlanLimitReachedException.class);

		verify(userService, never()).createInvited(any(), any(), any());
		verify(passwordRecoveryService, never()).sendInviteLink(any(), any());
	}

	@Test
	void changingTheRoleReplacesEveryOtherRole() {
		Role cashier = role(StaffRole.CASHIER);
		User target = user(7L, role(StaffRole.WAITER));
		when(userService.findUserLoggedIn()).thenReturn(user(1L, role(StaffRole.ADMIN)));
		when(userService.findById(7L)).thenReturn(target);
		when(roleService.findByName("ROLE_CASHIER")).thenReturn(cashier);
		when(userRepository.save(target)).thenReturn(target);

		UserAdminResponse response = service().changeRole(7L, StaffRole.CASHIER);

		assertThat(target.getRoles()).containsExactly(cashier);
		assertThat(response.role()).isEqualTo(StaffRole.CASHIER);
	}

	@Test
	void nobodyChangesTheOwnRole() {
		User self = user(1L, role(StaffRole.ADMIN));
		when(userService.findUserLoggedIn()).thenReturn(self);
		when(userService.findById(1L)).thenReturn(self);

		assertThatThrownBy(() -> service().changeRole(1L, StaffRole.WAITER))
				.isInstanceOf(DataIntegrityViolationException.class).hasMessageContaining("seu próprio papel");
	}

	@Test
	void theLastActiveAdministratorCannotBeDemotedOrDisabled() {
		User onlyAdmin = user(2L, role(StaffRole.ADMIN));
		onlyAdmin.setActive(true);
		when(userService.findUserLoggedIn()).thenReturn(user(1L, role(StaffRole.ADMIN)));
		when(userService.findById(2L)).thenReturn(onlyAdmin);
		when(userRepository.countActiveByRoleName("ROLE_ADMIN")).thenReturn(1L);

		assertThatThrownBy(() -> service().changeRole(2L, StaffRole.MANAGER))
				.isInstanceOf(DataIntegrityViolationException.class).hasMessageContaining("ao menos um administrador");
		assertThatThrownBy(() -> service().setActive(2L, false)).isInstanceOf(DataIntegrityViolationException.class)
				.hasMessageContaining("ao menos um administrador");
	}

	@Test
	void anAdministratorCanBeDemotedWhenAnotherOneIsActive() {
		User admin = user(2L, role(StaffRole.ADMIN));
		admin.setActive(true);
		Role manager = role(StaffRole.MANAGER);
		when(userService.findUserLoggedIn()).thenReturn(user(1L, role(StaffRole.ADMIN)));
		when(userService.findById(2L)).thenReturn(admin);
		when(userRepository.countActiveByRoleName("ROLE_ADMIN")).thenReturn(2L);
		when(roleService.findByName("ROLE_MANAGER")).thenReturn(manager);
		when(userRepository.save(admin)).thenReturn(admin);

		assertThat(service().changeRole(2L, StaffRole.MANAGER).role()).isEqualTo(StaffRole.MANAGER);
	}

	@Test
	void nobodyDisablesTheOwnAccess() {
		User self = user(1L, role(StaffRole.ADMIN));
		when(userService.findUserLoggedIn()).thenReturn(self);
		when(userService.findById(1L)).thenReturn(self);

		assertThatThrownBy(() -> service().setActive(1L, false)).isInstanceOf(DataIntegrityViolationException.class)
				.hasMessageContaining("seu próprio acesso");
	}

	@Test
	void theResponseShowsNoRoleForAUserWithoutAStaffRole() {
		User legacy = user(9L, new Role());

		assertThat(UserAdminResponse.from(legacy, 1L).role()).isNull();
	}

	private static Role role(StaffRole staffRole) {
		Role role = new Role();
		role.setName(staffRole.authority());
		return role;
	}

	private static User user(Long id, Role role) {
		User user = new User();
		ReflectionTestUtils.setField(user, "id", id);
		user.setName("User " + id);
		user.getRoles().add(role);
		return user;
	}
}
