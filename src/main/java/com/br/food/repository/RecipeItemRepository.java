package com.br.food.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.br.food.models.RecipeItem;

public interface RecipeItemRepository extends JpaRepository<RecipeItem, Long> {

	List<RecipeItem> findByFinalProductId(Long productId);

	void deleteByFinalProductId(Long productId);
}
