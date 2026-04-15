package com.br.food.models;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "recipe_items")
public class RecipeItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_final_product_id", foreignKey = @ForeignKey(name = "fk_recipe_item_final_product"))
	private Product finalProduct;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_ingredient_product_id", foreignKey = @ForeignKey(name = "fk_recipe_item_ingredient_product"))
	private Product ingredientProduct;

	@Column(name = "quantity", nullable = false, precision = 12, scale = 3)
	private BigDecimal quantity;

	public RecipeItem() {
	}

	public RecipeItem(Product finalProduct, Product ingredientProduct, BigDecimal quantity) {
		this.finalProduct = finalProduct;
		this.ingredientProduct = ingredientProduct;
		this.quantity = quantity;
	}

	public Long getId() {
		return id;
	}

	public Product getFinalProduct() {
		return finalProduct;
	}

	public Product getIngredientProduct() {
		return ingredientProduct;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}
}
