package com.br.food.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.br.food.models.StockConsumption;

public interface StockConsumptionRepository extends JpaRepository<StockConsumption, Long> {

	List<StockConsumption> findByOrderItemId(Long orderItemId);
}
