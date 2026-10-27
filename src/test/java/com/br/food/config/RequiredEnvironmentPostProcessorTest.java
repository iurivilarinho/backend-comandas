package com.br.food.config;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

class RequiredEnvironmentPostProcessorTest {

	private final RequiredEnvironmentPostProcessor processor = new RequiredEnvironmentPostProcessor();

	private static Map<String, Object> validSettings() {
		Map<String, Object> settings = new HashMap<>();
		settings.put("spring.datasource.password", "${SPRING_DATASOURCE_PASSWORD:}");
		settings.put("api.security.token.secret", "0123456789012345678901234567890123456789");
		settings.put("spring.mail.username", "");
		settings.put("spring.mail.password", "");
		return settings;
	}

	private StandardEnvironment environment(Map<String, Object> settings) {
		StandardEnvironment environment = new StandardEnvironment();
		environment.getPropertySources().addFirst(new MapPropertySource("test", settings));
		return environment;
	}

	@Test
	void acceptsAllRequiredSettingsEvenWhenEmpty() {
		assertThatCode(() -> processor.postProcessEnvironment(environment(validSettings()), null))
				.doesNotThrowAnyException();
	}

	@Test
	void listsEveryMissingVariableAtOnce() {
		Map<String, Object> settings = validSettings();
		settings.put("api.security.token.secret", "${JWT_SECRET_UNDEFINED_FOR_TEST}");
		settings.put("spring.mail.password", "${SPRING_MAIL_PASSWORD_UNDEFINED_FOR_TEST}");

		assertThatThrownBy(() -> processor.postProcessEnvironment(environment(settings), null))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("JWT_SECRET is not set")
				.hasMessageContaining("SPRING_MAIL_PASSWORD is not set");
	}

	@Test
	void rejectsAShortJwtSecret() {
		Map<String, Object> settings = validSettings();
		settings.put("api.security.token.secret", "too-short");

		assertThatThrownBy(() -> processor.postProcessEnvironment(environment(settings), null))
				.isInstanceOf(IllegalStateException.class).hasMessageContaining("at least 32 characters");
	}
}
