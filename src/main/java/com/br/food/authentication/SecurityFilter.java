package com.br.food.authentication;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.br.food.authentication.token.TokenService;
import com.br.food.service.UserService;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Schema(description = "Security filter that validates JWT (cookie or Authorization header) and sets the authenticated user in the security context.")
public class SecurityFilter extends OncePerRequestFilter {

	private final TokenService tokenService;
	private final UserService userService;

	public SecurityFilter(TokenService tokenService, UserService userService) {
		this.tokenService = tokenService;
		this.userService = userService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		HttpMethod method = HttpMethod.valueOf(request.getMethod());

		if (shouldBypassAuthentication(request, method)) {

			filterChain.doFilter(request, response);
			return;
		}

		String jwtToken = extractToken(request);

		if (jwtToken != null && tokenService.isTokenValid(jwtToken)) {
			String subject = tokenService.getSubject(jwtToken);
			var user = userService.findById(Long.parseLong(subject));

			if (user != null) {
				var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
				SecurityContextHolder.getContext().setAuthentication(authentication);
				filterChain.doFilter(request, response);
				return;
			}

			handleUserNotFound(response);
			return;
		}

		handleInvalidToken(response);
	}

	@Schema(description = "Returns JWT from cookie 'token' or Authorization header (Bearer).")
	private String extractToken(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();

		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if ("token".equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}

		String tokenHeader = request.getHeader("Authorization");
		if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
			return tokenHeader.substring(7);
		}

		return null;
	}

	private void handleUserNotFound(HttpServletResponse response) throws IOException {
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType("application/json;charset=UTF-8");

		try (PrintWriter writer = response.getWriter()) {
			writer.write("{ \"timestamp\": \"" + LocalDateTime.now() + "\", ");
			writer.write("\"message\": [\"User not found.\"] }");
			writer.flush();
		}
	}

	private boolean shouldBypassAuthentication(HttpServletRequest request, HttpMethod method) {
		String requestUri = request.getRequestURI();

		if ((requestUri.startsWith("/auth") && method == HttpMethod.POST)
				|| requestUri.startsWith("/swagger")
				|| requestUri.startsWith("/v3")) {
			return true;
		}

		if (method == HttpMethod.GET && (requestUri.equals("/products")
				|| requestUri.startsWith("/products/")
				|| requestUri.equals("/product-categories")
				|| requestUri.equals("/menu/products")
				|| requestUri.matches("^/documents/\\d+$")
				|| requestUri.matches("^/documents/\\d+/content$")
				|| requestUri.equals("/promotions")
				|| requestUri.startsWith("/promotions/")
				|| requestUri.equals("/company-profile")
				|| requestUri.equals("/tables")
				|| requestUri.startsWith("/tables/")
				|| requestUri.equals("/customers/by-document")
				|| requestUri.matches("^/customers/\\d+$")
				|| requestUri.equals("/orders")
				|| requestUri.matches("^/orders/\\d+$"))) {
			return true;
		}

		if (method == HttpMethod.POST && (requestUri.equals("/customers")
				|| requestUri.equals("/orders")
				|| requestUri.matches("^/orders/\\d+/items$")
				|| requestUri.matches("^/orders/\\d+/request-close$"))) {
			return true;
		}

		if (method == HttpMethod.PUT && requestUri.matches("^/customers/\\d+$")) {
			return true;
		}

		return false;
	}

	private void handleInvalidToken(HttpServletResponse response) throws IOException {
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType("application/json;charset=UTF-8");

		try (PrintWriter writer = response.getWriter()) {
			writer.write("{ \"timestamp\": \"" + LocalDateTime.now() + "\", ");
			writer.write("\"message\": [\"Invalid or expired token. Please login again.\"] }");
			writer.flush();
		}
	}
}

