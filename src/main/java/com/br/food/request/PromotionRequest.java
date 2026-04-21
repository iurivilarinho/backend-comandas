package com.br.food.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Promotion create or update payload")
public class PromotionRequest {

	@NotBlank(message = "Promotion title is required.")
	@Size(min = 3, max = 120, message = "Promotion title must have between 3 and 120 characters.")
	private String title;

	@Size(max = 500, message = "Description must have at most 500 characters.")
	private String description;

	@NotEmpty(message = "At least one product must be selected for the promotion.")
	private List<Long> productIds = new ArrayList<>();

	@NotNull(message = "Promotion price is required.")
	@DecimalMin(value = "0.01", message = "Promotion price must be greater than zero.")
	private BigDecimal promotionPrice;

	@DecimalMin(value = "0.0", message = "Old price cannot be negative.")
	private BigDecimal oldPrice;

	@DecimalMin(value = "0.0", message = "New price cannot be negative.")
	private BigDecimal newPrice;

	@NotNull(message = "Expiration date is required.")
	@FutureOrPresent(message = "Expiration date cannot be in the past.")
	private LocalDate expiresAt;

	private Boolean active;

	public String getTitle() {
		return title != null ? title.trim() : null;
	}

	public String getDescription() {
		return description != null && !description.isBlank() ? description.trim() : null;
	}

	public List<Long> getProductIds() {
		return productIds != null ? productIds : List.of();
	}

	public BigDecimal getPromotionPrice() {
		return promotionPrice;
	}

	public BigDecimal getOldPrice() {
		return oldPrice;
	}

	public BigDecimal getNewPrice() {
		return newPrice;
	}

	public LocalDate getExpiresAt() {
		return expiresAt;
	}

	public Boolean getActive() {
		return active;
	}
}
