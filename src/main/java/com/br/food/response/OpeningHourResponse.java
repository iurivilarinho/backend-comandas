package com.br.food.response;

import java.time.DayOfWeek;
import java.time.LocalTime;

import com.br.food.models.CompanyOpeningHour;

public class OpeningHourResponse {

	private final Long id;
	private final DayOfWeek dayOfWeek;
	private final LocalTime openTime;
	private final LocalTime closeTime;
	private final Boolean active;

	public OpeningHourResponse(CompanyOpeningHour openingHour) {
		this.id = openingHour.getId();
		this.dayOfWeek = openingHour.getDayOfWeek();
		this.openTime = openingHour.getOpenTime();
		this.closeTime = openingHour.getCloseTime();
		this.active = openingHour.getActive();
	}

	public Long getId() {
		return id;
	}

	public DayOfWeek getDayOfWeek() {
		return dayOfWeek;
	}

	public LocalTime getOpenTime() {
		return openTime;
	}

	public LocalTime getCloseTime() {
		return closeTime;
	}

	public Boolean getActive() {
		return active;
	}
}
