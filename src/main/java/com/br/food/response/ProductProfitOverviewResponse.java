package com.br.food.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Product profitability overview: per-product rows plus consolidated summary")
public class ProductProfitOverviewResponse {

	private final ProductProfitSummaryResponse summary;
	private final List<ProductProfitResponse> products;

	public ProductProfitOverviewResponse(ProductProfitSummaryResponse summary, List<ProductProfitResponse> products) {
		this.summary = summary;
		this.products = products;
	}

	public ProductProfitSummaryResponse getSummary() {
		return summary;
	}

	public List<ProductProfitResponse> getProducts() {
		return products;
	}
}
