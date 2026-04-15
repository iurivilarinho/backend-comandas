package com.br.food.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.br.food.authentication.request.GoogleRegistrationCompletionRequest;
import com.br.food.models.Document;
import com.br.food.models.User;
import com.br.food.repository.UserRepository;
import com.br.food.request.UserRequest;
import com.br.food.request.UserUpdateRequest;
import com.br.food.specification.UserSpecification;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;

@Service
public class UserService {

	private final DocumentService documentService;
	private final UserRepository userRepository;
	private final EntityManager entityManager;
	private final PasswordEncoder passwordEncoder;

	@Value("${api.security.token.secret}")
	private String secret;

	public UserService(DocumentService documentService, UserRepository userRepository, EntityManager entityManager,
			PasswordEncoder passwordEncoder) {
		this.documentService = documentService;
		this.userRepository = userRepository;
		this.entityManager = entityManager;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional(readOnly = true)
	public User findByEmail(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new EntityNotFoundException("Email nao encontrado na nossa base de dados."));
	}

	@Transactional(readOnly = true)
	public Optional<User> findOptionalByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	@Transactional(readOnly = true)
	public Optional<User> findOptionalByLogin(String login) {
		return userRepository.findByLogin(login);
	}

	@Transactional(readOnly = true)
	public Optional<User> findOptionalByGoogleSubject(String googleSubject) {
		return userRepository.findByGoogleSubject(googleSubject);
	}

	@Transactional(readOnly = true)
	public User findUserLoggedIn() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			return null;
		}

		Object principal = authentication.getPrincipal();
		if (principal instanceof User) {
			return (User) principal;
		}

		String username = authentication.getName();
		return userRepository.findByLogin(username).orElse(null);
	}

	@Transactional
	public void resetPassword(User user, String newPassword) {
		user.setPassword(newPassword);
		user.setForcePasswordChange(false);
		user.setPasswordLastChanged(LocalDateTime.now());
		userRepository.save(user);
	}

	@Transactional
	public void resetPasswordById(Long userId, String newPassword) {
		User user = findById(userId);
		user.setPassword(passwordEncoder.encode(newPassword));
		user.setForcePasswordChange(true);
		user.setPasswordLastChanged(LocalDateTime.now());
		userRepository.save(user);
	}

	@Transactional(readOnly = true)
	public User findByToken(String tokenJWT) {
		try {
			Algorithm algorithm = Algorithm.HMAC256(secret);
			JWTVerifier verifier = JWT.require(algorithm).withIssuer("API Autenticacao").build();
			DecodedJWT decodedJWT = verifier.verify(tokenJWT);
			String id = decodedJWT.getSubject();
			return userRepository.findById(Long.parseLong(id))
					.orElseThrow(() -> new EntityNotFoundException("Usuario nao encontrado para o login informado."));
		} catch (JWTDecodeException exception) {
			throw new DataIntegrityViolationException("token invalido!");
		}
	}

	@Transactional(readOnly = true)
	public void emailValidate(String email) {
		if (email != null) {
			userRepository.findByEmail(email).ifPresent(user -> {
				throw new DataIntegrityViolationException("O e-mail " + user.getEmail() + " ja esta cadastrado.");
			});
		}
	}

	@Transactional(readOnly = true)
	public void cpfValidate(String cpf) {
		if (cpf != null) {
			userRepository.findByCpf(cpf).ifPresent(user -> {
				throw new DataIntegrityViolationException("O cpf " + user.getCpf() + " ja esta cadastrado.");
			});
		}
	}

	@Transactional(readOnly = true)
	public void loginValidate(String login) {
		userRepository.findByLogin(login).ifPresent(user -> {
			throw new DataIntegrityViolationException("O login " + user.getLogin() + " ja esta cadastrado.");
		});
	}

	@Transactional(readOnly = true)
	public Optional<User> findByCpf(String cpf) {
		return userRepository.findByCpf(cpf);
	}

	@Transactional
	public User create(UserRequest form, MultipartFile img) throws IOException {
		if (form.getEmail() != null && !form.getEmail().isBlank()) {
			emailValidate(form.getEmail());
		}
		if (form.getCpf() != null && !form.getCpf().isBlank()) {
			cpfValidate(form.getCpf());
		}
		loginValidate(form.getLogin());

		Document document = img != null ? documentService.convertToDocument(img) : null;

		User user = new User(form, document);

		return userRepository.save(user);
	}

	@Transactional
	public User save(User user) {
		return userRepository.save(user);
	}

	@Transactional
	public User update(Long userId, UserUpdateRequest form, MultipartFile img) throws IOException {
		User user = findById(userId);

		updateBasicUserData(user, form.getName(), form.getLogin(), form.getCpf(), form.getCellphoneCorporate());
		updateEmail(user, form.getEmail());

		if (img != null) {
			user.setImage(documentService.convertToDocument(img));
		}

		return userRepository.save(user);
	}

	@Transactional
	public User completeGoogleRegistration(User user, GoogleRegistrationCompletionRequest request) {
		if (Boolean.TRUE.equals(user.getRegistrationCompleted())) {
			throw new DataIntegrityViolationException("O cadastro deste usuario ja foi finalizado.");
		}

		updateBasicUserData(user, request.name(), request.login(), request.cpf(), request.cellphoneCorporate());
		user.setRegistrationCompleted(Boolean.TRUE);
		return userRepository.save(user);
	}

	private void updateBasicUserData(User user, String name, String login, String cpf, String cellphoneCorporate) {
		user.setName(name);
		user.setCellphoneCorporate(cellphoneCorporate);

		if (login != null && !login.equals(user.getLogin())) {
			loginValidate(login);
			user.setLogin(login);
		}

		if (cpf != null && !cpf.isBlank()) {
			String normalizedCpf = cpf.replaceAll("\\D", "");
			if (!normalizedCpf.equals(user.getCpf())) {
				cpfValidate(normalizedCpf);
				user.setCpf(normalizedCpf);
			}
		}
	}

	private void updateEmail(User user, String email) {
		String currentEmail = user.getEmail();
		if (email != null && !email.isBlank() && !email.equals(currentEmail)) {
			emailValidate(email);
			user.setEmail(email);
		} else if (email == null || email.isBlank()) {
			user.setEmail(null);
		}
	}

	@Transactional(readOnly = true)
	public User findById(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new EntityNotFoundException("Usuario nao encontrado para o ID: " + userId));
	}

	@Transactional(readOnly = true)
	public User getReferenceById(Long userId) {
		return userRepository.getReferenceById(userId);
	}

	@Transactional(readOnly = true)
	public List<User> findAllById(Set<Long> userIds) {
		return userRepository.findAllById(userIds);
	}

	@Transactional(readOnly = true)
	public Page<User> findAllPage(Long idCargo, List<Long> rolesIds, Boolean status, String search, Pageable page) {
		return userRepository.findAll(UserSpecification.searchAllFields(search, entityManager)
				.and(UserSpecification.statusIgual(status)).and(UserSpecification.hasAnyRoleIds(rolesIds)), page);
	}

	@Transactional
	public void enableDisable(Long userId, Boolean status) {
		User user = findById(userId);
		user.setActive(status);
		userRepository.save(user);
	}
}
