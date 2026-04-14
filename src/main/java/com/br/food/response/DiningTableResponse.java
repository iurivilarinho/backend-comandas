package com.br.food.response;

import com.br.food.models.DiningTable;

public class DiningTableResponse {

	private final Long id;
	private final String number;
	private final Boolean active;
	private final Boolean occupied;

	public DiningTableResponse(DiningTable table) {
		this.id = table.getId();
		this.number = table.getNumber();
		this.active = table.getStatus();
		this.occupied = table.getOccupied();
	}

	public Long getId() {
		return id;
	}

	public String getNumber() {
		return number;
	}

	public Boolean getActive() {
		return active;
	}

	public Boolean getOccupied() {
		return occupied;
	}
}
