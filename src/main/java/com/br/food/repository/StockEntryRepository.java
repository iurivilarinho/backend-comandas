package com.br.food.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.br.food.models.StockEntry;

public interface StockEntryRepository extends JpaRepository<StockEntry, Long>, JpaSpecificationExecutor<StockEntry> {

	List<StockEntry> findByProductIdAndAvailableQuantityGreaterThanOrderByIdAsc(Long productId, java.math.BigDecimal quantity);
}
