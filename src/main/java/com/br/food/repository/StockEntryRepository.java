package com.br.food.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.br.food.models.StockEntry;

@Repository
public interface StockEntryRepository extends JpaRepository<StockEntry, Long>, JpaSpecificationExecutor<StockEntry> {

	List<StockEntry> findByProductIdAndRetainedFalseAndAvailableQuantityGreaterThanOrderByIdAsc(Long productId,
			java.math.BigDecimal quantity);

	List<StockEntry> findAllByProductIdAndBatchIgnoreCase(Long productId, String batch);

	List<StockEntry> findAllByRetainedFalseAndExpirationDateBefore(LocalDate expirationDate);
}
