package com.br.food.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.forms.EstoqueForm;
import com.br.food.models.Estoque;
import com.br.food.models.Produto;
import com.br.food.repository.EstoqueRepository;
import com.br.food.repository.filtros.EstoqueFiltro;

import jakarta.persistence.EntityNotFoundException;

@Service
public class EstoqueService {

	@Autowired
	private EstoqueRepository estoqueRepository;

	@Autowired
	private ProdutoService produtoService;

	@Autowired
	private EstoqueFiltro estoqueFiltro;

	@Transactional(readOnly = true)
	public Estoque buscarPorId(Long id) {
		return estoqueRepository.findById(id).orElseThrow(
				() -> new EntityNotFoundException("Registro com ID " + id + " não encontrado na tabela de estoque"));
	}

	@Transactional(readOnly = true)
	public Page<Estoque> consultarEstoque(String codigoProduto, String like, Pageable page) {

		return estoqueFiltro.filtro(codigoProduto, codigoProduto, page);
	}

	@Transactional
	public void adicionarEstoqueProduto(EstoqueForm form) {
		Produto produto = produtoService.buscarProdutoPorId(form.getIdProduto());
		Estoque estoque = new Estoque(form, produto);

		estoqueRepository.save(estoque);
	}

	@Transactional
	public void darBaixaEstoque(Long idEstoque, BigDecimal quantidade) {
		Estoque estoque = buscarPorId(idEstoque);
		if (estoque.getQuantidadeDisponivel().compareTo(quantidade) < 0) {
			throw new DataIntegrityViolationException("Estoque insuficiente! Disponivel: "
					+ estoque.getQuantidadeDisponivel() + ", solicitado: " + quantidade);
		}
		estoque.setQuantidadeDisponivel(estoque.getQuantidadeDisponivel().subtract(quantidade));
		estoque.setQuantidadeVendida(estoque.getQuantidadeVendida().add(quantidade));
	}

}
