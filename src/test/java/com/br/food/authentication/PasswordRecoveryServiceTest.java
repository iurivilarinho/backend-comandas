package com.br.food.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.br.food.authentication.models.RecoveryPassword;
import com.br.food.authentication.repository.RecoveryPasswordRepository;
import com.br.food.models.User;
import com.br.food.service.UserService;
import com.br.food.util.EmailSenderService;

@ExtendWith(MockitoExtension.class)
class PasswordRecoveryServiceTest {

	@Mock
	private EmailSenderService emailService;

	@Mock
	private UserService userService;

	@Mock
	private RecoveryPasswordRepository recoveryRepository;

	@InjectMocks
	private PasswordRecoveryService service;

	@Test
	void theSetPasswordLinkCarriesALongCodeThatLivesAWeekAndPointsToTheTenantAddress() {
		User admin = new User();
		admin.setEmail("boss@example.com");
		ReflectionTestUtils.setField(admin, "id", 4L);

		service.sendSetPasswordLink(admin, "https://comandas.gloomy.com.br/zil/", "Zil Restaurante");

		ArgumentCaptor<RecoveryPassword> saved = ArgumentCaptor.forClass(RecoveryPassword.class);
		verify(recoveryRepository).save(saved.capture());
		assertThat(saved.getValue().getCode()).hasSize(12);
		assertThat(saved.getValue().getExpirationDate()).isAfter(LocalDateTime.now().plusDays(6));

		ArgumentCaptor<String> html = ArgumentCaptor.forClass(String.class);
		verify(emailService).send(eq("boss@example.com"), eq("Defina sua senha - Zil Restaurante"), html.capture(), eq(true));
		Matcher link = Pattern.compile("href=\"([^\"]+)\"").matcher(html.getValue());
		assertThat(link.find()).isTrue();
		assertThat(link.group(1)).startsWith("https://comandas.gloomy.com.br/zil/recuperar-senha?email=boss%40example.com&amp;code=")
				.endsWith(saved.getValue().getCode());
	}

	@Test
	void theRecoveryCodeOfForgotMyPasswordHasFourDigits() {
		User user = new User();
		ReflectionTestUtils.setField(user, "id", 4L);
		org.mockito.Mockito.when(userService.findByEmail("boss@example.com")).thenReturn(user);

		service.sendRecoveryEmail("boss@example.com");

		ArgumentCaptor<RecoveryPassword> saved = ArgumentCaptor.forClass(RecoveryPassword.class);
		verify(recoveryRepository).save(saved.capture());
		assertThat(saved.getValue().getCode()).matches("[0-9]{4}");
		verify(emailService).send(eq("boss@example.com"), anyString(), any(), eq(true));
	}
}
