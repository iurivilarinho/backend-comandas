package com.br.food.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.br.food.enums.Types.SupplyInvoiceStatus;
import com.br.food.models.SupplyInvoice;

@Repository
public interface SupplyInvoiceRepository extends JpaRepository<SupplyInvoice, Long>, JpaSpecificationExecutor<SupplyInvoice> {

	Optional<SupplyInvoice> findByAccessKeyAndStatusNot(String accessKey, SupplyInvoiceStatus canceledStatus);
}
