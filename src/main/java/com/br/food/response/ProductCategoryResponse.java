package com.br.food.response;

import com.br.food.models.ProductCategory;

public class ProductCategoryResponse {

	private final Long id;
	private final String name;
	private final Boolean active;

	public ProductCategoryResponse(ProductCategory category) {
		this.id = category.getId();
		this.name = category.getName();
		this.active = category.getActive();
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Boolean getActive() {
		return active;
	}
}
