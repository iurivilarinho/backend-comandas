package com.br.food.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.br.food.models.Promotion;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

}

