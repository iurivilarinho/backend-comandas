package com.br.food.service;

import java.math.BigDecimal;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.models.StockEntry;
import com.br.food.models.Product;
import com.br.food.repository.StockEntryRepository;
import com.br.food.repository.StockEntrySpecification;
import com.br.food.request.StockEntryRequest;

import jakarta.persistence.EntityNotFoundException;

@Service
public class StockEntryService {

	private final StockEntryRepository stockEntryRepository;
	private final ProductService productService;

	public StockEntryService(StockEntryRepository stockEntryRepository, ProductService productService) {
		this.stockEntryRepository = stockEntryRepository;
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
		if (stockEntry.getAvailableQuantity().compareTo(quantity) < 0) {
			throw new DataIntegrityViolationException(
					"Insufficient stock. Available: " + stockEntry.getAvailableQuantity() + ", requested: " + quantity);
		}
		stockEntry.setAvailableQuantity(stockEntry.getAvailableQuantity().subtract(quantity));
		stockEntry.setSoldQuantity(stockEntry.getSoldQuantity().add(quantity));
	}
}
