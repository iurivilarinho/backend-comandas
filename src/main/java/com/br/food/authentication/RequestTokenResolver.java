package com.br.food.authentication;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Reads the access JWT of a request: the cookie "token" first, then the "Authorization: Bearer" header.
 */
public final class RequestTokenResolver {

	private RequestTokenResolver() {
	}

	public static String resolve(HttpServletRequest request) {
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
}
