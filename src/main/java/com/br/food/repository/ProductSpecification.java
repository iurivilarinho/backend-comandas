package com.br.food.repository;

import org.springframework.data.jpa.domain.Specification;

import com.br.food.enums.Types.ProductType;
import com.br.food.models.Product;

public final class ProductSpecification {

	private ProductSpecification() {
	}

	public static Specification<Product> hasCategoryId(Long categoryId) {
		return (root, query, criteriaBuilder) -> {
			if (categoryId == null) {
				return criteriaBuilder.conjunction();
			}
			query.distinct(true);
			return criteriaBuilder.equal(root.join("categories").get("id"), categoryId);
		};
	}

	public static Specification<Product> hasActive(Boolean active) {
		return (root, query, criteriaBuilder) -> active == null
				? criteriaBuilder.conjunction()
				: criteriaBuilder.equal(root.get("active"), active);
	}

	public static Specification<Product> hasVisibleOnMenu(Boolean visibleOnMenu) {
		return (root, query, criteriaBuilder) -> visibleOnMenu == null
				? criteriaBuilder.conjunction()
				: criteriaBuilder.equal(root.get("visibleOnMenu"), visibleOnMenu);
	}

	public static Specification<Product> hasType(ProductType type) {
		return (root, query, criteriaBuilder) -> type == null
				? criteriaBuilder.conjunction()
				: criteriaBuilder.equal(root.get("type"), type);
	}

	public static Specification<Product> hasComplement(Boolean complement) {
		return (root, query, criteriaBuilder) -> complement == null
				? criteriaBuilder.conjunction()
				: criteriaBuilder.equal(root.get("complement"), complement);
	}

	public static Specification<Product> search(String term) {
		return (root, query, criteriaBuilder) -> {
			if (term == null || term.isBlank()) {
				return criteriaBuilder.conjunction();
			}

			String likeTerm = "%" + term.trim().toLowerCase() + "%";
			return criteriaBuilder.or(
					criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), likeTerm),
					criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), likeTerm));
		};
	}
}
