package com.br.food.authentication;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.authentication.models.RecoveryPassword;
import com.br.food.authentication.repository.RecoveryPasswordRepository;
import com.br.food.authentication.request.RecoveryPasswordRequest;
import com.br.food.models.User;
import com.br.food.service.UserService;
import com.br.food.util.EmailSenderService;

@Service
public class PasswordRecoveryService {

	private static final int CODE_MIN = 1000;
	private static final int CODE_RANGE = 9000; // 1000..9999
	private static final int EXPIRATION_MINUTES = 10;

	private final EmailSenderService emailService;
	private final UserService userService;
	private final RecoveryPasswordRepository recoveryRepository;

	public PasswordRecoveryService(EmailSenderService emailService, UserService userService,
			RecoveryPasswordRepository recoveryRepository) {

		this.emailService = emailService;
		this.userService = userService;
		this.recoveryRepository = recoveryRepository;

	}

	@Transactional
	public void sendRecoveryEmail(String email) {
		// Se vocÃª NÃƒO quiser vazar se o email existe, troque findByEmail por um mÃ©todo
		// opcional.
		// Mantive o seu comportamento: se nÃ£o existir, vai lanÃ§ar.
		User user = userService.findByEmail(email);

		// Garante "um cÃ³digo ativo por vez"
		recoveryRepository.deleteByUserId(user.getId());

		String code = generateFourDigitCode();
		LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);

		recoveryRepository.save(new RecoveryPassword(code, expiresAt, user));

		String html = buildRecoveryEmailHtml(code);
		emailService.send(email, "RecuperaÃ§Ã£o de Senha - Fala Vereador", html, true);
	}

	@Transactional(readOnly = true)
	public void validateCode(String code, String email) {
		User user = userService.findByEmail(email);

		boolean valid = recoveryRepository.existsByUserIdAndCodeAndExpirationDateAfter(user.getId(), code,
				LocalDateTime.now());

		if (!valid) {
			throw new AccessDeniedException("CÃ³digo de recuperaÃ§Ã£o de senha invÃ¡lido!");
		}
	}

	@Transactional
	public void resetPassword(RecoveryPasswordRequest form) {
		User user = userService.findByEmail(form.getEmail());

		Optional<RecoveryPassword> recovery = recoveryRepository
				.findFirstByUserIdAndCodeAndExpirationDateAfter(user.getId(), form.getCode(), LocalDateTime.now());

		if (recovery.isEmpty()) {
			throw new AccessDeniedException("CÃ³digo de recuperaÃ§Ã£o de senha invÃ¡lido!");
		}

		userService.resetPassword(user, form.getNewPassword());

		// Invalida o cÃ³digo apÃ³s uso (evita reuso)
		recoveryRepository.delete(recovery.get());

		// Se preferir, pode limpar todos do usuÃ¡rio:
		// recoveryRepository.deleteByUserId(user.getId());
	}

	private String generateFourDigitCode() {
		Random random = new Random();
		int value = random.nextInt(CODE_RANGE) + CODE_MIN;
		return String.valueOf(value);
	}

	private String buildRecoveryEmailHtml(String code) {
		return "<html><body>" + "<h2>Fala Vereador</h2>" + "<p>Seu cÃ³digo de recuperaÃ§Ã£o de senha Ã©:</p>" + "<h2>"
				+ escapeHtml(code) + "</h2>" + "</body></html>";
	}

	private String escapeHtml(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
				.replace("'", "&#39;");
	}
}
