package com.br.food.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;

import com.br.food.models.Promotion;

public final class PromotionSpecification {

	private PromotionSpecification() {
	}

	public static Specification<Promotion> hasActive(Boolean active) {
		return (root, query, criteriaBuilder) -> active == null
				? criteriaBuilder.conjunction()
				: criteriaBuilder.equal(root.get("active"), active);
	}

	public static Specification<Promotion> notExpired(Boolean onlyValid) {
		return (root, query, criteriaBuilder) -> Boolean.TRUE.equals(onlyValid)
				? criteriaBuilder.greaterThanOrEqualTo(root.get("expiresAt"), LocalDate.now())
				: criteriaBuilder.conjunction();
	}
}
