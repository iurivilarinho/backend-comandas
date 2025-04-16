package com.br.food.service;

import java.io.IOException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.br.food.forms.ProdutoForm;
import com.br.food.models.Produto;
import com.br.food.repository.ProdutoRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ProdutoService {

	private final ProdutoRepository produtoRepository;
	private final DocumentoService documentoService;

	public ProdutoService(ProdutoRepository produtoRepository, DocumentoService documentoService) {
		this.produtoRepository = produtoRepository;
		this.documentoService = documentoService;
	}

	@Transactional(readOnly = true)
	public Produto buscarProdutoPorId(long id) {
		return produtoRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Produto não encontrada para ID " + id));
	}

	@Transactional(readOnly = true)
	public Page<Produto> buscarProdutos(Pageable page) {
		return produtoRepository.findAll(page);
	}

	@Transactional
	public Produto cadastrarProduto(ProdutoForm form, MultipartFile img) throws IOException {
		return produtoRepository.save(new Produto(form, documentoService.converterEmDocumento(img, false)));
	}

	@Transactional
	public Produto atualizarProduto(ProdutoForm form, MultipartFile img, long idProduto) throws IOException {

		Produto produto = buscarProdutoPorId(idProduto);

		if (img != null) {
			produto.setImagem(documentoService.converterEmDocumento(img, false));
		}

		produto.setCodigo(form.getCodigo());
		produto.setDescricao(form.getDescricao());
		produto.setTipo(form.getTipo());
		produto.setValor(form.getValor());

		return produtoRepository.save(produto);
	}

	@Transactional
	public void ativarDesativarProduto(long idProduto, boolean status) throws IOException {

		Produto produto = buscarProdutoPorId(idProduto);
		produto.setStatus(status);
	}

	@Transactional
	public void vincularProdutoEmComplemento(Long idProduto, Long idComplemento) {
		Produto produto = buscarProdutoPorId(idProduto);
		Produto complemento = buscarProdutoPorId(idProduto);

		if (!complemento.getComplemento()) {
			throw new DataIntegrityViolationException("Este produto não é um complemento!");
		}

		produto.getComplementos().add(complemento);
		produtoRepository.save(produto);
	}

	@Transactional
	public void desvincularProdutoEmComplemento(Long idProduto, Long idComplemento) {
		Produto produto = buscarProdutoPorId(idProduto);
		produto.getComplementos().removeIf(c -> c.getId().equals(idComplemento));
		produtoRepository.save(produto);
	}
}
