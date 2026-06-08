package com.br.food.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Consolidated profitability totals for the selected period")
public class ProductProfitSummaryResponse {

	private final int productCount;
	private final Long soldQuantity;
	private final BigDecimal totalRevenue;
	private final BigDecimal totalCost;
	private final BigDecimal totalProfit;
	private final BigDecimal averageMarginPercent;

	public ProductProfitSummaryResponse(int productCount, Long soldQuantity, BigDecimal totalRevenue,
			BigDecimal totalCost, BigDecimal totalProfit, BigDecimal averageMarginPercent) {
		this.productCount = productCount;
		this.soldQuantity = soldQuantity;
		this.totalRevenue = totalRevenue;
		this.totalCost = totalCost;
		this.totalProfit = totalProfit;
		this.averageMarginPercent = averageMarginPercent;
	}

	public int getProductCount() {
		return productCount;
	}

	public Long getSoldQuantity() {
		return soldQuantity;
	}

	public BigDecimal getTotalRevenue() {
		return totalRevenue;
	}

	public BigDecimal getTotalCost() {
		return totalCost;
	}

	public BigDecimal getTotalProfit() {
		return totalProfit;
	}

	public BigDecimal getAverageMarginPercent() {
		return averageMarginPercent;
	}
}
