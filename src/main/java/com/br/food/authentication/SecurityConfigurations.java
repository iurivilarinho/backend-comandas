package com.br.food.authentication;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * The only place that decides which routes are public (CMD-015). The {@link SecurityFilter} just recognises the user
 * when a valid token is present; it never rejects. A route not listed here needs a logged-in user.
 *
 * <p>
 * Routes marked "guarded" are open to the filter chain because the customer portal calls them without a staff login,
 * but each one checks in its controller that the caller is the staff or holds the customer token of the data owner
 * ({@code PortalAccessService}).
 * </p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfigurations {

	private final SecurityFilter securityFilter;

	public SecurityConfigurations(SecurityFilter securityFilter) {
		this.securityFilter = securityFilter;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http.csrf(AbstractHttpConfigurer::disable).cors(Customizer.withDefaults())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(handling -> handling.authenticationEntryPoint(unauthorizedEntryPoint()))
				.authorizeHttpRequests(authorize -> authorize
						// sign-in and password recovery
						.requestMatchers(HttpMethod.POST, "/auth/**").permitAll()
						// public menu
						.requestMatchers(HttpMethod.GET, "/products", "/product-categories", "/promotions",
								"/company-profile", "/menu/products", "/push/public-key",
								"/tables/resolve-access-token")
						.permitAll()
						.requestMatchers(numeric(HttpMethod.GET, "/products"), numeric(HttpMethod.GET, "/documents"),
								new RegexRequestMatcher("^/documents/\\d+/content$", "GET"))
						.permitAll()
						// customer portal sign-up and identification
						.requestMatchers(HttpMethod.POST, "/customers", "/customers/identify").permitAll()
						// guarded in the controllers by PortalAccessService
						.requestMatchers(numeric(HttpMethod.GET, "/customers"), numeric(HttpMethod.PUT, "/customers"),
								numeric(HttpMethod.GET, "/orders"),
								new RegexRequestMatcher("^/orders/\\d+/request-close$", "POST"),
								new RegexRequestMatcher("^/digital-orders/\\d+/items$", "POST"))
						.permitAll()
						.requestMatchers(HttpMethod.GET, "/orders").permitAll()
						.requestMatchers(HttpMethod.POST, "/digital-orders", "/push/subscriptions").permitAll()
						.requestMatchers(HttpMethod.DELETE, "/push/subscriptions", "/push/subscriptions/topic")
						.permitAll()
						// API documentation
						.requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**", "/docs", "/docs/**")
						.permitAll().anyRequest().authenticated())
				.addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class).build();
	}

	/**
	 * The filter belongs to the security chain only. Without this, Spring Boot also registers it as a plain servlet
	 * filter, where it would act on the routes of the hosting platform (/platform/**), which have their own chain.
	 */
	@Bean
	public FilterRegistrationBean<SecurityFilter> securityFilterRegistration(SecurityFilter filter) {
		FilterRegistrationBean<SecurityFilter> registration = new FilterRegistrationBean<>(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	private static RequestMatcher numeric(HttpMethod method, String prefix) {
		return new RegexRequestMatcher("^" + prefix + "/\\d+$", method.name());
	}

	private AuthenticationEntryPoint unauthorizedEntryPoint() {
		return (request, response, exception) -> {
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
			response.setContentType("application/json;charset=UTF-8");
			try {
				response.getWriter().write("{ \"timestamp\": \"" + LocalDateTime.now() + "\", "
						+ "\"message\": [\"Invalid or expired token. Please login again.\"] }");
				response.getWriter().flush();
			} catch (IOException ioException) {
				throw new IllegalStateException(ioException);
			}
		};
	}
}
