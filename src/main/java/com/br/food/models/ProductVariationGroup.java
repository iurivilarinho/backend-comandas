package com.br.food.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.br.food.request.ProductVariationGroupRequest;
import com.br.food.request.ProductVariationRequest;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_variation_group")
public class ProductVariationGroup {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_product_id", foreignKey = @ForeignKey(name = "fk_product_variation_group_product"))
	private Product product;

	@Column(name = "title", nullable = false, length = 60)
	private String title;

	@Column(name = "active", nullable = false)
	private Boolean active;

	@Column(name = "display_order", nullable = false)
	private Integer displayOrder;

	@OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ProductVariation> variations = new ArrayList<>();

	@Column(name = "created_at", updatable = false, nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	public ProductVariationGroup() {
	}

	public ProductVariationGroup(Product product, ProductVariationGroupRequest request, Integer displayOrder) {
		this.product = product;
		this.title = request.getTitle().trim();
		this.active = request.getResolvedActive();
		this.displayOrder = displayOrder;
		replaceVariations(request.getVariations());
	}

	public void update(ProductVariationGroupRequest request, Integer displayOrder) {
		this.title = request.getTitle().trim();
		this.active = request.getResolvedActive();
		this.displayOrder = displayOrder;
		replaceVariations(request.getVariations());
	}

	public void replaceVariations(List<ProductVariationRequest> variationRequests) {
		this.variations.clear();
		if (variationRequests == null) {
			return;
		}

		for (int index = 0; index < variationRequests.size(); index++) {
			ProductVariation variation = new ProductVariation(this, variationRequests.get(index), index);
			this.variations.add(variation);
		}
		this.variations.sort(Comparator.comparing(ProductVariation::getDisplayOrder));
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

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
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

	public List<ProductVariation> getVariations() {
		return variations;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (!(object instanceof ProductVariationGroup other)) {
			return false;
		}
		return id != null && Objects.equals(id, other.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}
}
