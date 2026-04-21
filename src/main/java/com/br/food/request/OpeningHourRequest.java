package com.br.food.request;

import java.time.DayOfWeek;
import java.time.LocalTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Opening hour payload")
public class OpeningHourRequest {

	@NotNull(message = "Day of week is required.")
	private DayOfWeek dayOfWeek;

	@NotNull(message = "Open time is required.")
	private LocalTime openTime;

	@NotNull(message = "Close time is required.")
	private LocalTime closeTime;

	private Boolean active;

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
