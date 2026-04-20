package com.br.food.response;

import java.math.BigDecimal;
import java.util.List;

import com.br.food.enums.Types.ProductType;
import com.br.food.models.Product;

public class ProductResponse {

	private final Long id;
	private final String code;
	private final String description;
	private final ProductType type;
	private final BigDecimal price;
	private final BigDecimal minimumStock;
	private final Boolean active;
	private final Boolean complement;
	private final Boolean visibleOnMenu;
	private final DocumentResponse image;
	private final List<ProductBasicResponse> complements;
	private final List<ProductCategoryResponse> categories;
	private final List<RecipeItemResponse> recipeItems;

	public ProductResponse(Product product) {
		this.id = product.getId();
		this.code = product.getCode();
		this.description = product.getDescription();
		this.type = product.getType();
		this.price = product.getPrice();
		this.minimumStock = product.getMinimumStock();
		this.active = product.getActive();
		this.complement = product.getComplement();
		this.visibleOnMenu = product.getVisibleOnMenu();
		this.image = product.getImage() != null ? new DocumentResponse(product.getImage()) : null;
		this.complements = product.getComplements().stream().map(ProductBasicResponse::new).toList();
		this.categories = product.getCategories().stream().map(ProductCategoryResponse::new).toList();
		this.recipeItems = product.getRecipeItems().stream().map(RecipeItemResponse::new).toList();
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

	public BigDecimal getMinimumStock() {
		return minimumStock;
	}

	public Boolean getActive() {
		return active;
	}

	public Boolean getComplement() {
		return complement;
	}

	public Boolean getVisibleOnMenu() {
		return visibleOnMenu;
	}

	public DocumentResponse getImage() {
		return image;
	}

	public List<ProductBasicResponse> getComplements() {
		return complements;
	}

	public List<ProductCategoryResponse> getCategories() {
		return categories;
	}

	public List<RecipeItemResponse> getRecipeItems() {
		return recipeItems;
	}
}
