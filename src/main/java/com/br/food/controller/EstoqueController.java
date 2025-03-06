package com.br.food.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.br.food.models.Estoque;
import com.br.food.service.EstoqueService;

@RestController
@RequestMapping("/estoque")
public class EstoqueController {

	@Autowired
	private EstoqueService estoqueService;

	@GetMapping
	public Page<Estoque> consultarEstoquePorProduto(@RequestParam(required = false) String codigoProduto,
			@RequestParam(required = false) String like, Pageable page) {

		System.out.println(like);

		return estoqueService.consultarEstoque(codigoProduto, like, page);
	}

}
