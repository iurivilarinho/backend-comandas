package com.br.food.authentication;

import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.models.User;
import com.br.food.service.DocumentService;
import com.br.food.service.UserService;

import br.com.gloomy.platform.spi.GoogleIdentity;

/**
 * Turns a Google account into a user of the current tenant. The platform (PLT-038) has already run the OAuth round
 * trip and verified the ID token, the e-mail included, so this only decides who the account is: the user already
 * linked to the Google subject, or the active user with the same e-mail (which gets linked).
 *
 * <p>
 * Comandas is a staff application, and any logged-in user can reach every screen, so an unknown Google account never
 * becomes a user: the restaurant has to register the person first.
 * </p>
 */
@Service
public class GoogleAuthenticationService {

	private final UserService userService;
	private final DocumentService documentService;

	public GoogleAuthenticationService(UserService userService, DocumentService documentService) {
		this.userService = userService;
		this.documentService = documentService;
	}

	@Transactional
	public User authenticate(GoogleIdentity identity) {
		String subject = identity.sub();
		String email = identity.email();

		if (subject == null || subject.isBlank()) {
			throw new AccessDeniedException("Não foi possível identificar a conta Google informada.");
		}
		if (email == null || email.isBlank()) {
			throw new AccessDeniedException("A conta Google informada não possui e-mail disponível.");
		}

		User user = resolveUser(identity);
		if (!Boolean.TRUE.equals(user.getActive())) {
			throw new AccessDeniedException("O usuário vinculado a conta Google está inativo.");
		}
		return user;
	}

	private User resolveUser(GoogleIdentity identity) {
		Optional<User> linkedUser = userService.findOptionalByGoogleSubject(identity.sub());
		if (linkedUser.isPresent()) {
			User user = linkedUser.get();
			applyProfileImageIfMissing(user, identity);
			return userService.save(user);
		}

		User user = userService.findOptionalByEmail(identity.email())
				.or(() -> userService.findOptionalByLogin(identity.email()))
				.orElseThrow(() -> new AccessDeniedException(
						"Esta conta Google não está cadastrada neste estabelecimento. Peça ao administrador para cadastrar seu e-mail."));
		if (user.getGoogleSubject() != null && !user.getGoogleSubject().equals(identity.sub())) {
			throw new AccessDeniedException("Já existe outro vínculo Google para o e-mail informado.");
		}
		user.setGoogleSubject(identity.sub());
		applyProfileImageIfMissing(user, identity);
		return userService.save(user);
	}

	private void applyProfileImageIfMissing(User user, GoogleIdentity identity) {
		if (user.getImage() != null || identity.picture() == null || identity.picture().isBlank()) {
			return;
		}
		user.setImage(documentService.convertGoogleProfileImageToDocument(identity.picture(),
				"google-profile-" + identity.sub()));
	}
}
