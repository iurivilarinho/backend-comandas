package com.br.food.service;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Comparator;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.enums.Types.OrderItemStatus;
import com.br.food.enums.Types.OrderStatus;
import com.br.food.models.DiningTable;
import com.br.food.models.Order;
import com.br.food.repository.DiningTableRepository;
import com.br.food.repository.OrderRepository;
import com.br.food.repository.OrderSpecification;
import com.br.food.request.DiningTableRequest;
import com.br.food.response.DiningTableResponse;

import jakarta.persistence.EntityNotFoundException;

@Service
public class DiningTableService {

	private final DiningTableRepository diningTableRepository;
	private final OrderRepository orderRepository;
	private final TableAccessTokenService tableAccessTokenService;

	public DiningTableService(
			DiningTableRepository diningTableRepository,
			OrderRepository orderRepository,
			TableAccessTokenService tableAccessTokenService) {
		this.diningTableRepository = diningTableRepository;
		this.orderRepository = orderRepository;
		this.tableAccessTokenService = tableAccessTokenService;
	}

	@Transactional
	public List<DiningTable> createMany(Integer tableCount) {
		if (tableCount == null || tableCount <= 0) {
			throw new DataIntegrityViolationException("Table batch count must be greater than zero.");
		}

		int nextNumber = resolveNextTableNumber();
		Set<String> existingNumbers = new HashSet<>(diningTableRepository.findAll().stream()
				.map(DiningTable::getNumber)
				.filter(Objects::nonNull)
				.toList());
		List<DiningTable> tables = new ArrayList<>();

		for (int index = 0; index < tableCount; index++) {
			String tableNumber = generateNextAvailableTableNumber(nextNumber, existingNumbers);
			tables.add(new DiningTable(tableNumber));
			existingNumbers.add(tableNumber);
			nextNumber = Integer.parseInt(tableNumber) + 1;
		}

		return diningTableRepository.saveAll(tables);
	}

	@Transactional
	public DiningTable create(DiningTableRequest request) {
		String normalizedNumber = normalizeTableNumber(request.getNumber());
		validateTableNumberUniqueness(normalizedNumber, null);

		DiningTable table = new DiningTable(request);
		table.setNumber(normalizedNumber);
		return diningTableRepository.save(table);
	}

	@Transactional(readOnly = true)
	public String generateTableNumber() {
		return String.valueOf(resolveNextTableNumber());
	}

	@Transactional
	public DiningTable update(Long id, DiningTableRequest request) {
		DiningTable table = findById(id);
		String normalizedNumber = normalizeTableNumber(request.getNumber());
		validateTableNumberUniqueness(normalizedNumber, id);
		table.update(request);
		table.setNumber(normalizedNumber);
		return diningTableRepository.save(table);
	}

	@Transactional(readOnly = true)
	public DiningTable findById(Long id) {
		return diningTableRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Table not found for id " + id + "."));
	}

	@Transactional(readOnly = true)
	public DiningTable findByNumber(String number) {
		return diningTableRepository.findByNumber(number)
				.orElseThrow(() -> new EntityNotFoundException("Table not found for number " + number + "."));
	}

	@Transactional(readOnly = true)
	public List<DiningTableResponse> findAll(String status) {
		List<DiningTable> tables = diningTableRepository.findAll();
		Map<String, Order> activeOrdersByTable = orderRepository
				.findAll(OrderSpecification.hasAnyStatus(List.of(OrderStatus.OPEN, OrderStatus.READY_TO_CLOSE)))
				.stream()
				.filter(order -> order.getDiningTable() != null && order.getDiningTable().getNumber() != null)
				.collect(java.util.stream.Collectors.toMap(
						order -> order.getDiningTable().getNumber(),
						order -> order,
						this::pickOrderWithHigherPriority));

		return tables.stream()
				.map(table -> new DiningTableResponse(table, resolveOperationalStatus(table, activeOrdersByTable.get(table.getNumber()))))
				.filter(response -> {
					if (status != null && !status.isBlank()) {
						return Objects.equals(response.getOperationalStatus(), status);
					}
					return !"INACTIVE".equals(response.getOperationalStatus());
				})
				.sorted(Comparator
						.comparingInt((DiningTableResponse response) -> getOperationalStatusPriority(response.getOperationalStatus()))
						.thenComparing(this::parseResponseTableNumber, Comparator.nullsLast(Integer::compareTo))
						.thenComparing(DiningTableResponse::getNumber, Comparator.nullsLast(String::compareTo)))
				.toList();
	}

