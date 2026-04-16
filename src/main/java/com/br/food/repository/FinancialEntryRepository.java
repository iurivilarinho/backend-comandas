package com.br.food.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.br.food.models.FinancialEntry;

public interface FinancialEntryRepository extends JpaRepository<FinancialEntry, Long> {
}
