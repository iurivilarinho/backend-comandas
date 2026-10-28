package com.br.food.authentication.models;

import java.time.LocalDateTime;

import com.br.food.models.User;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbRecoveryPassword")
@Schema(name = "RecoveryPassword", description = "Entidade que armazena códigos de recuperação de senha e sua expiração para um usuário.")
public class RecoveryPassword {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Schema(description = "Código de recuperação: 4 dígitos no esqueci-minha-senha, 12 caracteres no link de definir senha.", example = "1234")
	private Long id;

	@Column(length = 16, nullable = false)
	@Schema(description = "Código de recuperação com 4 dígitos.", example = "1234")
	private String code;

	@Column(nullable = false)
	@Schema(description = "Data/hora de expiração do código.", example = "2026-02-10T23:59:59")
	private LocalDateTime expirationDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_Id_User", nullable = false, foreignKey = @ForeignKey(name = "FK_FROM_TBRECOVERYPASSWORD_FOR_TBUSER"))
	@Schema(description = "Usuário associado ao código de recuperação.")
	private User user;

	public RecoveryPassword() {
	}

	public RecoveryPassword(String code, LocalDateTime expirationDate, User user) {
		this.code = code;
		this.expirationDate = expirationDate;
		this.user = user;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public LocalDateTime getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(LocalDateTime expirationDate) {
		this.expirationDate = expirationDate;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}
