package com.br.food.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import com.br.food.repository.FinancialStatementRepository;
import com.br.food.repository.OrderPaymentRepository;
import com.br.food.repository.projection.FinancialStatementProjection;
import com.br.food.request.FinancialEntryRequest;
import com.br.food.response.FinancialBreakdownResponse;
import com.br.food.response.FinancialCategoryTotalResponse;
import com.br.food.response.FinancialDailySummaryResponse;
import com.br.food.response.FinancialEntryResponse;
import com.br.food.response.FinancialOverviewResponse;
import com.br.food.response.FinancialPaymentMethodTotalResponse;
import com.br.food.response.FinancialReportExcelRow;
import com.br.food.response.FinancialReportSummaryResponse;
import com.br.food.response.PageMetadataResponse;
import com.br.food.util.FinancialLabelUtils;
import com.br.food.util.excel.GeneratorExcel;

@Service
public class FinancialService {

	private static final DateTimeFormatter EXCEL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	private final FinancialEntryRepository financialEntryRepository;
	private final FinancialStatementRepository financialStatementRepository;
	private final OrderPaymentRepository orderPaymentRepository;
	private final AuditLogService auditLogService;
	private final GeneratorExcel generatorExcel;

	public FinancialService(FinancialEntryRepository financialEntryRepository,
			FinancialStatementRepository financialStatementRepository, OrderPaymentRepository orderPaymentRepository,
			AuditLogService auditLogService,

			GeneratorExcel generatorExcel) {
		this.financialEntryRepository = financialEntryRepository;
		this.financialStatementRepository = financialStatementRepository;
		this.orderPaymentRepository = orderPaymentRepository;
		this.auditLogService = auditLogService;
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
		auditLogService.register("FinancialEntry", savedEntry.getId(), "FINANCIAL_ENTRY_CREATED", actorName,
				savedEntry.getType() + " " + savedEntry.getCategory() + " " + savedEntry.getAmount());
		return new FinancialEntryResponse(savedEntry);
	}

	@Transactional(readOnly = true)
	public FinancialOverviewResponse overview(LocalDate startDate, LocalDate endDate, FinanceEntryType type,
			FinanceCategory category, Pageable pageable) {
		List<FinancialEntryResponse> entries = buildEntries(startDate, endDate, type, category);
		Page<FinancialEntryResponse> entriesPage = financialStatementRepository
				.searchEntries(startDate != null ? startDate.atStartOfDay() : null,
						endDate != null ? endDate.atTime(LocalTime.MAX) : null, type != null ? type.name() : null,
						category != null ? category.name() : null, pageable)
				.map(this::toFinancialEntryResponse);

		return buildOverview(entries, entriesPage);
	}

	@Transactional(readOnly = true)
	public byte[] exportReport(LocalDate startDate, LocalDate endDate, FinanceEntryType type, FinanceCategory category)
			throws Exception {
		List<FinancialEntryResponse> entries = buildEntries(startDate, endDate, type, category);
		FinancialOverviewResponse overview = buildOverview(entries, null);

		List<FinancialReportSummaryResponse> summaryRows = List
				.of(new FinancialReportSummaryResponse(startDate, endDate, type, category, overview.getTotalIncome(),
						overview.getTotalExpense(), overview.getBalance(), entries.size()));
		List<FinancialReportExcelRow> movementRows = entries.stream().map(this::toExcelRow).toList();

		Map<String, List<?>> sheets = new LinkedHashMap<>();
		sheets.put("Resumo", summaryRows);
		if (!movementRows.isEmpty()) {
			sheets.put("Movimentacoes", movementRows);
		}

		return generatorExcel.gerarAbas(sheets).toByteArray();
	}

