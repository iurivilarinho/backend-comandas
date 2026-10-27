package com.br.food.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Fails the startup with one readable message when a required setting is missing (CMD-015).
 *
 * <p>
 * Secrets have no default in {@code application.properties}. Without this check Spring stops at the first
 * unresolved placeholder with a generic message; here every missing variable is listed at once, together with the
 * environment variable that provides it.
 * </p>
 */
public class RequiredEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

	static final int MIN_JWT_SECRET_LENGTH = 32;

	/** Property -> environment variable that must provide it (the property must resolve, it may be empty). */
	private static final String[][] REQUIRED = {
			{ "spring.datasource.password", "SPRING_DATASOURCE_PASSWORD" },
			{ "api.security.token.secret", "JWT_SECRET" },
			{ "spring.mail.username", "SPRING_MAIL_USERNAME" },
			{ "spring.mail.password", "SPRING_MAIL_PASSWORD" } };

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		List<String> problems = new ArrayList<>();
		for (String[] entry : REQUIRED) {
			if (resolve(environment, entry[0]) == null) {
				problems.add(entry[1] + " is not set");
			}
		}
		String jwt = resolve(environment, "api.security.token.secret");
		if (jwt != null && jwt.trim().length() < MIN_JWT_SECRET_LENGTH) {
			problems.add("JWT_SECRET must have at least " + MIN_JWT_SECRET_LENGTH + " characters");
		}
		if (!problems.isEmpty()) {
			throw new IllegalStateException("Missing or invalid required configuration: "
					+ String.join("; ", problems)
					+ ". Set them in the environment (.env of the app) or in a local .env file; see .env.example.");
		}
	}

	private static String resolve(ConfigurableEnvironment environment, String property) {
		try {
			return environment.getProperty(property);
		} catch (IllegalArgumentException unresolvedPlaceholder) {
			return null;
		}
	}

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}
}
