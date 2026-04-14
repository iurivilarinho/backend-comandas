package com.br.food.models.acesso;

import java.time.LocalDateTime;

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
@Table(name = "tbPasswordRecovery")
public class PasswordRecovery {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_Id_User", foreignKey = @ForeignKey(name = "FK_FROM_TBUSER_FOR_TBRECOVERYPASSWORD"))
	private User usuario;

	@Column(length = 10, nullable = false)
	private String code;

	@Column(nullable = false)
	private LocalDateTime expiredDate;

	public PasswordRecovery() {
	}

	public Long getId() {
		return id;
	}

	public User getUser() {
		return usuario;
	}

	public void setUser(User usuario) {
		this.usuario = usuario;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public LocalDateTime getExpiredDate() {
		return expiredDate;
	}

	public void setExpiredDate(LocalDateTime expiredDate) {
		this.expiredDate = expiredDate;
	}
}
