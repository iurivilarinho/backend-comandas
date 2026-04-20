package com.br.food.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.br.food.models.Product;
import com.br.food.models.Promotion;

public class PromotionResponse {

	private final Long id;
	private final String title;
	private final String description;
	private final BigDecimal promotionPrice;
	private final BigDecimal oldPrice;
	private final BigDecimal newPrice;
	private final LocalDate expiresAt;
	private final Boolean active;
	private final Boolean expired;
	private final DocumentResponse image;
	private final List<ProductBasicResponse> products;

	public PromotionResponse(Promotion promotion) {
		this.id = promotion.getId();
		this.title = promotion.getTitle();
		this.description = promotion.getDescription();
		this.promotionPrice = promotion.getPromotionPrice();
		this.oldPrice = promotion.getOldPrice();
		this.newPrice = promotion.getNewPrice();
		this.expiresAt = promotion.getExpiresAt();
		this.active = promotion.getActive();
		this.expired = promotion.getExpiresAt() != null && promotion.getExpiresAt().isBefore(LocalDate.now());
		Product imageSource = promotion.getProducts().stream().findFirst().orElse(null);
		this.image = imageSource != null && imageSource.getImage() != null ? new DocumentResponse(imageSource.getImage()) : null;
		this.products = promotion.getProducts().stream().map(ProductBasicResponse::new).toList();
	}

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
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

	public Boolean getExpired() {
		return expired;
	}

	public DocumentResponse getImage() {
		return image;
	}

	public List<ProductBasicResponse> getProducts() {
		return products;
	}
}
