package com.br.food.authentication;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfigurations {

	private final SecurityFilter securityFilter;

	public SecurityConfigurations(SecurityFilter securityFilter) {
		this.securityFilter = securityFilter;
	}

	@SuppressWarnings("removal")
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		return http.csrf().disable().cors().and().sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().authorizeHttpRequests()
				.requestMatchers(HttpMethod.POST, "/auth/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/products", "/products/*", "/product-categories",
						"/promotions", "/promotions/*", "/company-profile", "/tables", "/tables/*",
						"/menu/products", "/documents/*", "/documents/*/content",
						"/customers/by-document", "/customers/*", "/orders", "/orders/*",
						"/push/public-key").permitAll()
				.requestMatchers(HttpMethod.POST, "/customers", "/digital-orders", "/digital-orders/*/items",
						"/orders/*/request-close", "/push/subscriptions").permitAll()
				.requestMatchers(HttpMethod.DELETE, "/push/subscriptions").permitAll()
				.requestMatchers(HttpMethod.PUT, "/customers/*").permitAll()
				.requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll().anyRequest()
				.authenticated().and().addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
				.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

}

