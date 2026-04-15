package com.br.food.models;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.br.food.authentication.models.Role;
import com.br.food.request.UserRequest;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@SuppressWarnings("serial")
@Entity
@Table(name = "users", indexes = { @Index(name = "login", columnList = "login"),
		@Index(name = "google_subject", columnList = "google_subject") }, uniqueConstraints = @UniqueConstraint(name = "cpf", columnNames = "cpf"))
@Schema(name = "User", description = "Entidade que representa um usuario do sistema.")
public class User implements UserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Schema(description = "Identificador unico do usuario.", example = "1")
	private Long id;

	@Column(length = 100, nullable = false)
	@Schema(description = "Nome completo do usuario.", example = "Samuel Rocha Oliveira")
	private String name;

	@Column(length = 14)
	@Schema(description = "CPF do usuario.", example = "12345678901")
	private String cpf;

	@Column(length = 100, nullable = false)
	@Schema(description = "Login utilizado para autenticacao no sistema.", example = "samuel.oliveira")
	private String login;

	@JsonIgnore
	@Column(length = 200, nullable = false)
	@Schema(description = "Senha criptografada do usuario.", accessMode = Schema.AccessMode.READ_ONLY)
	private String password;

	@Column(name = "password_last_changed", nullable = false)
	@Schema(description = "Data e hora da ultima alteracao de senha.", example = "2026-02-08T18:06:02")
	private LocalDateTime passwordLastChanged;

	@Column(name = "force_password_change", nullable = false)
	@Schema(description = "Indica se o usuario deve alterar a senha no proximo acesso.", example = "false")
	private boolean forcePasswordChange = false;

	@Column(length = 100)
	@Schema(description = "E-mail do usuario.", example = "samuel.oliveira@empresa.com")
	private String email;

	@Schema(description = "Telefone corporativo do usuario.", example = "31999999999")
	private String cellphoneCorporate;

	@Column(name = "google_subject", length = 100)
	@Schema(description = "Identificador estavel do usuario no Google.", example = "114572938475629384756")
	private String googleSubject;

	@Column(name = "registration_completed", nullable = false)
	@Schema(description = "Indica se o cadastro obrigatorio do usuario ja foi finalizado.", example = "true")
	private Boolean registrationCompleted;

	@JsonIgnore
	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_Id_Image", foreignKey = @ForeignKey(name = "FK_FROM_TBDOCUMENT_FOR_TBUSER"))
	@Schema(description = "Imagem de perfil associada ao usuario.")
	private Document image;

	@Column(name = "status")
	@Schema(description = "Indica se o usuario esta ativo no sistema.", example = "true")
	private Boolean active;

	@Schema(description = "Data e hora de criacao do usuario.", example = "2026-02-08T18:06:02")
	private LocalDateTime createdAt;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "fk_Id_User"), inverseJoinColumns = @JoinColumn(name = "fk_Id_Role"), foreignKey = @ForeignKey(name = "FK_FROM_USER"), inverseForeignKey = @ForeignKey(name = "FK_FROM_ROLE"))
	@Fetch(FetchMode.JOIN)
	@Schema(description = "Perfis de acesso associados ao usuario.")
	private Set<Role> roles = new HashSet<>();

	@Schema(description = "Matricula ou registro interno do usuario.", example = "MAT-2026-001")
	private String registration;

	public User() {
	}

	@PrePersist
	public void create() {
		if (this.active == null) {
			this.active = true;
		}
		if (this.createdAt == null) {
			this.createdAt = LocalDateTime.now();
		}
		if (this.registrationCompleted == null) {
			this.registrationCompleted = true;
		}
	}

	public User(UserRequest form, Document image) {
		this.login = form.getLogin();
		this.name = form.getName();
		this.email = form.getEmail();
		this.password = new BCryptPasswordEncoder().encode(form.getPassword());
		this.cpf = form.getCpf();
		this.image = image;
		this.forcePasswordChange = false;
		this.passwordLastChanged = LocalDateTime.now();
		this.active = true;
		this.registrationCompleted = true;
	}

	public User(String name, String cpf, String login, String password, String email, String cellphoneCorporate) {
		this.login = login;
		this.name = name;
		this.email = email;
		this.password = new BCryptPasswordEncoder().encode(password);
		this.cpf = cpf;
		this.cellphoneCorporate = cellphoneCorporate;
		this.forcePasswordChange = false;
		this.passwordLastChanged = LocalDateTime.now();
		this.active = true;
		this.registrationCompleted = true;
	}

	public static User createPendingGoogleUser(String name, String email, String googleSubject) {
		User user = new User();
		user.setName(name);
		user.setEmail(email);
		user.setLogin(email);
		user.setPassword(new BCryptPasswordEncoder().encode(UUID.randomUUID().toString()));
		user.setActive(Boolean.TRUE);
		user.setForcePasswordChange(false);
		user.setPasswordLastChanged(LocalDateTime.now());
		user.setCreatedAt(LocalDateTime.now());
		user.setGoogleSubject(googleSubject);
		user.setRegistrationCompleted(Boolean.FALSE);
		return user;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	@Override
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public LocalDateTime getPasswordLastChanged() {
		return passwordLastChanged;
	}

	public void setPasswordLastChanged(LocalDateTime passwordLastChanged) {
		this.passwordLastChanged = passwordLastChanged;
	}

	public boolean isForcePasswordChange() {
		return forcePasswordChange;
	}

	public void setForcePasswordChange(boolean forcePasswordChange) {
		this.forcePasswordChange = forcePasswordChange;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCellphoneCorporate() {
		return cellphoneCorporate;
	}

	public void setCellphoneCorporate(String cellphoneCorporate) {
		this.cellphoneCorporate = cellphoneCorporate;
	}

	public String getGoogleSubject() {
		return googleSubject;
	}

	public void setGoogleSubject(String googleSubject) {
		this.googleSubject = googleSubject;
	}

	public Boolean getRegistrationCompleted() {
		return registrationCompleted == null ? Boolean.TRUE : registrationCompleted;
	}

	public void setRegistrationCompleted(Boolean registrationCompleted) {
		this.registrationCompleted = registrationCompleted;
	}

	public Document getImage() {
		return image;
	}

	public void setImage(Document image) {
		this.image = image;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	public String getRegistration() {
		return registration;
	}

	public void setRegistration(String registration) {
		this.registration = registration;
	}

	@JsonIgnore
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.roles;
	}

	@Override
	@Schema(description = "Nome de usuario utilizado na autenticacao.", example = "samuel.oliveira")
	public String getUsername() {
		return login;
	}

	@Override
	public boolean isAccountNonExpired() {
		return UserDetails.super.isAccountNonExpired();
	}

	@Override
	public boolean isAccountNonLocked() {
		return UserDetails.super.isAccountNonLocked();
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return UserDetails.super.isCredentialsNonExpired();
	}

	@Override
	public boolean isEnabled() {
		return UserDetails.super.isEnabled();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof User)) {
			return false;
		}
		User user = (User) other;
		return this.id != null && this.id.equals(user.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
