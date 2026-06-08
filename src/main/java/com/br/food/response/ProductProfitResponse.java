package com.br.food.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Profitability metrics for a single product over a period")
public class ProductProfitResponse {

	private final Long productId;
	private final String code;
	private final String description;
	private final BigDecimal price;
	private final BigDecimal costPrice;
	private final BigDecimal unitProfit;
	private final BigDecimal marginPercent;
	private final Long soldQuantity;
	private final BigDecimal salesRevenue;
	private final BigDecimal salesCost;
	private final BigDecimal salesProfit;

	public ProductProfitResponse(Long productId, String code, String description, BigDecimal price,
			BigDecimal costPrice, BigDecimal unitProfit, BigDecimal marginPercent, Long soldQuantity,
			BigDecimal salesRevenue, BigDecimal salesCost, BigDecimal salesProfit) {
		this.productId = productId;
		this.code = code;
		this.description = description;
		this.price = price;
		this.costPrice = costPrice;
		this.unitProfit = unitProfit;
		this.marginPercent = marginPercent;
		this.soldQuantity = soldQuantity;
		this.salesRevenue = salesRevenue;
		this.salesCost = salesCost;
		this.salesProfit = salesProfit;
	}

	public Long getProductId() {
		return productId;
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public BigDecimal getCostPrice() {
		return costPrice;
	}

	public BigDecimal getUnitProfit() {
		return unitProfit;
	}

	public BigDecimal getMarginPercent() {
		return marginPercent;
	}

	public Long getSoldQuantity() {
		return soldQuantity;
	}

	public BigDecimal getSalesRevenue() {
		return salesRevenue;
	}

	public BigDecimal getSalesCost() {
		return salesCost;
	}

	public BigDecimal getSalesProfit() {
		return salesProfit;
	}
}
