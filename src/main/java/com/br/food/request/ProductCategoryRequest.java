package com.br.food.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Product category create or update payload")
public class ProductCategoryRequest {

	@NotBlank(message = "Category name is required.")
	@Size(min = 2, max = 100, message = "Category name must have between 2 and 100 characters.")
	private String name;

	public String getName() {
		return name != null ? name.trim() : null;
	}
}
