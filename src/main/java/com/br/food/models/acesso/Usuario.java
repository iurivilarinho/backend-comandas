package com.br.food.models.acesso;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbUsuario")
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 100, nullable = false)
	private String name;

	@Column(length = 50, nullable = false, unique = true)
	private String login;

	@Column(length = 100, nullable = false, unique = true)
	private String email;

	@Column(length = 255, nullable = false)
	private String password;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_Id_Role", foreignKey = @ForeignKey(name = "FK_FROM_TBPERFIL_FOR_TBUSUARIO"))
	private Perfil perfil;

	@Column(nullable = false)
	private LocalDateTime passwordLastChanged;

	@Column(nullable = false)
	private Boolean forcePasswordChange;

	@Column(nullable = false)
	private Boolean status;

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Perfil getPerfil() {
		return perfil;
	}

	public void setPerfil(Perfil perfil) {
		this.perfil = perfil;
	}

	public LocalDateTime getPasswordLastChanged() {
		return passwordLastChanged;
	}

	public void setPasswordLastChanged(LocalDateTime passwordLastChanged) {
		this.passwordLastChanged = passwordLastChanged;
	}

	public Boolean getForcePasswordChange() {
		return forcePasswordChange;
	}

	public void setForcePasswordChange(Boolean forcePasswordChange) {
		this.forcePasswordChange = forcePasswordChange;
	}

	public Boolean getStatus() {
		return status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}
}