package com.br.food.specification;

import org.springframework.data.jpa.domain.Specification;

import com.br.food.authentication.models.Role;
import com.br.food.models.User;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

public final class UserSpecification {

	private UserSpecification() {
	}

	/** Name, e-mail, login or phone contains the text, ignoring case. */
	public static Specification<User> search(String term) {
		if (term == null || term.isBlank()) {
			return Specification.where(null);
		}
		String like = "%" + term.trim().toLowerCase() + "%";
		return (root, query, builder) -> builder.or(builder.like(builder.lower(root.get("name")), like),
				builder.like(builder.lower(root.get("email")), like),
				builder.like(builder.lower(root.get("login")), like),
				builder.like(builder.lower(root.get("cellphoneCorporate")), like));
	}

	/** Holds the role, matched by name. */
	public static Specification<User> hasRole(String roleName) {
		if (roleName == null || roleName.isBlank()) {
			return Specification.where(null);
		}
		return (root, query, builder) -> {
			query.distinct(true);
			Join<User, Role> roles = root.join("roles", JoinType.INNER);
			return builder.equal(roles.get("name"), roleName);
		};
	}

	public static Specification<User> active(Boolean active) {
		if (active == null) {
			return Specification.where(null);
		}
		return (root, query, builder) -> active ? builder.isTrue(root.get("active"))
				: builder.or(builder.isFalse(root.get("active")), builder.isNull(root.get("active")));
	}
}
