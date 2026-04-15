package com.br.food.authentication;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.authentication.models.Role;
import com.br.food.authentication.service.RoleService;
import com.br.food.models.User;
import com.br.food.service.DocumentService;
import com.br.food.service.UserService;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

@Service
public class GoogleAuthenticationService {

	private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
	private static final String GOOGLE_USERINFO_URL = "https://openidconnect.googleapis.com/v1/userinfo";

	private final UserService userService;
	private final RoleService roleService;
	private final DocumentService documentService;
	private final String googleClientId;
	private final String googleClientSecret;
	private final String defaultGoogleRole;

	public GoogleAuthenticationService(UserService userService, RoleService roleService, DocumentService documentService,
			@Value("${google.oauth.client-id:}") String googleClientId,
			@Value("${google.oauth.client-secret:}") String googleClientSecret,
			@Value("${google.oauth.default-role:ROLE_USER}") String defaultGoogleRole) {
		this.userService = userService;
		this.roleService = roleService;
		this.documentService = documentService;
		this.googleClientId = googleClientId;
		this.googleClientSecret = googleClientSecret;
		this.defaultGoogleRole = defaultGoogleRole;
	}

	@Transactional
	public User authenticate(GoogleLoginRequest request) {
		GoogleTokenResponse tokenResponse = exchangeAuthorizationCode(request.code(), request.redirectUri());
		String idToken = tokenResponse.getIdToken();
		GoogleIdToken.Payload payload = verifyIdToken(idToken);
		String subject = payload.getSubject();
		String email = payload.getEmail();

		if (subject == null || subject.isBlank()) {
			throw new AccessDeniedException("Nao foi possivel identificar a conta Google informada.");
		}

		if (email == null || email.isBlank()) {
			throw new AccessDeniedException("A conta Google informada nao possui e-mail disponivel.");
		}

		if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
			throw new AccessDeniedException("O e-mail da conta Google precisa estar verificado.");
		}

		User user = resolveUser(payload, tokenResponse.getAccessToken(), subject, email);
		if (!Boolean.TRUE.equals(user.getActive())) {
			throw new AccessDeniedException("O usuario vinculado a conta Google esta inativo.");
		}

		return user;
	}

	private GoogleTokenResponse exchangeAuthorizationCode(String code, String redirectUri) {
		if (googleClientId == null || googleClientId.isBlank()) {
			throw new DataIntegrityViolationException("Login com Google nao esta configurado.");
		}

		if (googleClientSecret == null || googleClientSecret.isBlank()) {
			throw new DataIntegrityViolationException("Client secret do Google nao configurado.");
		}

		if (redirectUri == null || redirectUri.isBlank()) {
			throw new AccessDeniedException("Origin do login Google nao informado.");
		}

		try {
			return new GoogleAuthorizationCodeTokenRequest(new NetHttpTransport(), GsonFactory.getDefaultInstance(),
					GOOGLE_TOKEN_URL, googleClientId, googleClientSecret, code, redirectUri).execute();
		} catch (IOException exception) {
			throw new AccessDeniedException("Nao foi possivel concluir a autenticacao com Google.");
		}
	}

	private User resolveUser(GoogleIdToken.Payload payload, String accessToken, String subject, String email) {
		Optional<User> linkedUser = userService.findOptionalByGoogleSubject(subject);
		if (linkedUser.isPresent()) {
			User user = linkedUser.get();
			applyProfileImageIfMissing(user, payload, accessToken, subject);
			return userService.save(user);
		}

		Optional<User> existingByEmail = userService.findOptionalByEmail(email)
				.or(() -> userService.findOptionalByLogin(email));

		if (existingByEmail.isPresent()) {
			User user = existingByEmail.get();
			if (user.getGoogleSubject() != null && !user.getGoogleSubject().equals(subject)) {
				throw new AccessDeniedException("Ja existe outro vinculo Google para o e-mail informado.");
			}

			user.setGoogleSubject(subject);
			applyProfileImageIfMissing(user, payload, accessToken, subject);
			return userService.save(user);
		}

		return createPendingGoogleUser(payload, accessToken, email, subject);
	}

	private User createPendingGoogleUser(GoogleIdToken.Payload payload, String accessToken, String email,
			String subject) {
		Role role = roleService.findByName(defaultGoogleRole);
		if (!Boolean.TRUE.equals(role.getActive())) {
			throw new DataIntegrityViolationException("O perfil padrao para login Google esta inativo.");
		}

		User user = User.createPendingGoogleUser(resolveName(payload, email), email, subject);
		user.setRoles(Set.of(role));
		applyProfileImageIfMissing(user, payload, accessToken, subject);
		return userService.save(user);
	}

	private void applyProfileImageIfMissing(User user, GoogleIdToken.Payload payload, String accessToken,
			String subject) {
		if (user.getImage() != null) {
			return;
		}

		String pictureUrl = resolvePictureUrl(payload, accessToken);
		if (pictureUrl == null || pictureUrl.isBlank()) {
			return;
		}

		user.setImage(documentService.convertGoogleProfileImageToDocument(pictureUrl, "google-profile-" + subject));
	}

	private String resolvePictureUrl(GoogleIdToken.Payload payload, String accessToken) {
		Object pictureClaim = payload.get("picture");
		if (pictureClaim instanceof String pictureUrl && !pictureUrl.isBlank()) {
			return pictureUrl;
		}

		return fetchPictureFromUserInfo(accessToken);
	}

	private String fetchPictureFromUserInfo(String accessToken) {
		if (accessToken == null || accessToken.isBlank()) {
			return null;
		}

		try {
			HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory();
			HttpRequest request = requestFactory.buildGetRequest(new GenericUrl(GOOGLE_USERINFO_URL));
			request.getHeaders().setAuthorization("Bearer " + accessToken);
			HttpResponse response = request.execute();
			try {
				Map<String, Object> userInfo = response.parseAs(Map.class);
				Object picture = userInfo.get("picture");
				return picture instanceof String ? (String) picture : null;
			} finally {
				response.disconnect();
			}
		} catch (IOException exception) {
			return null;
		}
	}

	private String resolveName(GoogleIdToken.Payload payload, String email) {
		Object nameClaim = payload.get("name");
		if (nameClaim instanceof String name && !name.isBlank()) {
			return name;
		}
		return email;
	}

	private GoogleIdToken.Payload verifyIdToken(String idToken) {
		if (idToken == null || idToken.isBlank()) {
			throw new AccessDeniedException("O Google nao retornou um ID token valido.");
		}

		try {
			GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(),
					GsonFactory.getDefaultInstance()).setAudience(List.of(googleClientId)).build();

			GoogleIdToken googleIdToken = verifier.verify(idToken);
			if (googleIdToken == null) {
				throw new AccessDeniedException("Token Google invalido.");
			}

			return googleIdToken.getPayload();
		} catch (GeneralSecurityException | IOException exception) {
			throw new AccessDeniedException("Nao foi possivel validar o token do Google.");
		}
	}
}

