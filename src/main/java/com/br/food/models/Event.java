package com.br.food.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbEvent")
public class Event {

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

	public Event() {
	}

	public Long getId() {
		return id;
	}

	public String getDescricao() {
		return descricao;
	}

	public String getDescription() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public void setDescription(String description) {
		this.descricao = description;
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

	public BigDecimal getValue() {
		return valor;
	}

	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}

	public void setValue(BigDecimal value) {
		this.valor = value;
	}

	public LocalDateTime getHoraAbertura() {
		return horaAbertura;
	}

	public LocalDateTime getOpenedAt() {
		return horaAbertura;
	}

	public void setHoraAbertura(LocalDateTime horaAbertura) {
		this.horaAbertura = horaAbertura;
	}

	public void setOpenedAt(LocalDateTime openedAt) {
		this.horaAbertura = openedAt;
	}

	public LocalDateTime getHoraFim() {
		return horaFim;
	}

	public LocalDateTime getClosedAt() {
		return horaFim;
	}

	public void setHoraFim(LocalDateTime horaFim) {
		this.horaFim = horaFim;
	}

	public void setClosedAt(LocalDateTime closedAt) {
		this.horaFim = closedAt;
	}
}
