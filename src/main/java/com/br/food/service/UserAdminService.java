package com.br.food.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.authentication.PasswordRecoveryService;
import com.br.food.authentication.models.Role;
import com.br.food.authentication.service.RoleService;
import com.br.food.config.PlanFeatures;
import com.br.food.enums.Types.StaffRole;
import com.br.food.models.User;
import com.br.food.repository.UserRepository;
import com.br.food.response.UserAdminResponse;
import com.br.food.specification.UserSpecification;

import br.com.gloomy.platform.entitlements.Entitlements;

/**
 * CMD-017. The users screen: list, invite with a role, change the role, enable or disable and send the password link.
 * The plan limit {@code users.max} counts every user; the restaurant never ends up without an active administrator.
 */
@Service
public class UserAdminService {

	private final UserRepository userRepository;
	private final UserService userService;
	private final RoleService roleService;
	private final PasswordRecoveryService passwordRecoveryService;
	private final Entitlements entitlements;

	public UserAdminService(UserRepository userRepository, UserService userService, RoleService roleService,
			PasswordRecoveryService passwordRecoveryService, Entitlements entitlements) {
		this.userRepository = userRepository;
		this.userService = userService;
		this.roleService = roleService;
		this.passwordRecoveryService = passwordRecoveryService;
		this.entitlements = entitlements;
	}

	@Transactional(readOnly = true)
	public Page<UserAdminResponse> search(String search, StaffRole role, Boolean active, Pageable pageable) {
		Long loggedUserId = loggedUserId();
		return userRepository.findAll(UserSpecification.search(search)
				.and(UserSpecification.hasRole(role == null ? null : role.authority()))
				.and(UserSpecification.active(active)), pageable)
				.map(user -> UserAdminResponse.from(user, loggedUserId));
	}

	@Transactional
	public UserAdminResponse invite(String name, String email, StaffRole role, String accessUrl) {
		String normalized = email.trim().toLowerCase();
		userService.emailValidate(normalized);
		userService.loginValidate(normalized);
		entitlements.requireBelow(PlanFeatures.USERS_MAX, userRepository.count());
		User user = userService.createInvited(name.trim(), normalized, roleService.findByName(role.authority()));
		passwordRecoveryService.sendInviteLink(user, accessUrl);
		return UserAdminResponse.from(user, loggedUserId());
	}

	@Transactional
	public UserAdminResponse changeRole(Long userId, StaffRole target) {
		User user = userService.findById(userId);
		if (user.getId().equals(loggedUserId())) {
			throw new DataIntegrityViolationException("Você não pode alterar o seu próprio papel.");
		}
		if (isAdmin(user) && target != StaffRole.ADMIN) {
			requireAnotherActiveAdmin(user);
		}
		Role role = roleService.findByName(target.authority());
		user.getRoles().clear();
		user.getRoles().add(role);
		return UserAdminResponse.from(userRepository.save(user), loggedUserId());
	}

	@Transactional
	public UserAdminResponse setActive(Long userId, boolean active) {
		User user = userService.findById(userId);
		if (!active) {
			if (user.getId().equals(loggedUserId())) {
				throw new DataIntegrityViolationException("Você não pode desativar o seu próprio acesso.");
			}
			if (isAdmin(user)) {
				requireAnotherActiveAdmin(user);
			}
		}
		user.setActive(active);
		return UserAdminResponse.from(userRepository.save(user), loggedUserId());
	}

	@Transactional
	public void sendPasswordLink(Long userId, String accessUrl) {
		User user = userService.findById(userId);
		if (user.getEmail() == null || user.getEmail().isBlank()) {
			throw new DataIntegrityViolationException("Este usuário não tem e-mail cadastrado.");
		}
		passwordRecoveryService.sendPasswordResetLink(user, accessUrl);
	}

	private static boolean isAdmin(User user) {
		return user.getRoles().stream().anyMatch(role -> StaffRole.ADMIN.authority().equals(role.getName()));
	}

	/** The restaurant never ends up without an active administrator: the user must not be the only one left. */
	private void requireAnotherActiveAdmin(User user) {
		boolean activeAdmin = !Boolean.FALSE.equals(user.getActive());
		if (activeAdmin && userRepository.countActiveByRoleName(StaffRole.ADMIN.authority()) <= 1) {
			throw new DataIntegrityViolationException(
					"Precisa haver ao menos um administrador ativo. Promova outro usuário antes.");
		}
	}

	private Long loggedUserId() {
		User logged = userService.findUserLoggedIn();
		return logged == null ? null : logged.getId();
	}
}
