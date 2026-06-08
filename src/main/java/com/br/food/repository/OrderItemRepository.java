package com.br.food.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.br.food.enums.Types.OrderItemStatus;
import com.br.food.enums.Types.OrderStatus;
import com.br.food.models.OrderItem;
import com.br.food.repository.projection.ProductSalesAggregationProjection;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

	List<OrderItem> findByStatusInOrderByRequestedAtAsc(List<OrderItemStatus> statuses);

	@Query("""
			select oi.product.id as productId,
			       sum(oi.quantity) as soldQuantity,
			       sum(oi.unitPrice * oi.quantity) as salesRevenue
			from OrderItem oi
			where oi.product is not null
			  and oi.order.status = :orderStatus
			  and oi.status not in :excludedStatuses
			  and oi.requestedAt >= :startDateTime
			  and oi.requestedAt <= :endDateTime
			group by oi.product.id
			""")
	List<ProductSalesAggregationProjection> aggregateSalesByProduct(@Param("orderStatus") OrderStatus orderStatus,
			@Param("excludedStatuses") List<OrderItemStatus> excludedStatuses,
			@Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime);
}
