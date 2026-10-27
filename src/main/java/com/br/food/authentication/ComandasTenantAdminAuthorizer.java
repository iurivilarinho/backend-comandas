package com.br.food.authentication;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.authentication.token.TokenService;
import com.br.food.config.DataInitializer;
import com.br.food.models.User;
import com.br.food.service.UserService;

import br.com.gloomy.platform.model.TenantInfo;
import br.com.gloomy.platform.spi.TenantAdminAuthorizer;
import jakarta.servlet.http.HttpServletRequest;

/**
 * PLT-032. Tells the hosting platform who may manage the address and domains of the tenant: an active user with
 * ROLE_ADMIN whose access JWT (cookie "token" or Bearer header) is valid for this tenant. The platform binds the
 * tenant to the thread before calling, so the user is read from the tenant schema.
 */
@Component
public class ComandasTenantAdminAuthorizer implements TenantAdminAuthorizer {

	private final TokenService tokenService;
	private final UserService userService;

	public ComandasTenantAdminAuthorizer(TokenService tokenService, UserService userService) {
		this.tokenService = tokenService;
		this.userService = userService;
	}

	@Override
	@Transactional(readOnly = true)
	public boolean isTenantAdmin(HttpServletRequest request, TenantInfo tenant) {
		String jwtToken = RequestTokenResolver.resolve(request);
		if (jwtToken == null || !tokenService.isTokenValid(jwtToken)) {
			return false;
		}
		User user = userService.findById(Long.parseLong(tokenService.getSubject(jwtToken)));
		return Boolean.TRUE.equals(user.getActive()) && user.getAuthorities().stream()
				.anyMatch(role -> DataInitializer.ROLE_ADMIN.equals(role.getAuthority()));
	}
}
