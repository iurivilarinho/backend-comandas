package com.br.food.config;

import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.authentication.models.Role;
import com.br.food.enums.Types.StaffRole;
import com.br.food.models.User;
import com.br.food.repository.RoleRepository;
import com.br.food.repository.UserRepository;
import com.br.food.service.PaymentService;
import com.br.food.service.SystemSettingService;

/**
 * Per-tenant base data: payment methods, service fee, cover charge, the administrator role and, when
 * {@code APP_ADMIN_PASSWORD} is set, the "admin" user of an own instance. {@link #seedCurrentTenant()} runs against the
 * tenant bound to the thread; {@link TenantSeeding} calls it for every tenant at startup and the tenant provisioner
 * calls it for a new tenant (CMD-016). The password is never in the code (CMD-015).
 */
@Component
public class DataInitializer {

	public static final String ROLE_ADMIN = StaffRole.ADMIN.authority();

	private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
	private static final String DEFAULT_ADMIN_LOGIN = "admin";

	private final PaymentService paymentService;
	private final SystemSettingService systemSettingService;
	private final RoleRepository roleRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final String initialAdminPassword;

	public DataInitializer(PaymentService paymentService, SystemSettingService systemSettingService,
			RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder,
			@Value("${comandas.admin.initial-password:}") String initialAdminPassword) {
		this.paymentService = paymentService;
		this.systemSettingService = systemSettingService;
		this.roleRepository = roleRepository;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.initialAdminPassword = initialAdminPassword;
	}

	/** Idempotent. Returns the administrator role of the current tenant. */
	@Transactional
	public Role seedCurrentTenant() {
		paymentService.ensureDefaultMethods();
		systemSettingService.createIfAbsent(SystemSettingService.SERVICE_FEE_PERCENT, "10.00");
		systemSettingService.createIfAbsent(SystemSettingService.COVER_CHARGE_AMOUNT, "0.00");

		Role adminRole = ensureRole(StaffRole.ADMIN);
		for (StaffRole staffRole : StaffRole.values()) {
			ensureRole(staffRole);
		}

		Optional<User> existingAdmin = userRepository.findByLogin(DEFAULT_ADMIN_LOGIN);
		if (existingAdmin.isEmpty() && initialAdminPassword.isBlank()) {
			log.debug("[init] Sem usuário admin e sem APP_ADMIN_PASSWORD: o administrador vem do provisionamento.");
		} else if (existingAdmin.isEmpty()) {
			User admin = new User();
			admin.setName("Administrador");
			admin.setLogin(DEFAULT_ADMIN_LOGIN);
			admin.setEmail("admin@comandas.local");
			admin.setPassword(passwordEncoder.encode(initialAdminPassword));
			admin.setPasswordLastChanged(LocalDateTime.now());
			admin.setForcePasswordChange(false);
			admin.setActive(Boolean.TRUE);
			admin.setRegistrationCompleted(Boolean.TRUE);
			admin.getRoles().add(adminRole);
			userRepository.save(admin);
			log.info("[init] Usuário admin inicial criado: login={} (senha vinda de APP_ADMIN_PASSWORD)",
					DEFAULT_ADMIN_LOGIN);
		}
		return adminRole;
	}

	private Role ensureRole(StaffRole staffRole) {
		Optional<Role> byName = roleRepository.findByNameIgnoreCase(staffRole.authority());
		if (byName.isPresent()) {
			return byName.get();
		}
		// Databases created before CMD-015 stored ROLE_ADMIN only in the description and left the name empty.
		Role legacy = staffRole == StaffRole.ADMIN ? roleRepository.findByDescription(ROLE_ADMIN) : null;
		Role role = legacy != null ? legacy : new Role();
		role.setName(staffRole.authority());
		role.setDescription(staffRole == StaffRole.ADMIN ? ROLE_ADMIN : staffRole.label());
		return roleRepository.save(role);
	}
}
