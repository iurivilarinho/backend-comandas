package com.br.food.authentication;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.br.food.authentication.token.TokenService;
import com.br.food.models.User;
import com.br.food.service.UserService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Recognises the logged-in user from the JWT (cookie {@code token} or {@code Authorization: Bearer}) and puts it in
 * the security context. It never rejects a request: which routes need a user is decided only by
 * {@link SecurityConfigurations} (CMD-015), so there is no second list of public routes to keep in step.
 */
@Component
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
		String jwtToken = RequestTokenResolver.resolve(request);

		if (jwtToken != null && tokenService.isTokenValid(jwtToken)) {
			try {
				User user = userService.findById(Long.parseLong(tokenService.getSubject(jwtToken)));
				if (Boolean.TRUE.equals(user.getActive())) {
					SecurityContextHolder.getContext().setAuthentication(
							new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
				}
			} catch (EntityNotFoundException unknownUser) {
				// the user was removed after the token was issued: the request goes on as anonymous
			}
		}

		filterChain.doFilter(request, response);
	}
}
