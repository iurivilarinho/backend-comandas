package com.br.food.util;

import jakarta.servlet.http.HttpServletRequest;

/** Public address of the restaurant, used in links that leave the app (e-mails). */
public final class PublicBaseUrl {

	private PublicBaseUrl() {
	}

	/**
	 * The address the visitor used: every tenant has its own, so one fixed value would be wrong for all but one of
	 * them. A tenant served by path (PLT-035) adds the prefix nginx reports in {@code X-Forwarded-Prefix}, such as
	 * {@code /zil}. With the forwarded headers handled by the framework (the default of the platform) that header is
	 * consumed and shows up as the context path of the request, so both are read.
	 */
	public static String resolve(HttpServletRequest request) {
		String forwarded = request.getHeader("X-Forwarded-Proto");
		String scheme = forwarded != null && !forwarded.isBlank() ? forwarded.split(",")[0].trim() : request.getScheme();
		String path = validPrefix(request.getContextPath());
		if (path.isEmpty()) {
			path = validPrefix(request.getHeader("X-Forwarded-Prefix"));
		}
		String host = request.getServerName();
		int port = request.getServerPort();
		boolean defaultPort = port <= 0 || port == 80 || port == 443 || forwarded != null;
		return scheme + "://" + host + (defaultPort ? "" : ":" + port) + path;
	}

	private static String validPrefix(String prefix) {
		return prefix != null && prefix.matches("/[a-z0-9-]{3,40}") ? prefix : "";
	}
}
