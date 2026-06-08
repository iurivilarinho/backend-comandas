package com.br.food.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.enums.Types.OrderItemStatus;
import com.br.food.enums.Types.OrderStatus;
import com.br.food.enums.Types.ProductType;
import com.br.food.models.Product;
import com.br.food.repository.OrderItemRepository;
import com.br.food.repository.ProductRepository;
import com.br.food.repository.ProductSpecification;
import com.br.food.repository.projection.ProductSalesAggregationProjection;
import com.br.food.response.ProductProfitExcelRow;
import com.br.food.response.ProductProfitOverviewResponse;
import com.br.food.response.ProductProfitResponse;
import com.br.food.response.ProductProfitSummaryResponse;
import com.br.food.util.excel.GeneratorExcel;

@Service
public class ProfitabilityService {

	private static final List<OrderItemStatus> EXCLUDED_ITEM_STATUSES = List.of(OrderItemStatus.DECLINED,
			OrderItemStatus.CANCELED);
	private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

	private final ProductRepository productRepository;
	private final OrderItemRepository orderItemRepository;
	private final GeneratorExcel generatorExcel;

	public ProfitabilityService(ProductRepository productRepository, OrderItemRepository orderItemRepository,
			GeneratorExcel generatorExcel) {
		this.productRepository = productRepository;
		this.orderItemRepository = orderItemRepository;
		this.generatorExcel = generatorExcel;
	}

	@Transactional(readOnly = true)
	public ProductProfitOverviewResponse overview(LocalDate startDate, LocalDate endDate, Long categoryId,
			String term) {
		List<ProductProfitResponse> products = buildRows(startDate, endDate, categoryId, term);
		return new ProductProfitOverviewResponse(buildSummary(products), products);
	}

	@Transactional(readOnly = true)
	public byte[] exportReport(LocalDate startDate, LocalDate endDate, Long categoryId, String term) throws Exception {
		List<ProductProfitResponse> products = buildRows(startDate, endDate, categoryId, term);
		List<ProductProfitExcelRow> rows = products.stream().map(this::toExcelRow).toList();
		return generatorExcel.gerar(rows).toByteArray();
	}

	private List<ProductProfitResponse> buildRows(LocalDate startDate, LocalDate endDate, Long categoryId,
			String term) {
		Map<Long, ProductSalesAggregationProjection> salesByProduct = loadSalesByProduct(startDate, endDate);

		Specification<Product> specification = Specification.where(ProductSpecification.hasActive(true))
				.and(ProductSpecification.hasType(ProductType.FINISHED))
				.and(ProductSpecification.hasCategoryId(categoryId)).and(ProductSpecification.search(term));

		return productRepository.findAll(specification).stream().map(product -> buildRow(product,
				salesByProduct.get(product.getId()))).sorted(profitComparator()).toList();
	}

	private Map<Long, ProductSalesAggregationProjection> loadSalesByProduct(LocalDate startDate, LocalDate endDate) {
		LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
		LocalDateTime endDateTime = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

		Map<Long, ProductSalesAggregationProjection> salesByProduct = new HashMap<>();
		for (ProductSalesAggregationProjection aggregation : orderItemRepository.aggregateSalesByProduct(
				OrderStatus.CLOSED, EXCLUDED_ITEM_STATUSES, startDateTime, endDateTime)) {
			salesByProduct.put(aggregation.getProductId(), aggregation);
		}
		return salesByProduct;
	}

	private ProductProfitResponse buildRow(Product product, ProductSalesAggregationProjection sales) {
		BigDecimal price = scale(product.getPrice());
		BigDecimal costPrice = product.getCostPrice() != null ? scale(product.getCostPrice()) : null;

		BigDecimal unitProfit = costPrice != null ? price.subtract(costPrice) : null;
		BigDecimal marginPercent = marginPercent(price, unitProfit);

		long soldQuantity = sales != null && sales.getSoldQuantity() != null ? sales.getSoldQuantity() : 0L;
		BigDecimal salesRevenue = sales != null && sales.getSalesRevenue() != null ? scale(sales.getSalesRevenue())
				: BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		BigDecimal salesCost = costPrice != null
				? scale(costPrice.multiply(BigDecimal.valueOf(soldQuantity)))
				: null;
		BigDecimal salesProfit = salesCost != null ? salesRevenue.subtract(salesCost) : null;

		return new ProductProfitResponse(product.getId(), product.getCode(), product.getDescription(), price, costPrice,
				unitProfit, marginPercent, soldQuantity, salesRevenue, salesCost, salesProfit);
	}

	private BigDecimal marginPercent(BigDecimal price, BigDecimal unitProfit) {
		if (unitProfit == null || price == null || price.compareTo(BigDecimal.ZERO) == 0) {
			return null;
		}
		return unitProfit.multiply(ONE_HUNDRED).divide(price, 2, RoundingMode.HALF_UP);
	}

	private Comparator<ProductProfitResponse> profitComparator() {
		return Comparator
				.comparing((ProductProfitResponse row) -> row.getSalesProfit() != null ? row.getSalesProfit()
						: BigDecimal.valueOf(Long.MIN_VALUE))
				.reversed();
	}

	private ProductProfitSummaryResponse buildSummary(List<ProductProfitResponse> products) {
		long soldQuantity = 0L;
		BigDecimal totalRevenue = BigDecimal.ZERO;
		BigDecimal totalCost = BigDecimal.ZERO;

		for (ProductProfitResponse product : products) {
			soldQuantity += product.getSoldQuantity() != null ? product.getSoldQuantity() : 0L;
			totalRevenue = totalRevenue.add(product.getSalesRevenue() != null ? product.getSalesRevenue()
					: BigDecimal.ZERO);
			totalCost = totalCost.add(product.getSalesCost() != null ? product.getSalesCost() : BigDecimal.ZERO);
		}

		BigDecimal totalProfit = totalRevenue.subtract(totalCost);
		BigDecimal averageMarginPercent = totalRevenue.compareTo(BigDecimal.ZERO) > 0
				? totalProfit.multiply(ONE_HUNDRED).divide(totalRevenue, 2, RoundingMode.HALF_UP)
				: null;

		return new ProductProfitSummaryResponse(products.size(), soldQuantity, scale(totalRevenue), scale(totalCost),
				scale(totalProfit), averageMarginPercent);
	}

	private ProductProfitExcelRow toExcelRow(ProductProfitResponse product) {
		return new ProductProfitExcelRow(product.getCode(), product.getDescription(), text(product.getPrice()),
				text(product.getCostPrice()), text(product.getUnitProfit()), text(product.getMarginPercent()),
				String.valueOf(product.getSoldQuantity() != null ? product.getSoldQuantity() : 0L),
				text(product.getSalesRevenue()), text(product.getSalesCost()), text(product.getSalesProfit()));
	}

	private String text(BigDecimal value) {
		return value != null ? value.toString() : "-";
	}

	private BigDecimal scale(BigDecimal value) {
		return value != null ? value.setScale(2, RoundingMode.HALF_UP)
				: BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
	}
}
