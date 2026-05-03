package com.br.food.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.br.food.models.StockConsumption;

@Repository
public interface StockConsumptionRepository extends JpaRepository<StockConsumption, Long> {

	List<StockConsumption> findByOrderItemId(Long orderItemId);
}
