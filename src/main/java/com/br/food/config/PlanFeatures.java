package com.br.food.config;

/**
 * Codes of the plan limits and features this app reads through {@code Entitlements} (CMD-016). They must be
 * registered under the same names in the Central, on the product {@code comandas}.
 */
public final class PlanFeatures {

	/** Limit: dining tables. */
	public static final String TABLES_MAX = "tables.max";

	/** Limit: products of the menu. */
	public static final String PRODUCTS_MAX = "products.max";

	/** Limit: staff users. */
	public static final String USERS_MAX = "users.max";

	/** Feature: delivery and takeaway orders. */
	public static final String DELIVERY = "delivery";

	/** Feature: financial module and product profitability. */
	public static final String FINANCIAL = "financial";

	private PlanFeatures() {
	}
}
