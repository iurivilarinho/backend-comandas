package com.br.food.authentication.response;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.br.food.models.User;
import com.br.food.response.DocumentResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UserProfileResponse", description = "Dados do perfil do usuario autenticado.")
public class UserProfileResponse {

	private Long id;
	private String name;
	private String login;
	private String cpf;
	private String email;
	private String cellphoneCorporate;
	private Boolean active;
	private Boolean registrationCompleted;
	private Set<RoleResponse> roles = new HashSet<>();
	private DocumentResponse image;

	public UserProfileResponse(User user) {
		this.id = user.getId();
		this.name = user.getName();
		this.login = user.getLogin();
		this.cpf = user.getCpf();
		this.email = user.getEmail();
		this.cellphoneCorporate = user.getCellphoneCorporate();
		this.active = user.getActive();
		this.registrationCompleted = user.getRegistrationCompleted();

		this.roles = user.getRoles() != null
				? user.getRoles().stream().map(RoleResponse::new).collect(Collectors.toSet())
				: null;

		this.image = user.getImage() != null ? new DocumentResponse(user.getImage()) : null;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getLogin() {
		return login;
	}

	public String getCpf() {
		return cpf;
	}

	public String getEmail() {
		return email;
	}

	public String getCellphoneCorporate() {
		return cellphoneCorporate;
	}

	public Boolean getActive() {
		return active;
	}

	public Boolean getRegistrationCompleted() {
		return registrationCompleted;
	}

	public Set<RoleResponse> getRoles() {
		return roles;
	}

	public DocumentResponse getImage() {
		return image;
	}
}