	@Transactional
	public void updateStatus(Long id, Boolean active) {
		DiningTable table = findById(id);
		table.setActive(active);
	}

	@Transactional
	public void reserveTable(Long id) throws AccessDeniedException {
		DiningTable table = findById(id);
		if (!Boolean.TRUE.equals(table.getActive())) {
			throw new AccessDeniedException("Mesa inativa.");
		}
		if (Boolean.TRUE.equals(table.getOccupied())) {
			throw new AccessDeniedException("Esta mesa já está ocupada.");
		}
		table.setOccupied(true);
	}

	@Transactional
	public void releaseTable(Long id) {
		DiningTable table = findById(id);
		table.setOccupied(false);
	}

	@Transactional
	public void delete(Long id) {
		diningTableRepository.delete(findById(id));
	}

	@Transactional(readOnly = true)
	public String generateAccessToken(String tableNumber) {
		DiningTable table = findByNumber(tableNumber);
		if (!Boolean.TRUE.equals(table.getActive())) {
			throw new EntityNotFoundException("Table not available for number " + tableNumber + ".");
		}
		return tableAccessTokenService.generate(table.getNumber());
	}

	@Transactional(readOnly = true)
	public String resolveAccessToken(String token) {
		String tableNumber = tableAccessTokenService.resolve(token);
		findByNumber(tableNumber);
		return tableNumber;
	}

	private Order pickOrderWithHigherPriority(Order currentOrder, Order candidateOrder) {
		return getOrderPriority(candidateOrder) >= getOrderPriority(currentOrder) ? candidateOrder : currentOrder;
	}

	private int getOrderPriority(Order order) {
		if (order.getCheckoutRequestedAt() != null) {
			return 3;
		}
		boolean hasReadyItem = order.getItems().stream().anyMatch(item -> item.getStatus() == OrderItemStatus.READY);
		if (hasReadyItem) {
			return 2;
		}
		return 1;
	}

	private String resolveOperationalStatus(DiningTable table, Order order) {
		if (!Boolean.TRUE.equals(table.getActive())) {
			return "INACTIVE";
		}
		if (order != null && order.getCheckoutRequestedAt() != null) {
			return "READY_TO_CLOSE";
		}
		if (order != null && order.getItems().stream().anyMatch(item -> item.getStatus() == OrderItemStatus.READY)) {
			return "READY_FOR_SERVICE";
		}
		if ((order != null && (order.getStatus() == OrderStatus.OPEN || order.getStatus() == OrderStatus.READY_TO_CLOSE))
				|| Boolean.TRUE.equals(table.getOccupied())) {
			return "OCCUPIED";
		}
		return "FREE";
	}

	private int getOperationalStatusPriority(String operationalStatus) {
		if ("READY_TO_CLOSE".equals(operationalStatus)) {
			return 0;
		}
		if ("READY_FOR_SERVICE".equals(operationalStatus)) {
			return 1;
		}
		if ("OCCUPIED".equals(operationalStatus)) {
			return 2;
		}
		if ("FREE".equals(operationalStatus)) {
			return 3;
		}
		return 4;
	}

	private Integer parseResponseTableNumber(DiningTableResponse response) {
		return parseTableNumber(response.getNumber());
	}

	@Transactional(readOnly = true)
	private int resolveNextTableNumber() {
		return diningTableRepository.findAll().stream()
				.map(DiningTable::getNumber)
				.map(this::parseTableNumber)
				.filter(Objects::nonNull)
				.mapToInt(Integer::intValue)
				.max()
				.orElse(0) + 1;
	}

	private String generateNextAvailableTableNumber(int startingNumber, Set<String> existingNumbers) {
		int currentNumber = Math.max(1, startingNumber);

		while (existingNumbers.contains(String.valueOf(currentNumber))) {
			currentNumber++;
		}

		return String.valueOf(currentNumber);
	}

	private void validateTableNumberUniqueness(String number, Long currentTableId) {
		diningTableRepository.findByNumber(number).ifPresent(existingTable -> {
			if (currentTableId == null || !existingTable.getId().equals(currentTableId)) {
				throw new DataIntegrityViolationException("There is already a table using number " + number + ".");
			}
		});
	}

	private String normalizeTableNumber(String number) {
		if (number == null || number.isBlank()) {
			throw new DataIntegrityViolationException("Table number must be informed.");
		}

		return number.trim();
	}

	private Integer parseTableNumber(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}

		try {
			return Integer.valueOf(value.trim());
		} catch (NumberFormatException exception) {
			return null;
		}
	}
}
