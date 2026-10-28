package com.br.food.authentication;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.authentication.models.RecoveryPassword;
import com.br.food.authentication.repository.RecoveryPasswordRepository;
import com.br.food.authentication.request.RecoveryPasswordRequest;
import com.br.food.models.CompanyProfile;
import com.br.food.models.User;
import com.br.food.service.CompanyProfileService;
import com.br.food.service.UserService;
import com.br.food.util.EmailSenderService;

@Service
public class PasswordRecoveryService {

	private static final int CODE_MIN = 1000;
	private static final int CODE_RANGE = 9000; // 1000..9999
	private static final int EXPIRATION_MINUTES = 10;

	// Set-password link of a new tenant administrator (CMD-016): long random code, valid for a week.
	private static final String SETUP_CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
	private static final int SETUP_CODE_LENGTH = 12;
	private static final int SETUP_EXPIRATION_DAYS = 7;
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	private final EmailSenderService emailService;
	private final UserService userService;
	private final RecoveryPasswordRepository recoveryRepository;
	private final CompanyProfileService companyProfileService;

	public PasswordRecoveryService(EmailSenderService emailService, UserService userService,
			RecoveryPasswordRepository recoveryRepository, CompanyProfileService companyProfileService) {

		this.emailService = emailService;
		this.userService = userService;
		this.recoveryRepository = recoveryRepository;
		this.companyProfileService = companyProfileService;

	}

	@Transactional
	public void sendRecoveryEmail(String email) {
		User user = userService.findByEmail(email);

		// One active code at a time.
		recoveryRepository.deleteByUserId(user.getId());

		String code = generateFourDigitCode();
		LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);

		recoveryRepository.save(new RecoveryPassword(code, expiresAt, user));

		String html = buildRecoveryEmailHtml(code);
		emailService.send(email, "Recuperação de senha - Comandas", html, true);
	}

	/**
	 * E-mails the user a link that opens the password recovery page with the e-mail and a one-week code already
	 * filled in, so a new administrator can choose a password. Uses the same code table and reset endpoint as
	 * "forgot my password"; only the code is longer and lives longer.
	 *
	 * @param accessUrl address of the tenant with scheme, e.g. {@code https://comandas.gloomy.com.br/zil}
	 */
	@Transactional
	public void sendSetPasswordLink(User user, String accessUrl, String siteName) {
		sendCodeLink(user, accessUrl, "Defina sua senha - " + siteName,
				"Sua conta de administrador foi criada. Clique no link para definir sua senha (válido por "
						+ SETUP_EXPIRATION_DAYS + " dias):",
				siteName);
	}

	/** Set-password link for a user an administrator invited. Uses the name of the restaurant. */
	@Transactional
	public void sendInviteLink(User user, String accessUrl) {
		String siteName = siteName();
		sendCodeLink(user, accessUrl, "Defina sua senha - " + siteName,
				"Você foi convidado para a equipe. Clique no link para definir sua senha (válido por "
						+ SETUP_EXPIRATION_DAYS + " dias):",
				siteName);
	}

	/** Same link for an existing user whose password an administrator asked to reset. */
	@Transactional
	public void sendPasswordResetLink(User user, String accessUrl) {
		String siteName = siteName();
		sendCodeLink(user, accessUrl, "Redefina sua senha - " + siteName,
				"O administrador pediu a redefinição da sua senha. Clique no link para escolher uma nova (válido por "
						+ SETUP_EXPIRATION_DAYS + " dias):",
				siteName);
	}

	private void sendCodeLink(User user, String accessUrl, String subject, String intro, String siteName) {
		recoveryRepository.deleteByUserId(user.getId());

		String code = generateSetupCode();
		recoveryRepository.save(new RecoveryPassword(code, LocalDateTime.now().plusDays(SETUP_EXPIRATION_DAYS), user));

		String base = accessUrl.endsWith("/") ? accessUrl.substring(0, accessUrl.length() - 1) : accessUrl;
		String link = base + "/recuperar-senha?email=" + URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8)
				+ "&code=" + code;
		String html = "<html><body><h2>" + escapeHtml(siteName) + "</h2><p>" + escapeHtml(intro) + "</p><p><a href=\""
				+ escapeHtml(link) + "\">" + escapeHtml(link) + "</a></p></body></html>";
		emailService.send(user.getEmail(), subject, html, true);
	}

	private String siteName() {
		CompanyProfile profile = companyProfileService.findCurrent();
		return profile != null && profile.getCompanyName() != null && !profile.getCompanyName().isBlank()
				? profile.getCompanyName().trim()
				: "Comandas";
	}

	@Transactional(readOnly = true)
	public void validateCode(String code, String email) {
		User user = userService.findByEmail(email);

		boolean valid = recoveryRepository.existsByUserIdAndCodeAndExpirationDateAfter(user.getId(), code,
				LocalDateTime.now());

		if (!valid) {
			throw new AccessDeniedException("Código de recuperação de senha inválido!");
		}
	}

	@Transactional
	public void resetPassword(RecoveryPasswordRequest form) {
		User user = userService.findByEmail(form.getEmail());

		Optional<RecoveryPassword> recovery = recoveryRepository
				.findFirstByUserIdAndCodeAndExpirationDateAfter(user.getId(), form.getCode(), LocalDateTime.now());

		if (recovery.isEmpty()) {
			throw new AccessDeniedException("Código de recuperação de senha inválido!");
		}

		userService.resetPassword(user, form.getNewPassword());

		// The code is single use.
		recoveryRepository.delete(recovery.get());
	}

	private String generateSetupCode() {
		StringBuilder code = new StringBuilder(SETUP_CODE_LENGTH);
		for (int i = 0; i < SETUP_CODE_LENGTH; i++) {
			code.append(SETUP_CODE_ALPHABET.charAt(SECURE_RANDOM.nextInt(SETUP_CODE_ALPHABET.length())));
		}
		return code.toString();
	}

	private String generateFourDigitCode() {
		return String.valueOf(SECURE_RANDOM.nextInt(CODE_RANGE) + CODE_MIN);
	}

	private String buildRecoveryEmailHtml(String code) {
		return "<html><body>" + "<h2>Comandas</h2>" + "<p>Seu código de recuperação de senha é:</p>" + "<h2>"
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
