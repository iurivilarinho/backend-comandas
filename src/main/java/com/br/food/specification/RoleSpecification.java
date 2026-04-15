package com.br.food.specification;

import org.springframework.data.jpa.domain.Specification;

import com.br.food.authentication.models.Role;

public class RoleSpecification {

	public static Specification<Role> active(Boolean active) {
		if (active == null) {
			return Specification.where(null);
		}
		return (root, query, builder) -> builder.equal(root.get("active"), active);
	}
}
