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
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.br.food.config.StaffAuthorities;

import jakarta.servlet.http.HttpServletResponse;

/**
 * The only place that decides which routes are public and which role reaches what (CMD-015, CMD-017). The
 * {@link SecurityFilter} just recognises the user when a valid token is present; it never rejects. A route not listed
 * here needs a user with any staff role. The groups of roles are in {@link StaffAuthorities}.
 *
 * <p>
 * Routes marked "guarded" are open to the filter chain because the customer portal calls them without a staff login,
 * but each one checks in its controller that the caller is front-of-house staff or holds the customer token of the data
 * owner ({@code PortalAccessService}).
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
				.exceptionHandling(handling -> handling.authenticationEntryPoint(unauthorizedEntryPoint())
						.accessDeniedHandler(forbiddenHandler()))
				.authorizeHttpRequests(authorize -> authorize
						// permissions and resources are administration, and must come before the public POST /auth/**
						.requestMatchers("/auth/permissions/**", "/auth/resources/**", "/role/**", "/users/**")
						.hasAnyAuthority(StaffAuthorities.ADMIN_ONLY)
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
						.permitAll()
						// the own profile: any signed-in user, even one without a role yet
						.requestMatchers(HttpMethod.GET, "/user/me").authenticated()

						// kitchen: the queue is read by the whole floor, the actions are the kitchen's
						.requestMatchers(HttpMethod.GET, "/kitchen/pending").hasAnyAuthority(StaffAuthorities.ALL_STAFF)
						.requestMatchers("/kitchen/**").hasAnyAuthority(StaffAuthorities.KITCHEN_UP)

						// orders: closing the bill and undoing it is the cashier's; the rest is front of house
						.requestMatchers(HttpMethod.POST, "/orders/*/close", "/orders/*/service-fee", "/orders/*/reopen")
						.hasAnyAuthority(StaffAuthorities.CASHIER_UP)
						.requestMatchers(HttpMethod.DELETE, "/orders/*").hasAnyAuthority(StaffAuthorities.CASHIER_UP)
						.requestMatchers("/orders/**", "/orders").hasAnyAuthority(StaffAuthorities.FRONT_OF_HOUSE)

						// tables: the floor reads them, the setup (and the QR codes) are management
						.requestMatchers(HttpMethod.GET, "/tables/access-token").hasAnyAuthority(StaffAuthorities.MANAGEMENT)
						.requestMatchers(HttpMethod.GET, "/tables", "/tables/*")
						.hasAnyAuthority(StaffAuthorities.FRONT_OF_HOUSE)
						.requestMatchers("/tables/**", "/tables").hasAnyAuthority(StaffAuthorities.MANAGEMENT)

						// customers: front of house looks up and registers, management blocks and deletes
						.requestMatchers(HttpMethod.GET, "/customers", "/customers/by-document", "/customers/by-phone")
						.hasAnyAuthority(StaffAuthorities.FRONT_OF_HOUSE)
						.requestMatchers("/customers/**", "/customers").hasAnyAuthority(StaffAuthorities.MANAGEMENT)

						// settings read by the checkout, written by management
						.requestMatchers(HttpMethod.GET, "/system-settings/**")
						.hasAnyAuthority(StaffAuthorities.FRONT_OF_HOUSE)

						// management: catalog, stock, invoices, financial, profitability, settings, diagnostics
						.requestMatchers("/products/**", "/products", "/product-categories/**", "/product-categories",
								"/promotions/**", "/promotions", "/company-profile", "/stock/**", "/stock",
								"/supply-invoices/**", "/supply-invoices", "/financial/**", "/product-profit/**",
								"/system-settings/**", "/push/**")
						.hasAnyAuthority(StaffAuthorities.MANAGEMENT)

						// anything else needs a staff role
						.anyRequest().hasAnyAuthority(StaffAuthorities.ALL_STAFF))
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
		return (request, response, exception) -> writeError(response, HttpStatus.UNAUTHORIZED,
				"Invalid or expired token. Please login again.");
	}

	/**
	 * A signed-in user whose role does not reach the route. Written here, without {@code sendError}: the error dispatch
	 * of the container runs the chain again without the user and would turn the 403 into a 401.
	 */
	private AccessDeniedHandler forbiddenHandler() {
		return (request, response, exception) -> writeError(response, HttpStatus.FORBIDDEN,
				"Seu perfil não tem acesso a este recurso.");
	}

	private static void writeError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
		response.setStatus(status.value());
		response.setContentType("application/json;charset=UTF-8");
		response.getWriter().write("{ \"timestamp\": \"" + LocalDateTime.now() + "\", \"message\": [\"" + message + "\"] }");
		response.getWriter().flush();
	}
}
