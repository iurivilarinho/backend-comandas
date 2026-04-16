package com.br.food.authentication.specification;

import org.springframework.data.jpa.domain.Specification;

import com.br.food.authentication.models.PermissionResource;

public class PermissionResourceSpecification {

	public static Specification<PermissionResource> resourceIdEquals(Long resourceId) {
		if (resourceId == null) {
			return Specification.where(null);
		}
		return (root, query, builder) -> builder.equal(root.get("resource").get("id"), resourceId);
	}

}
