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

		if ((request.getRequestURI().startsWith("/auth") && method == HttpMethod.POST)
				|| request.getRequestURI().startsWith("/swagger")
				|| request.getRequestURI().startsWith("/v3")) {

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

