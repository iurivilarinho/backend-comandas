package com.br.food.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.enums.Types.FinanceCategory;
import com.br.food.enums.Types.FinanceEntryType;
import com.br.food.enums.Types.FinanceOrigin;
import com.br.food.models.FinancialEntry;
import com.br.food.models.Order;
import com.br.food.models.OrderPayment;
import com.br.food.repository.FinancialEntryRepository;
import com.br.food.repository.OrderPaymentRepository;
import com.br.food.request.FinancialEntryRequest;
import com.br.food.response.FinancialBreakdownResponse;
import com.br.food.response.FinancialEntryResponse;
import com.br.food.response.FinancialOverviewResponse;

@Service
public class FinancialService {

	private final FinancialEntryRepository financialEntryRepository;
	private final OrderPaymentRepository orderPaymentRepository;
	private final AuditLogService auditLogService;
	private final OrderService orderService;

	public FinancialService(
			FinancialEntryRepository financialEntryRepository,
			OrderPaymentRepository orderPaymentRepository,
			AuditLogService auditLogService,
			OrderService orderService) {
		this.financialEntryRepository = financialEntryRepository;
		this.orderPaymentRepository = orderPaymentRepository;
		this.auditLogService = auditLogService;
		this.orderService = orderService;
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

		return new FinancialOverviewResponse(
				totalIncome,
				totalExpense,
				totalIncome.subtract(totalExpense).setScale(2, RoundingMode.HALF_UP),
				entries);
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
}
