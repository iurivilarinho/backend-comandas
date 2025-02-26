package com.br.food.repository.filtros.specification;

import org.springframework.data.jpa.domain.Specification;

import com.br.food.enums.Status.StatusItem;
import com.br.food.models.ItemPedido;
import com.br.food.models.Pedido;

import jakarta.persistence.criteria.Join;

public class PedidoSpecification {

	public static Specification<Pedido> pedidoComItensPendentes() {
		return (root, query, builder) -> {
			Join<Pedido, ItemPedido> joinItens = root.join("itens");
			return builder.equal(joinItens.get("status"), StatusItem.PENDENTE);
		};
	}
}
