package com.br.food.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.authentication.PasswordRecoveryService;
import com.br.food.authentication.models.Role;
import com.br.food.config.DataInitializer;
import com.br.food.models.User;
import com.br.food.repository.UserRepository;

import br.com.gloomy.platform.spi.TenantAdminProvisioner;
import br.com.gloomy.platform.spi.TenantProvisioningRequest;

/**
 * CMD-016. Called by the hosting platform with a brand new tenant bound to the thread: seeds its base data, creates the
 * administrator for the customer e-mail and sends the set-password link built on the password-recovery mechanism.
 * Idempotent: the platform may call it again after a failure.
 */
@Service
public class ComandasTenantAdminProvisioner implements TenantAdminProvisioner {

	private static final Logger log = LoggerFactory.getLogger(ComandasTenantAdminProvisioner.class);

	private final DataInitializer baseData;
	private final UserRepository userRepository;
	private final UserService userService;
	private final PasswordRecoveryService passwordRecoveryService;

	public ComandasTenantAdminProvisioner(DataInitializer baseData, UserRepository userRepository,
			UserService userService, PasswordRecoveryService passwordRecoveryService) {
		this.baseData = baseData;
		this.userRepository = userRepository;
		this.userService = userService;
		this.passwordRecoveryService = passwordRecoveryService;
	}

	@Override
	@Transactional
	public void provision(TenantProvisioningRequest request) {
		Role adminRole = baseData.seedCurrentTenant();

		User admin = userRepository.findByEmail(request.adminEmail())
				.orElseGet(() -> userService.createInvited(request.adminName(), request.adminEmail(), adminRole));
		if (admin.getRoles().stream().noneMatch(role -> role.getId().equals(adminRole.getId()))) {
			admin.getRoles().add(adminRole);
			admin = userRepository.save(admin);
		}

		try {
			passwordRecoveryService.sendSetPasswordLink(admin, request.accessUrl(), request.tenantName());
		} catch (RuntimeException exception) {
			// The tenant must exist even when the mail server is down: the administrator can use "forgot my password".
			log.warn("[tenant:{}] Nao foi possivel enviar o link de definir senha: {}", request.slug(),
					exception.getMessage());
		}
	}
}
