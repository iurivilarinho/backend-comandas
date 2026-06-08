package com.br.food.repository.projection;

import java.math.BigDecimal;

public interface ProductSalesAggregationProjection {

	Long getProductId();

	Long getSoldQuantity();

	BigDecimal getSalesRevenue();
}
