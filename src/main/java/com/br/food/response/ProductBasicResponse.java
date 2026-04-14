package com.br.food.response;

import java.math.BigDecimal;

import com.br.food.enums.Types.ProductType;
import com.br.food.models.Product;

public class ProductBasicResponse {

	private final Long id;
	private final String code;
	private final String description;
	private final ProductType type;
	private final BigDecimal price;

	public ProductBasicResponse(Product product) {
		this.id = product.getId();
		this.code = product.getCode();
		this.description = product.getDescription();
		this.type = product.getType();
		this.price = product.getPrice();
	}

	public Long getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

	public ProductType getType() {
		return type;
	}

	public BigDecimal getPrice() {
		return price;
	}
}
