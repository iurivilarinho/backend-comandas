package com.br.food.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;

import com.br.food.models.SupplyInvoice;

public final class SupplyInvoiceSpecification {

	private SupplyInvoiceSpecification() {
	}

	public static Specification<SupplyInvoice> hasInvoiceNumber(String invoiceNumber) {
		return (root, query, builder) -> invoiceNumber == null || invoiceNumber.isBlank()
				? builder.conjunction()
				: builder.like(builder.lower(root.get("invoiceNumber")), "%" + invoiceNumber.trim().toLowerCase() + "%");
	}

	public static Specification<SupplyInvoice> issueDateBetween(LocalDate startDate, LocalDate endDate) {
		return (root, query, builder) -> {
			if (startDate == null && endDate == null) {
				return builder.conjunction();
			}
			if (startDate != null && endDate != null) {
				return builder.between(root.get("issueDate"), startDate, endDate);
			}
			if (startDate != null) {
				return builder.greaterThanOrEqualTo(root.get("issueDate"), startDate);
			}
			return builder.lessThanOrEqualTo(root.get("issueDate"), endDate);
		};
	}

	public static Specification<SupplyInvoice> launchDateBetween(LocalDate startDate, LocalDate endDate) {
		return (root, query, builder) -> {
			if (startDate == null && endDate == null) {
				return builder.conjunction();
			}
			if (startDate != null && endDate != null) {
				return builder.between(root.get("launchDate"), startDate, endDate);
			}
			if (startDate != null) {
				return builder.greaterThanOrEqualTo(root.get("launchDate"), startDate);
			}
			return builder.lessThanOrEqualTo(root.get("launchDate"), endDate);
		};
	}
}
