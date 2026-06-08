package com.br.food.response;

import com.br.food.util.excel.ExcelColumn;

public class ProductProfitExcelRow {

	@ExcelColumn("Código")
	private final String code;

	@ExcelColumn("Produto")
	private final String description;

	@ExcelColumn("Preço")
	private final String price;

	@ExcelColumn("Custo")
	private final String costPrice;

	@ExcelColumn("Lucro unitário")
	private final String unitProfit;

	@ExcelColumn("Margem %")
	private final String marginPercent;

	@ExcelColumn("Qtd. vendida")
	private final String soldQuantity;

	@ExcelColumn("Receita")
	private final String salesRevenue;

	@ExcelColumn("Custo total")
	private final String salesCost;

	@ExcelColumn("Lucro em vendas")
	private final String salesProfit;

	public ProductProfitExcelRow(String code, String description, String price, String costPrice, String unitProfit,
			String marginPercent, String soldQuantity, String salesRevenue, String salesCost, String salesProfit) {
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
}
