package com.br.food.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.models.OrderItem;
import com.br.food.models.Product;
import com.br.food.models.StockConsumption;
import com.br.food.models.StockEntry;
import com.br.food.repository.StockConsumptionRepository;
import com.br.food.repository.StockEntryRepository;
import com.br.food.repository.StockEntrySpecification;
import com.br.food.request.StockEntryRequest;

import jakarta.persistence.EntityNotFoundException;

@Service
public class StockEntryService {

	private final StockEntryRepository stockEntryRepository;
	private final StockConsumptionRepository stockConsumptionRepository;
	private final ProductService productService;

	public StockEntryService(
			StockEntryRepository stockEntryRepository,
			StockConsumptionRepository stockConsumptionRepository,
			ProductService productService) {
		this.stockEntryRepository = stockEntryRepository;
		this.stockConsumptionRepository = stockConsumptionRepository;
		this.productService = productService;
	}

	@Transactional(readOnly = true)
	public StockEntry findById(Long id) {
		return stockEntryRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Stock entry not found for id " + id + "."));
	}

	@Transactional(readOnly = true)
	public Page<StockEntry> search(String productCode, String term, Pageable pageable) {
		Specification<StockEntry> specification = Specification.where(StockEntrySpecification.hasProductCode(productCode))
				.and(StockEntrySpecification.search(term));
		return stockEntryRepository.findAll(specification, pageable);
	}

	@Transactional
	public StockEntry create(StockEntryRequest request) {
		Product product = productService.findById(request.getProductId());
		return stockEntryRepository.save(new StockEntry(request, product, null));
	}

	@Transactional
	public void decreaseStock(Long stockEntryId, BigDecimal quantity) {
		StockEntry stockEntry = findById(stockEntryId);
		decrease(stockEntry, quantity);
	}

	@Transactional
	public List<StockConsumption> decreaseStockForProduct(Long productId, BigDecimal quantity, OrderItem orderItem) {
		List<StockEntry> stockEntries = stockEntryRepository.findByProductIdAndAvailableQuantityGreaterThanOrderByIdAsc(productId, BigDecimal.ZERO);
		BigDecimal remainingQuantity = quantity;
		List<StockConsumption> consumptions = new ArrayList<>();

		for (StockEntry stockEntry : stockEntries) {
			if (remainingQuantity.signum() <= 0) {
				break;
			}
			BigDecimal consumed = stockEntry.getAvailableQuantity().min(remainingQuantity);
			if (consumed.signum() <= 0) {
				continue;
			}
			decrease(stockEntry, consumed);
			remainingQuantity = remainingQuantity.subtract(consumed);
			consumptions.add(stockConsumptionRepository.save(new StockConsumption(orderItem, stockEntry, consumed)));
		}

		if (remainingQuantity.signum() > 0) {
			throw new DataIntegrityViolationException("Insufficient stock to consume product " + productId + ".");
		}

		return consumptions;
	}

	@Transactional
	public void restoreConsumptions(OrderItem orderItem) {
		List<StockConsumption> consumptions = stockConsumptionRepository.findByOrderItemId(orderItem.getId());
		for (StockConsumption consumption : consumptions) {
			StockEntry stockEntry = consumption.getStockEntry();
			stockEntry.setAvailableQuantity(stockEntry.getAvailableQuantity().add(consumption.getQuantity()));
			stockEntry.setSoldQuantity(stockEntry.getSoldQuantity().subtract(consumption.getQuantity()));
		}
		stockConsumptionRepository.deleteAll(consumptions);
		orderItem.getStockConsumptions().clear();
	}

	private void decrease(StockEntry stockEntry, BigDecimal quantity) {
		if (stockEntry.getAvailableQuantity().compareTo(quantity) < 0) {
			throw new DataIntegrityViolationException(
					"Insufficient stock. Available: " + stockEntry.getAvailableQuantity() + ", requested: " + quantity);
		}
		stockEntry.setAvailableQuantity(stockEntry.getAvailableQuantity().subtract(quantity));
		stockEntry.setSoldQuantity(stockEntry.getSoldQuantity().add(quantity));
	}
}
