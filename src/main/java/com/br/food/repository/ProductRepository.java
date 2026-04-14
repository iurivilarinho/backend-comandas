package com.br.food.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.br.food.models.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

}

