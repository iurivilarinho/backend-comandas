package com.br.food.platform;

import com.br.food.authentication.models.Role;
import com.br.food.enums.Types.StaffRole;
import com.br.food.models.User;

/** Users holding a staff role, for tests that put a signed-in user in the security context. */
public final class TestUsers {

	private TestUsers() {
	}

	public static User with(StaffRole staffRole) {
		Role role = new Role();
		role.setName(staffRole.authority());
		User user = new User();
		user.getRoles().add(role);
		return user;
	}
}
