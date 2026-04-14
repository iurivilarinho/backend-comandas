package com.br.food.models;

import com.br.food.request.DiningTableRequest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbDiningTable")
public class DiningTable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 10, nullable = false)
	private String numero;

	@Column(nullable = false)
	private Boolean status;

	@Column(nullable = false)
	private Boolean ocupada;

	public DiningTable() {
	}

	public DiningTable(String numero) {
		this.ocupada = false;
		this.status = true;
		this.numero = numero;
	}

	public DiningTable(DiningTableRequest request) {
		this(request.getNumber());
	}

	public void update(DiningTableRequest request) {
		this.numero = request.getNumber();
	}

	public Long getId() {
		return id;
	}

	public String getNumero() {
		return numero;
	}

	public String getNumber() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

	public void setNumber(String number) {
		this.numero = number;
	}

	public Boolean getStatus() {
		return status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}

	public Boolean getOcupada() {
		return ocupada;
	}

	public Boolean getOccupied() {
		return ocupada;
	}

	public void setOcupada(Boolean ocupada) {
		this.ocupada = ocupada;
	}

	public void setOccupied(Boolean occupied) {
		this.ocupada = occupied;
	}

}
