package com.br.food.config;

import java.util.Arrays;
import java.util.Collection;

import com.br.food.enums.Types.StaffRole;

/**
 * Which roles may do what (CMD-017). One place, read by the security chain and by the checks that guard the customer
 * routes, so the matrix of the documentation and the code cannot drift apart.
 *
 * <ul>
 * <li>{@link #ADMIN_ONLY}: users, roles and permissions.</li>
 * <li>{@link #MANAGEMENT}: catalog, stock, financial, profitability, tables setup, company profile and settings.</li>
 * <li>{@link #CASHIER_UP}: closing the bill, reopening and cancelling orders.</li>
 * <li>{@link #FRONT_OF_HOUSE}: orders, tables, customers and delivery (everyone but the kitchen).</li>
 * <li>{@link #KITCHEN_UP}: the preparation queue.</li>
 * <li>{@link #ALL_STAFF}: any staff role.</li>
 * </ul>
 */
public final class StaffAuthorities {

	public static final String[] ADMIN_ONLY = authorities(StaffRole.ADMIN);
	public static final String[] MANAGEMENT = authorities(StaffRole.ADMIN, StaffRole.MANAGER);
	public static final String[] CASHIER_UP = authorities(StaffRole.ADMIN, StaffRole.MANAGER, StaffRole.CASHIER);
	public static final String[] FRONT_OF_HOUSE = authorities(StaffRole.ADMIN, StaffRole.MANAGER, StaffRole.CASHIER,
			StaffRole.WAITER);
	public static final String[] KITCHEN_UP = authorities(StaffRole.ADMIN, StaffRole.MANAGER, StaffRole.KITCHEN);
	public static final String[] ALL_STAFF = authorities(StaffRole.values());

	private StaffAuthorities() {
	}

	/** True when at least one of the granted authorities is in the group. */
	public static boolean holdsAny(Collection<String> granted, String[] group) {
		return Arrays.stream(group).anyMatch(granted::contains);
	}

	private static String[] authorities(StaffRole... roles) {
		return Arrays.stream(roles).map(StaffRole::authority).toArray(String[]::new);
	}
}
