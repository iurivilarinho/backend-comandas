package com.br.food.models;

import com.br.food.request.DiningTableRequest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "dining_tables")
public class DiningTable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "table_number", length = 10, nullable = false, unique = true)
	private String number;

	@Column(name = "active", nullable = false)
	private Boolean active;

	@Column(name = "occupied", nullable = false)
	private Boolean occupied;

	public DiningTable() {
	}

	public DiningTable(String number) {
		this.number = number;
		this.active = true;
		this.occupied = false;
	}

	public DiningTable(DiningTableRequest request) {
		this(request.getNumber());
	}

	public void update(DiningTableRequest request) {
		this.number = request.getNumber();
	}

	public Long getId() {
		return id;
	}

	public String getNumber() {
		return number;
	}

	public String getNumero() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public void setNumero(String number) {
		this.number = number;
	}

	public Boolean getStatus() {
		return active;
	}

	public Boolean getActive() {
		return active;
	}

	public void setStatus(Boolean active) {
		this.active = active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Boolean getOccupied() {
		return occupied;
	}

	public Boolean getOcupada() {
		return occupied;
	}

	public void setOccupied(Boolean occupied) {
		this.occupied = occupied;
	}

	public void setOcupada(Boolean occupied) {
		this.occupied = occupied;
	}
}
