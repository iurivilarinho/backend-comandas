package com.br.food.service;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
		List<DiningTable> tables = new ArrayList<>();
		for (int index = 0; index < tableCount; index++) {
			tables.add(new DiningTable(generateTableNumber()));
		}
		return diningTableRepository.saveAll(tables);
	}

	@Transactional
	public DiningTable create(DiningTableRequest request) {
		return diningTableRepository.save(new DiningTable(request));
	}

	@Transactional(readOnly = true)
	public String generateTableNumber() {
		DiningTable highestTable = diningTableRepository.findTopByOrderByNumberDesc();
		int nextNumber = highestTable != null ? Integer.parseInt(highestTable.getNumber()) + 1 : 1;
		return String.valueOf(nextNumber);
	}

	@Transactional
	public DiningTable update(Long id, DiningTableRequest request) {
		DiningTable table = findById(id);
		table.update(request);
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
				.filter(response -> status == null || status.isBlank() || Objects.equals(response.getOperationalStatus(), status))
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
			throw new AccessDeniedException("Table is inactive.");
		}
		if (Boolean.TRUE.equals(table.getOccupied())) {
			throw new AccessDeniedException("Table is already occupied.");
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
}
