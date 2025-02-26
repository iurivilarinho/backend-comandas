package com.br.food.models;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbEvento")
public class Evento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 255, nullable = false)
	private String descricao;

	@Column(nullable = false)
	private Boolean status;

	@Column(nullable = false)
	private BigDecimal valor;

	@Column(nullable = false)
	private LocalDateTime horaAbertura;

	private LocalDateTime horaFim;

	public Long getId() {
		return id;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public Boolean getStatus() {
		return status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}

	public BigDecimal getValor() {
		return valor;
	}

	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}

	public LocalDateTime getHoraAbertura() {
		return horaAbertura;
	}

	public void setHoraAbertura(LocalDateTime horaAbertura) {
		this.horaAbertura = horaAbertura;
	}

	public LocalDateTime getHoraFim() {
		return horaFim;
	}

	public void setHoraFim(LocalDateTime horaFim) {
		this.horaFim = horaFim;
	}
}