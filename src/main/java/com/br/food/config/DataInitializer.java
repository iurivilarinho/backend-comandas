package com.br.food.config;

import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.br.food.models.acesso.Role;
import com.br.food.models.acesso.User;
import com.br.food.repository.RoleRepository;
import com.br.food.repository.UserRepository;
import com.br.food.service.PaymentService;
import com.br.food.service.SystemSettingService;

@Configuration
public class DataInitializer {

	@Bean
	CommandLineRunner initializeSeedData(
			PaymentService paymentService,
			SystemSettingService systemSettingService,
			RoleRepository roleRepository,
			UserRepository userRepository) {
		return args -> {
			paymentService.ensureDefaultMethods();
			systemSettingService.upsert(SystemSettingService.SERVICE_FEE_PERCENT, "10.00");
			systemSettingService.upsert(SystemSettingService.COVER_CHARGE_AMOUNT, "5.00");

			Role adminRole = roleRepository.findByAuthority("ROLE_ADMIN");
			if (adminRole == null) {
				adminRole = new Role();
				adminRole.setDescription("Default administrator role");
				adminRole.setAuthority("ROLE_ADMIN");
				adminRole = roleRepository.save(adminRole);
			}

			User adminUser = userRepository.findByLogin("admin");
			if (adminUser == null) {
				adminUser = new User();
				adminUser.setName("Administrator");
				adminUser.setLogin("admin");
				adminUser.setEmail("admin@restaurant.local");
				adminUser.setPassword("admin123");
				adminUser.setRole(adminRole);
				adminUser.setPasswordLastChanged(LocalDateTime.now());
				adminUser.setForcePasswordChange(true);
				adminUser.setStatus(true);
				userRepository.save(adminUser);
			}
		};
	}
}