	private List<FinancialEntryResponse> buildEntries(LocalDate startDate, LocalDate endDate, FinanceEntryType type,
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

	private FinancialOverviewResponse buildOverview(List<FinancialEntryResponse> entries,
			Page<FinancialEntryResponse> entriesPage) {
		BigDecimal totalIncome = entries.stream().filter(entry -> entry.getType() == FinanceEntryType.INCOME)
				.map(FinancialEntryResponse::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add)
				.setScale(2, RoundingMode.HALF_UP);

		BigDecimal totalExpense = entries.stream().filter(entry -> entry.getType() == FinanceEntryType.EXPENSE)
				.map(FinancialEntryResponse::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add)
				.setScale(2, RoundingMode.HALF_UP);

		List<FinancialDailySummaryResponse> dailySummary = buildDailySummary(entries);
		List<FinancialCategoryTotalResponse> categorySummary = buildCategoryTotals(entries);
		List<FinancialPaymentMethodTotalResponse> paymentSummary = buildPaymentMethodTotals(entries);

		List<FinancialEntryResponse> responseEntries = entriesPage != null ? entriesPage.getContent() : entries;
		PageMetadataResponse pageMetadata = entriesPage != null ? new PageMetadataResponse(entriesPage) : null;

		return new FinancialOverviewResponse(totalIncome, totalExpense,
				totalIncome.subtract(totalExpense).setScale(2, RoundingMode.HALF_UP), responseEntries, pageMetadata,
				dailySummary, categorySummary, paymentSummary);
	}

	private FinancialEntryResponse buildOrderPaymentEntry(OrderPayment payment) {
		Order order = payment.getOrder();
		BigDecimal subtotalAmount = safeAmount(order.getSubtotalAmount());
		BigDecimal discountAmount = safeAmount(order.getDiscountAmount());
		BigDecimal serviceFeeAmount = safeAmount(order.getServiceFeeAmount());
		BigDecimal coverChargeAmount = safeAmount(order.getCoverChargeAmount());
		BigDecimal totalAmount = order.getTotalAmount() != null && order.getTotalAmount().compareTo(BigDecimal.ZERO) > 0
				? order.getTotalAmount()
				: payment.getAmount();
		BigDecimal ratio = payment.getAmount().divide(totalAmount, 6, RoundingMode.HALF_UP);

		List<FinancialBreakdownResponse> breakdown = new ArrayList<>();
		addBreakdownLine(breakdown, "Produtos", subtotalAmount.multiply(ratio));
		addBreakdownLine(breakdown, "Descontos", discountAmount.multiply(ratio).negate());
		addBreakdownLine(breakdown, "Taxa de servico", serviceFeeAmount.multiply(ratio));
		addBreakdownLine(breakdown, "Couvert", coverChargeAmount.multiply(ratio));

		return new FinancialEntryResponse("order-payment-" + payment.getId(), FinanceOrigin.ORDER,
				FinanceEntryType.INCOME, FinanceCategory.PRODUCTS,
				"Pedido " + order.getCode() + " - "
						+ (order.getCustomer() != null ? order.getCustomer().getName() : "sem cliente"),
				order.getCode(), order.getCheckoutRequestNotes(), payment.getAmount(), payment.getPaymentMethod(),
				payment.getRecordedAt(), breakdown);
	}

	private FinancialEntryResponse toFinancialEntryResponse(FinancialStatementProjection projection) {
		FinanceOrigin origin = FinanceOrigin.valueOf(projection.getOrigin());
		if (origin == FinanceOrigin.ORDER) {
			OrderPayment payment = orderPaymentRepository.findById(projection.getSourceId()).orElseThrow();
			return buildOrderPaymentEntry(payment);
		}

		FinancialEntry entry = financialEntryRepository.findById(projection.getSourceId()).orElseThrow();
		return new FinancialEntryResponse(entry);
	}

	private void addBreakdownLine(List<FinancialBreakdownResponse> breakdown, String label, BigDecimal amount) {
		BigDecimal scaledAmount = amount.setScale(2, RoundingMode.HALF_UP);
		if (scaledAmount.compareTo(BigDecimal.ZERO) == 0) {
			return;
		}
		breakdown.add(new FinancialBreakdownResponse(label, scaledAmount));
	}

	private BigDecimal safeAmount(BigDecimal amount) {
		return amount != null ? amount.setScale(2, RoundingMode.HALF_UP)
				: BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
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

	private List<FinancialDailySummaryResponse> buildDailySummary(List<FinancialEntryResponse> entries) {
		Map<LocalDate, BigDecimal> incomeByDate = new TreeMap<>();
		Map<LocalDate, BigDecimal> expenseByDate = new TreeMap<>();

		for (FinancialEntryResponse entry : entries.stream()
				.sorted(Comparator.comparing(FinancialEntryResponse::getOccurredAt)).toList()) {
			LocalDate date = entry.getOccurredAt().toLocalDate();
			if (entry.getType() == FinanceEntryType.INCOME) {
				incomeByDate.merge(date, entry.getAmount(), BigDecimal::add);
			} else {
				expenseByDate.merge(date, entry.getAmount(), BigDecimal::add);
			}
		}

		List<LocalDate> dates = new ArrayList<>(incomeByDate.keySet());
		for (LocalDate date : expenseByDate.keySet()) {
			if (!dates.contains(date)) {
				dates.add(date);
			}
		}
		dates.sort(LocalDate::compareTo);

		List<FinancialDailySummaryResponse> summary = new ArrayList<>();
		for (LocalDate date : dates) {
			BigDecimal income = incomeByDate.getOrDefault(date, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
			BigDecimal expense = expenseByDate.getOrDefault(date, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
			summary.add(new FinancialDailySummaryResponse(date, income, expense,
					income.subtract(expense).setScale(2, RoundingMode.HALF_UP)));
		}
		return summary;
	}

	private List<FinancialCategoryTotalResponse> buildCategoryTotals(List<FinancialEntryResponse> entries) {
		Map<String, BigDecimal> totals = new LinkedHashMap<>();
		for (FinancialEntryResponse entry : entries) {
			String key = entry.getType().name() + "|" + entry.getCategory().name();
			totals.merge(key, entry.getAmount(), BigDecimal::add);
		}

		List<FinancialCategoryTotalResponse> response = new ArrayList<>();
		for (Map.Entry<String, BigDecimal> entry : totals.entrySet()) {
			String[] keyParts = entry.getKey().split("\\|");
			response.add(new FinancialCategoryTotalResponse(FinanceEntryType.valueOf(keyParts[0]),
					FinanceCategory.valueOf(keyParts[1]), entry.getValue().setScale(2, RoundingMode.HALF_UP)));
		}
		return response;
	}

	private List<FinancialPaymentMethodTotalResponse> buildPaymentMethodTotals(List<FinancialEntryResponse> entries) {
		Map<PaymentMethod, BigDecimal> totals = new LinkedHashMap<>();
		for (FinancialEntryResponse entry : entries) {
			if (entry.getType() != FinanceEntryType.INCOME || entry.getPaymentMethod() == null) {
				continue;
			}
			totals.merge(entry.getPaymentMethod(), entry.getAmount(), BigDecimal::add);
		}

		List<FinancialPaymentMethodTotalResponse> response = new ArrayList<>();
		for (Map.Entry<PaymentMethod, BigDecimal> entry : totals.entrySet()) {
			response.add(new FinancialPaymentMethodTotalResponse(entry.getKey(),
					entry.getValue().setScale(2, RoundingMode.HALF_UP)));
		}
		return response;
	}

	private FinancialReportExcelRow toExcelRow(FinancialEntryResponse entry) {
		String breakdown = entry.getBreakdown().stream()
				.map(item -> item.getLabel() + ": " + item.getAmount().setScale(2, RoundingMode.HALF_UP))
				.reduce((left, right) -> left + " | " + right).orElse("-");

		return new FinancialReportExcelRow(
				entry.getOccurredAt() != null ? entry.getOccurredAt().format(EXCEL_DATE_TIME_FORMATTER) : "-",
				entry.getOriginLabel(), entry.getTypeLabel(), entry.getCategoryLabel(), entry.getDescription(),
				entry.getReferenceCode(),
				entry.getPaymentMethod() != null ? FinancialLabelUtils.paymentMethod(entry.getPaymentMethod()) : "-",
				entry.getAmount().setScale(2, RoundingMode.HALF_UP).toString(), breakdown, entry.getNotes());
	}
}
