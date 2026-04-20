package com.br.food.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.enums.Types.FinanceCategory;
import com.br.food.enums.Types.FinanceEntryType;
import com.br.food.enums.Types.FinanceOrigin;
import com.br.food.enums.Types.PaymentMethod;
import com.br.food.models.FinancialEntry;
import com.br.food.models.Order;
import com.br.food.models.OrderPayment;
import com.br.food.repository.FinancialEntryRepository;
import com.br.food.repository.OrderPaymentRepository;
import com.br.food.request.FinancialEntryRequest;
import com.br.food.response.FinancialCategoryTotalResponse;
import com.br.food.response.FinancialDailySummaryResponse;
import com.br.food.response.FinancialBreakdownResponse;
import com.br.food.response.FinancialEntryResponse;
import com.br.food.response.FinancialOverviewResponse;
import com.br.food.response.FinancialPaymentMethodTotalResponse;
import com.br.food.response.FinancialReportExcelRow;
import com.br.food.response.FinancialReportSummaryResponse;
import com.br.food.util.excel.GeneratorExcel;

@Service
public class FinancialService {

	private static final DateTimeFormatter EXCEL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	private final FinancialEntryRepository financialEntryRepository;
	private final OrderPaymentRepository orderPaymentRepository;
	private final AuditLogService auditLogService;
	private final OrderService orderService;
	private final GeneratorExcel generatorExcel;

	public FinancialService(
			FinancialEntryRepository financialEntryRepository,
			OrderPaymentRepository orderPaymentRepository,
			AuditLogService auditLogService,
			OrderService orderService,
			GeneratorExcel generatorExcel) {
		this.financialEntryRepository = financialEntryRepository;
		this.orderPaymentRepository = orderPaymentRepository;
		this.auditLogService = auditLogService;
		this.orderService = orderService;
		this.generatorExcel = generatorExcel;
	}

	@Transactional
	public FinancialEntryResponse create(FinancialEntryRequest request, String actorName) {
		FinancialEntry entry = new FinancialEntry();
		entry.setType(request.getType());
		entry.setCategory(request.getCategory());
		entry.setPaymentMethod(request.getPaymentMethod());
		entry.setDescription(request.getDescription().trim());
		entry.setNotes(request.getNotes() != null && !request.getNotes().isBlank() ? request.getNotes().trim() : null);
		entry.setAmount(request.getAmount().setScale(2, RoundingMode.HALF_UP));
		entry.setOccurredAt(request.getOccurredAt() != null ? request.getOccurredAt() : LocalDateTime.now());

		FinancialEntry savedEntry = financialEntryRepository.save(entry);
		auditLogService.register(
				"FinancialEntry",
				savedEntry.getId(),
				"FINANCIAL_ENTRY_CREATED",
				actorName,
				savedEntry.getType() + " " + savedEntry.getCategory() + " " + savedEntry.getAmount());
		return new FinancialEntryResponse(savedEntry);
	}

	@Transactional(readOnly = true)
	public FinancialOverviewResponse overview(
			LocalDate startDate,
			LocalDate endDate,
			FinanceEntryType type,
			FinanceCategory category) {
		List<FinancialEntryResponse> entries = buildEntries(startDate, endDate, type, category);
		return buildOverview(startDate, endDate, type, category, entries);
	}

	@Transactional(readOnly = true)
	public byte[] exportReport(
			LocalDate startDate,
			LocalDate endDate,
			FinanceEntryType type,
			FinanceCategory category) throws Exception {
		List<FinancialEntryResponse> entries = buildEntries(startDate, endDate, type, category);
		FinancialOverviewResponse overview = buildOverview(startDate, endDate, type, category, entries);

		List<FinancialReportSummaryResponse> summaryRows = List.of(new FinancialReportSummaryResponse(
				startDate,
				endDate,
				type,
				category,
				overview.getTotalIncome(),
				overview.getTotalExpense(),
				overview.getBalance(),
				entries.size()));
		List<FinancialReportExcelRow> movementRows = entries.stream()
				.map(this::toExcelRow)
				.toList();

		Map<String, List<?>> sheets = new LinkedHashMap<>();
		sheets.put("Resumo", summaryRows);
		if (!movementRows.isEmpty()) {
			sheets.put("Movimentacoes", movementRows);
		}

		return generatorExcel.gerarAbas(sheets).toByteArray();
	}

