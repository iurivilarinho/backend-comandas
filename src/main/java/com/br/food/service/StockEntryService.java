package com.br.food.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
	public Page<StockEntry> search(
			String productCode,
			String term,
			LocalDate manufacturingDateStart,
			LocalDate manufacturingDateEnd,
			LocalDate expirationDateStart,
			LocalDate expirationDateEnd,
			Pageable pageable) {
		Specification<StockEntry> specification = Specification.where(StockEntrySpecification.hasProductCode(productCode))
				.and(StockEntrySpecification.search(term))
				.and(StockEntrySpecification.manufacturingDateBetween(manufacturingDateStart, manufacturingDateEnd))
				.and(StockEntrySpecification.expirationDateBetween(expirationDateStart, expirationDateEnd));
		return stockEntryRepository.findAll(specification, pageable);
	}

	@Transactional
	public StockEntry create(StockEntryRequest request) {
		Product product = productService.findById(request.getProductId());
		return createOrMerge(product, request, null);
	}

	@Transactional
	public StockEntry createOrMerge(Product product, StockEntryRequest request, com.br.food.models.SupplyInvoice supplyInvoice) {
		String normalizedBatch = normalizeBatch(request.getBatch());
		return stockEntryRepository.findAllByProductIdAndBatchIgnoreCase(product.getId(), normalizedBatch).stream()
				.filter(existingStockEntry -> sameDates(existingStockEntry, request))
				.findFirst()
				.map(existingStockEntry -> mergeIntoExisting(existingStockEntry, request, normalizedBatch))
				.orElseGet(() -> stockEntryRepository.save(new StockEntry(request, product, supplyInvoice, normalizedBatch)));
	}

	@Transactional
	public void decreaseStock(Long stockEntryId, BigDecimal quantity) {
		StockEntry stockEntry = findById(stockEntryId);
		decrease(stockEntry, quantity);
	}

	@Transactional
	public List<StockConsumption> decreaseStockForProduct(Long productId, BigDecimal quantity, OrderItem orderItem) {
		List<StockEntry> stockEntries = stockEntryRepository
				.findByProductIdAndRetainedFalseAndAvailableQuantityGreaterThanOrderByIdAsc(productId, BigDecimal.ZERO);
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
			Product product = productService.findById(productId);
			String productLabel = (product.getCode() != null ? product.getCode() : "SEM-CODIGO")
					+ " - "
					+ (product.getDescription() != null ? product.getDescription() : "Produto");
			throw new DataIntegrityViolationException("Estoque insuficiente para " + productLabel + ".");
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

	@Transactional
	public StockEntry updateRetention(Long stockEntryId, boolean retained) {
		StockEntry stockEntry = findById(stockEntryId);
		stockEntry.setRetained(retained);
		return stockEntryRepository.save(stockEntry);
	}

	@Transactional
	public int retainExpiredEntries(LocalDate referenceDate) {
		List<StockEntry> expiredEntries = stockEntryRepository.findAllByRetainedFalseAndExpirationDateBefore(referenceDate);
		for (StockEntry stockEntry : expiredEntries) {
			stockEntry.setRetained(true);
		}
		stockEntryRepository.saveAll(expiredEntries);
		return expiredEntries.size();
	}

	private void decrease(StockEntry stockEntry, BigDecimal quantity) {
		if (stockEntry.getAvailableQuantity().compareTo(quantity) < 0) {
			throw new DataIntegrityViolationException(
					"Insufficient stock. Available: " + stockEntry.getAvailableQuantity() + ", requested: " + quantity);
		}
		stockEntry.setAvailableQuantity(stockEntry.getAvailableQuantity().subtract(quantity));
		stockEntry.setSoldQuantity(stockEntry.getSoldQuantity().add(quantity));
	}

	private StockEntry mergeIntoExisting(StockEntry existingStockEntry, StockEntryRequest request, String normalizedBatch) {
		existingStockEntry.setBatch(normalizedBatch);
		existingStockEntry.setInputQuantity(existingStockEntry.getInputQuantity().add(request.getQuantity()));
		existingStockEntry.setAvailableQuantity(existingStockEntry.getAvailableQuantity().add(request.getQuantity()));
		existingStockEntry.setManufacturingDate(request.getManufacturingDate());
		existingStockEntry.setExpirationDate(request.getExpirationDate());
		return stockEntryRepository.save(existingStockEntry);
	}

	private String normalizeBatch(String batch) {
		return batch == null ? null : batch.trim();
	}

	private boolean sameDates(StockEntry stockEntry, StockEntryRequest request) {
		return Objects.equals(stockEntry.getManufacturingDate(), request.getManufacturingDate())
				&& Objects.equals(stockEntry.getExpirationDate(), request.getExpirationDate());
	}
}
