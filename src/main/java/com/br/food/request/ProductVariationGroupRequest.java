package com.br.food.request;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ProductVariationGroupRequest {

	private Long id;

	@NotBlank(message = "Variation group title is required.")
	@Size(max = 60, message = "Variation group title must have at most 60 characters.")
	private String title;

	private Boolean active;

	@Valid
	@NotNull(message = "Variation list is required.")
	private List<ProductVariationRequest> variations = new ArrayList<>();

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public Boolean getActive() {
		return active;
	}

	public List<ProductVariationRequest> getVariations() {
		return variations != null ? variations : List.of();
	}

	public boolean getResolvedActive() {
		return !Boolean.FALSE.equals(active);
	}
}
