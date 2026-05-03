package com.br.food.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.br.food.models.FinancialEntry;

@Repository
public interface FinancialEntryRepository extends JpaRepository<FinancialEntry, Long> {
}
