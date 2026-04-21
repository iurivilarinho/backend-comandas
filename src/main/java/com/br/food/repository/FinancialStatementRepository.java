package com.br.food.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.br.food.models.FinancialEntry;
import com.br.food.repository.projection.FinancialStatementProjection;

public interface FinancialStatementRepository extends Repository<FinancialEntry, Long> {

	@Query(value = """
			select ledger.entryId as entryId,
			       ledger.sourceId as sourceId,
			       ledger.origin as origin,
			       ledger.type as type,
			       ledger.category as category,
			       ledger.description as description,
			       ledger.referenceCode as referenceCode,
			       ledger.notes as notes,
			       ledger.amount as amount,
			       ledger.paymentMethod as paymentMethod,
			       ledger.occurredAt as occurredAt
			  from (
			        select concat('manual-', cast(fe.id as varchar)) as entryId,
			               fe.id as sourceId,
			               'MANUAL' as origin,
			               fe.entry_type as type,
			               fe.category as category,
			               fe.description as description,
			               concat('#', cast(fe.id as varchar)) as referenceCode,
			               fe.notes as notes,
			               fe.amount as amount,
			               fe.payment_method as paymentMethod,
			               fe.occurred_at as occurredAt
			          from financial_entries fe
			         where (cast(:startDateTime as timestamp) is null or fe.occurred_at >= :startDateTime)
			           and (cast(:endDateTime as timestamp) is null or fe.occurred_at <= :endDateTime)
			           and (cast(:type as varchar) is null or fe.entry_type = :type)
			           and (cast(:category as varchar) is null or fe.category = :category)

			        union all

			        select concat('order-payment-', cast(op.id as varchar)) as entryId,
			               op.id as sourceId,
			               'ORDER' as origin,
			               'INCOME' as type,
			               'PRODUCTS' as category,
			               concat('Pedido ', o.code, ' - ', coalesce(c.name, 'sem cliente')) as description,
			               o.code as referenceCode,
			               o.checkout_request_notes as notes,
			               op.amount as amount,
			               op.payment_method as paymentMethod,
			               op.recorded_at as occurredAt
			          from order_payments op
			          join orders o on o.id = op.fk_order_id
			     left join customers c on c.id = o.fk_customer_id
			         where (cast(:startDateTime as timestamp) is null or op.recorded_at >= :startDateTime)
			           and (cast(:endDateTime as timestamp) is null or op.recorded_at <= :endDateTime)
			           and (cast(:type as varchar) is null or :type = 'INCOME')
			           and (cast(:category as varchar) is null or :category = 'PRODUCTS')
			       ) ledger
			 order by ledger.occurredAt desc
			""", countQuery = """
			select count(*)
			  from (
			        select fe.id
			          from financial_entries fe
			         where (cast(:startDateTime as timestamp) is null or fe.occurred_at >= :startDateTime)
			           and (cast(:endDateTime as timestamp) is null or fe.occurred_at <= :endDateTime)
			           and (cast(:type as varchar) is null or fe.entry_type = :type)
			           and (cast(:category as varchar) is null or fe.category = :category)

			        union all

			        select op.id
			          from order_payments op
			          join orders o on o.id = op.fk_order_id
			         where (cast(:startDateTime as timestamp) is null or op.recorded_at >= :startDateTime)
			           and (cast(:endDateTime as timestamp) is null or op.recorded_at <= :endDateTime)
			           and (cast(:type as varchar) is null or :type = 'INCOME')
			           and (cast(:category as varchar) is null or :category = 'PRODUCTS')
			       ) ledger
			""", nativeQuery = true)
	Page<FinancialStatementProjection> searchEntries(
			@Param("startDateTime") LocalDateTime startDateTime,
			@Param("endDateTime") LocalDateTime endDateTime,
			@Param("type") String type,
			@Param("category") String category,
			Pageable pageable);
}
