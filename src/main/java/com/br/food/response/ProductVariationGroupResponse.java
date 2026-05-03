package com.br.food.response;

import java.util.Comparator;
import java.util.List;

import com.br.food.models.ProductVariationGroup;

public class ProductVariationGroupResponse {

	private final Long id;
	private final String title;
	private final Boolean active;
	private final Integer displayOrder;
	private final List<ProductVariationResponse> variations;

	public ProductVariationGroupResponse(ProductVariationGroup group) {
		this.id = group.getId();
		this.title = group.getTitle();
		this.active = group.getActive();
		this.displayOrder = group.getDisplayOrder();
		this.variations = group.getVariations().stream()
				.sorted(Comparator.comparing(variation -> variation.getDisplayOrder() != null ? variation.getDisplayOrder() : 0))
				.map(ProductVariationResponse::new)
				.toList();
	}

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public Boolean getActive() {
		return active;
	}

	public Integer getDisplayOrder() {
		return displayOrder;
	}

	public List<ProductVariationResponse> getVariations() {
		return variations;
	}
}
