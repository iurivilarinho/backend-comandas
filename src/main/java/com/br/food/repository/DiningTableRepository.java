package com.br.food.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.br.food.models.DiningTable;

public interface DiningTableRepository extends JpaRepository<DiningTable, Long> {

	Optional<DiningTable> findByNumber(String number);

	boolean existsByNumber(String number);

}
