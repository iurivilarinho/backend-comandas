package com.br.food.repository.filtros;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.br.food.models.Pedido;
import com.br.food.repository.PedidoRepository;
import com.br.food.repository.filtros.specification.PedidoSpecification;

@Service
public class PedidoFiltro {

	@Autowired
	private PedidoRepository pedidoRepository;

	public Page<Pedido> filtro(Boolean itensPendentes, Pageable page) {

		Specification<Pedido> spec = Specification.where(null);

		if (itensPendentes) {
			spec = spec.and(PedidoSpecification.pedidoComItensPendentes());
		}

		return pedidoRepository.findAll(spec, page);
	}

}
