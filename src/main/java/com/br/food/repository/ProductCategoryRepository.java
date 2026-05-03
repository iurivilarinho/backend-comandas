package com.br.food.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.br.food.models.ProductCategory;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

	Optional<ProductCategory> findByNameIgnoreCase(String name);

	Page<ProductCategory> findAllByActiveAndNameContainingIgnoreCaseOrderByNameAsc(Boolean active, String name, Pageable pageable);

	Page<ProductCategory> findAllByNameContainingIgnoreCaseOrderByNameAsc(String name, Pageable pageable);
}
