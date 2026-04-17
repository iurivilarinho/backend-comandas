package com.br.food.config;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.br.food.authentication.models.Role;
import com.br.food.models.User;
import com.br.food.repository.RoleRepository;
import com.br.food.repository.UserRepository;
import com.br.food.service.PaymentService;
import com.br.food.service.SystemSettingService;

@Configuration
public class DataInitializer {

	@Bean
	CommandLineRunner initializeSeedData(PaymentService paymentService, SystemSettingService systemSettingService,
			RoleRepository roleRepository, UserRepository userRepository) {
		return args -> {
			paymentService.ensureDefaultMethods();
			systemSettingService.upsert(SystemSettingService.SERVICE_FEE_PERCENT, "10.00");
			systemSettingService.upsert(SystemSettingService.COVER_CHARGE_AMOUNT, "5.00");

			Role adminRole = roleRepository.findByDescription("ROLE_ADMIN");
			if (adminRole == null) {
				adminRole = new Role();
				adminRole.setDescription("Default administrator role");
				adminRole.setAuthority("ROLE_ADMIN");
				adminRole = roleRepository.save(adminRole);
			}

			Optional<User> adminUserOptional = userRepository.findByLogin("admin");
			if (adminUserOptional.isEmpty()) {

				User adminUser = new User();
				adminUser.setName("Administrator");
				adminUser.setLogin("admin");
				adminUser.setEmail("admin@restaurant.local");
				adminUser.setPassword("$2y$10$x7mhJQXxb941hVtAfVbKe.mpwYVe9CtuNJhmiqiRN2dH7R1Mn8Pz.");
				adminUser.getRoles().add(adminRole);
				adminUser.setPasswordLastChanged(LocalDateTime.now());
				adminUser.setForcePasswordChange(true);
				adminUser.setActive(null);
				userRepository.save(adminUser);
			}
		};
	}
}
