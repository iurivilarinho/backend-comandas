package com.br.food.repository.filtros;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.br.food.models.Estoque;
import com.br.food.repository.EstoqueRepository;
import com.br.food.repository.filtros.specification.EstoqueSpecification;

@Service
public class EstoqueFiltro {

	@Autowired
	private EstoqueRepository estoqueRepository;

	public Page<Estoque> filtro(String codigo, String like, Pageable page) {

		Specification<Estoque> spec = Specification.where(null);

		if (like != null) {
			spec = spec.and(EstoqueSpecification.searchAllFields(like));
		}

		if (codigo != null) {
			spec = spec.and(EstoqueSpecification.buscarEstoquePorCodigoProduto(codigo));
		}

		return estoqueRepository.findAll(spec, page);
	}

}
