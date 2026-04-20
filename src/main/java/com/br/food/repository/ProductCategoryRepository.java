package com.br.food.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.br.food.models.ProductCategory;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

	Optional<ProductCategory> findByNameIgnoreCase(String name);

	List<ProductCategory> findAllByActiveOrderByNameAsc(Boolean active);

	List<ProductCategory> findAllByOrderByNameAsc();
}