	private List<FinancialEntryResponse> buildEntries(
			LocalDate startDate,
			LocalDate endDate,
			FinanceEntryType type,
			FinanceCategory category) {
		LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
		LocalDateTime endDateTime = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

		List<FinancialEntryResponse> entries = new ArrayList<>();

		for (FinancialEntry entry : financialEntryRepository.findAll()) {
			if (!matches(entry.getOccurredAt(), startDateTime, endDateTime)) {
				continue;
			}
			if (type != null && entry.getType() != type) {
				continue;
			}
			if (category != null && entry.getCategory() != category) {
				continue;
			}
			entries.add(new FinancialEntryResponse(entry));
		}

		for (OrderPayment payment : orderPaymentRepository.findAll()) {
			if (!matches(payment.getRecordedAt(), startDateTime, endDateTime)) {
				continue;
			}
			if (type != null && type != FinanceEntryType.INCOME) {
				continue;
			}
			if (category != null && category != FinanceCategory.PRODUCTS) {
				continue;
			}
			entries.add(buildOrderPaymentEntry(payment));
		}

		entries.sort(Comparator.comparing(FinancialEntryResponse::getOccurredAt).reversed());
		return entries;
	}

	private FinancialOverviewResponse buildOverview(
			LocalDate startDate,
			LocalDate endDate,
			FinanceEntryType type,
			FinanceCategory category,
			List<FinancialEntryResponse> entries) {
		BigDecimal totalIncome = entries.stream()
				.filter(entry -> entry.getType() == FinanceEntryType.INCOME)
				.map(FinancialEntryResponse::getAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add)
				.setScale(2, RoundingMode.HALF_UP);

		BigDecimal totalExpense = entries.stream()
				.filter(entry -> entry.getType() == FinanceEntryType.EXPENSE)
				.map(FinancialEntryResponse::getAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add)
				.setScale(2, RoundingMode.HALF_UP);

		Map<LocalDate, BigDecimal> incomeByDay = new TreeMap<>();
		Map<LocalDate, BigDecimal> expenseByDay = new TreeMap<>();
		Map<String, BigDecimal> categoryTotals = new LinkedHashMap<>();
		Map<PaymentMethod, BigDecimal> paymentMethodTotals = new LinkedHashMap<>();

		LocalDate effectiveStartDate = startDate != null ? startDate : entries.stream()
				.map(FinancialEntryResponse::getOccurredAt)
				.filter(value -> value != null)
				.map(LocalDateTime::toLocalDate)
				.min(LocalDate::compareTo)
				.orElse(LocalDate.now().minusDays(6));
		LocalDate effectiveEndDate = endDate != null ? endDate : entries.stream()
				.map(FinancialEntryResponse::getOccurredAt)
				.filter(value -> value != null)
				.map(LocalDateTime::toLocalDate)
				.max(LocalDate::compareTo)
				.orElse(LocalDate.now());

		for (FinancialEntryResponse entry : entries) {
			LocalDate date = entry.getOccurredAt().toLocalDate();
			if (entry.getType() == FinanceEntryType.INCOME) {
				incomeByDay.merge(date, entry.getAmount(), BigDecimal::add);
			} else {
				expenseByDay.merge(date, entry.getAmount(), BigDecimal::add);
			}

			String categoryKey = entry.getType().name() + "-" + entry.getCategory().name();
			categoryTotals.merge(categoryKey, entry.getAmount(), BigDecimal::add);

			if (entry.getPaymentMethod() != null) {
				paymentMethodTotals.merge(entry.getPaymentMethod(), entry.getAmount(), BigDecimal::add);
			}
		}

		List<FinancialDailySummaryResponse> dailySummary = new ArrayList<>();
		for (LocalDate date = effectiveStartDate; !date.isAfter(effectiveEndDate); date = date.plusDays(1)) {
			BigDecimal dailyIncome = incomeByDay.getOrDefault(date, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
			BigDecimal dailyExpense = expenseByDay.getOrDefault(date, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
			dailySummary.add(new FinancialDailySummaryResponse(
					date,
					dailyIncome,
					dailyExpense,
					dailyIncome.subtract(dailyExpense).setScale(2, RoundingMode.HALF_UP)));
		}

		List<FinancialCategoryTotalResponse> categorySummary = categoryTotals.entrySet().stream()
				.map(entry -> {
					String[] parts = entry.getKey().split("-");
					return new FinancialCategoryTotalResponse(
							FinanceEntryType.valueOf(parts[0]),
							FinanceCategory.valueOf(parts[1]),
							entry.getValue().setScale(2, RoundingMode.HALF_UP));
				})
				.sorted(Comparator.comparing(FinancialCategoryTotalResponse::getType)
						.thenComparing(FinancialCategoryTotalResponse::getCategory))
				.toList();

		List<FinancialPaymentMethodTotalResponse> paymentSummary = paymentMethodTotals.entrySet().stream()
				.map(entry -> new FinancialPaymentMethodTotalResponse(
						entry.getKey(),
						entry.getValue().setScale(2, RoundingMode.HALF_UP)))
				.sorted(Comparator.comparing(FinancialPaymentMethodTotalResponse::getPaymentMethod))
				.toList();

		return new FinancialOverviewResponse(
				totalIncome,
				totalExpense,
				totalIncome.subtract(totalExpense).setScale(2, RoundingMode.HALF_UP),
				entries,
				dailySummary,
				categorySummary,
				paymentSummary);
	}

	private FinancialEntryResponse buildOrderPaymentEntry(OrderPayment payment) {
		Order order = payment.getOrder();
		BigDecimal grossProducts = orderService.calculateItemsTotal(order).setScale(2, RoundingMode.HALF_UP);
		BigDecimal discountAmount = grossProducts.subtract(order.getSubtotalAmount()).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
		BigDecimal totalAmount = order.getTotalAmount() != null && order.getTotalAmount().compareTo(BigDecimal.ZERO) > 0
				? order.getTotalAmount()
				: payment.getAmount();
		BigDecimal ratio = payment.getAmount().divide(totalAmount, 6, RoundingMode.HALF_UP);

		List<FinancialBreakdownResponse> breakdown = new ArrayList<>();
		addBreakdownLine(breakdown, "Produtos", grossProducts.multiply(ratio));
		addBreakdownLine(breakdown, "Descontos", discountAmount.multiply(ratio).negate());
		addBreakdownLine(breakdown, "Taxa de servico", order.getServiceFeeAmount().multiply(ratio));
		addBreakdownLine(breakdown, "Couvert", order.getCoverChargeAmount().multiply(ratio));

		return new FinancialEntryResponse(
				"order-payment-" + payment.getId(),
				FinanceOrigin.ORDER,
				FinanceEntryType.INCOME,
				FinanceCategory.PRODUCTS,
				"Pedido " + order.getCode() + " - " + (order.getCustomer() != null ? order.getCustomer().getName() : "sem cliente"),
				order.getCode(),
				order.getCheckoutRequestNotes(),
				payment.getAmount(),
				payment.getPaymentMethod(),
				payment.getRecordedAt(),
				breakdown);
	}

	private void addBreakdownLine(List<FinancialBreakdownResponse> breakdown, String label, BigDecimal amount) {
		BigDecimal scaledAmount = amount.setScale(2, RoundingMode.HALF_UP);
		if (scaledAmount.compareTo(BigDecimal.ZERO) == 0) {
			return;
		}
		breakdown.add(new FinancialBreakdownResponse(label, scaledAmount));
	}

	private boolean matches(LocalDateTime occurredAt, LocalDateTime startDateTime, LocalDateTime endDateTime) {
		if (occurredAt == null) {
			return false;
		}
		if (startDateTime != null && occurredAt.isBefore(startDateTime)) {
			return false;
		}
		if (endDateTime != null && occurredAt.isAfter(endDateTime)) {
			return false;
		}
		return true;
	}

	private FinancialReportExcelRow toExcelRow(FinancialEntryResponse entry) {
		String breakdown = entry.getBreakdown().stream()
				.map(item -> item.getLabel() + ": " + item.getAmount().setScale(2, RoundingMode.HALF_UP))
				.reduce((left, right) -> left + " | " + right)
				.orElse("-");

		return new FinancialReportExcelRow(
				entry.getOccurredAt() != null ? entry.getOccurredAt().format(EXCEL_DATE_TIME_FORMATTER) : "-",
				entry.getOrigin().name(),
				entry.getType().name(),
				entry.getCategory().name(),
				entry.getDescription(),
				entry.getReferenceCode(),
				entry.getPaymentMethod() != null ? entry.getPaymentMethod().name() : "-",
				entry.getAmount().setScale(2, RoundingMode.HALF_UP).toString(),
				breakdown,
				entry.getNotes());
	}
}
