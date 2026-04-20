package com.br.food.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.br.food.request.PromotionRequest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "promotions")
public class Promotion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "title", nullable = false, length = 120)
	private String title;

	@Column(name = "description", length = 500)
	private String description;

	@Column(name = "promotion_price", nullable = false, precision = 12, scale = 2)
	private BigDecimal promotionPrice;

	@Column(name = "old_price", precision = 12, scale = 2)
	private BigDecimal oldPrice;

	@Column(name = "new_price", precision = 12, scale = 2)
	private BigDecimal newPrice;

	@Column(name = "expires_at", nullable = false)
	private LocalDate expiresAt;

	@Column(name = "active", nullable = false)
	private Boolean active;

	@ManyToMany
	@JoinTable(name = "promotion_products", joinColumns = @JoinColumn(name = "fk_promotion_id"),
			inverseJoinColumns = @JoinColumn(name = "fk_product_id"))
	private List<Product> products = new ArrayList<>();

	@Column(name = "created_at", updatable = false, nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	public Promotion() {
	}

	public Promotion(PromotionRequest request) {
		this.title = request.getTitle();
		this.description = request.getDescription();
		this.promotionPrice = request.getPromotionPrice();
		this.oldPrice = request.getOldPrice();
		this.newPrice = request.getNewPrice();
		this.expiresAt = request.getExpiresAt();
		this.active = request.getActive() != null ? request.getActive() : Boolean.TRUE;
	}

	public void update(PromotionRequest request) {
		this.title = request.getTitle();
		this.description = request.getDescription();
		this.promotionPrice = request.getPromotionPrice();
		this.oldPrice = request.getOldPrice();
		this.newPrice = request.getNewPrice();
		this.expiresAt = request.getExpiresAt();
		this.active = request.getActive() != null ? request.getActive() : this.active;
	}

	@PrePersist
	private void prePersist() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	private void preUpdate() {
		this.updatedAt = LocalDateTime.now();
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

	public void setActive(Boolean active) {
		this.active = active;
	}

	public List<Product> getProducts() {
		return products;
	}

	public void setProducts(List<Product> products) {
		this.products = products;
	}
}
