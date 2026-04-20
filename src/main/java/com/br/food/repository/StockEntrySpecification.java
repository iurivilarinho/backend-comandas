package com.br.food.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;

import com.br.food.models.StockEntry;

public final class StockEntrySpecification {

	private StockEntrySpecification() {
	}

	public static Specification<StockEntry> hasProductCode(String productCode) {
		if (productCode == null || productCode.isBlank()) {
			return Specification.where(null);
		}
		return (root, query, builder) -> builder.equal(root.get("product").get("code"), productCode);
	}

	public static Specification<StockEntry> search(String term) {
		if (term == null || term.isBlank()) {
			return Specification.where(null);
		}
		String likeTerm = "%" + term.toLowerCase() + "%";
		return (root, query, builder) -> builder.or(
				builder.like(builder.lower(root.get("batch")), likeTerm),
				builder.like(builder.lower(root.get("product").get("description")), likeTerm),
				builder.like(builder.lower(root.get("product").get("code")), likeTerm));
	}

	public static Specification<StockEntry> manufacturingDateBetween(LocalDate startDate, LocalDate endDate) {
		return (root, query, builder) -> {
			if (startDate == null && endDate == null) {
				return builder.conjunction();
			}
			if (startDate != null && endDate != null) {
				return builder.between(root.get("manufacturingDate"), startDate, endDate);
			}
			if (startDate != null) {
				return builder.greaterThanOrEqualTo(root.get("manufacturingDate"), startDate);
			}
			return builder.lessThanOrEqualTo(root.get("manufacturingDate"), endDate);
		};
	}

	public static Specification<StockEntry> expirationDateBetween(LocalDate startDate, LocalDate endDate) {
		return (root, query, builder) -> {
			if (startDate == null && endDate == null) {
				return builder.conjunction();
			}
			if (startDate != null && endDate != null) {
				return builder.between(root.get("expirationDate"), startDate, endDate);
			}
			if (startDate != null) {
				return builder.greaterThanOrEqualTo(root.get("expirationDate"), startDate);
			}
			return builder.lessThanOrEqualTo(root.get("expirationDate"), endDate);
		};
	}
}
