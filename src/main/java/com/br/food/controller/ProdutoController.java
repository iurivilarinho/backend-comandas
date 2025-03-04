package com.br.food.controller;

import java.io.IOException;
import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.br.food.forms.ProdutoForm;
import com.br.food.models.Produto;
import com.br.food.service.ProdutoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/produtos")
@Tag(name = "Produtos", description = "Endpoints para gerenciamento de produtos")
public class ProdutoController {

	@Autowired
	private ProdutoService produtoService;

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "Cadastra um novo produto", description = "Cria um novo produto com dados do formulário e imagem opcional")
	@ApiResponses({ @ApiResponse(responseCode = "201", description = "Produto criado com sucesso"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos no formulário ou imagem inválida"),
			@ApiResponse(responseCode = "500", description = "Erro interno no servidor, possivelmente no upload da imagem") })
	public ResponseEntity<Produto> cadastrarProduto(@Valid @RequestPart ProdutoForm form,
			@RequestPart(required = false) MultipartFile img) throws IOException {
		Produto produto = produtoService.cadastrarProduto(form, img);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(produto.getId())
				.toUri();
		return ResponseEntity.created(location).body(produto);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Busca um produto por ID", description = "Retorna os detalhes de um produto específico")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Produto encontrado"),
			@ApiResponse(responseCode = "404", description = "Produto não encontrado"),
			@ApiResponse(responseCode = "500", description = "Erro interno no servidor") })
	public ResponseEntity<Produto> buscarProdutoPorId(@PathVariable Long id) {
		Produto produto = produtoService.buscarProdutoPorId(id);
		return ResponseEntity.ok(produto);
	}

	@GetMapping
	@Operation(summary = "Lista todos os produtos", description = "Retorna uma lista paginada de produtos")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Lista de produtos retornada com sucesso"),
			@ApiResponse(responseCode = "500", description = "Erro interno no servidor") })
	public ResponseEntity<Page<Produto>> buscarProdutos(Pageable pageable) {
		Page<Produto> produtos = produtoService.buscarProdutos(pageable);
		return ResponseEntity.ok(produtos);
	}

	@PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "Atualiza um produto existente", description = "Atualiza os dados de um produto, incluindo imagem opcional")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos no formulário ou imagem inválida"),
			@ApiResponse(responseCode = "404", description = "Produto não encontrado"),
			@ApiResponse(responseCode = "500", description = "Erro interno no servidor, possivelmente no upload da imagem") })
	public ResponseEntity<Produto> atualizarProduto(@PathVariable Long id, @Valid @RequestPart ProdutoForm form,
			@RequestPart(required = false) MultipartFile img) throws IOException {
		Produto produtoAtualizado = produtoService.atualizarProduto(form, img, id);
		return ResponseEntity.ok(produtoAtualizado);
	}

	@PatchMapping("/{id}/status")
	@Operation(summary = "Ativa ou desativa um produto", description = "Altera o status de ativação de um produto")
	@ApiResponses({ @ApiResponse(responseCode = "204", description = "Status alterado com sucesso"),
			@ApiResponse(responseCode = "404", description = "Produto não encontrado"),
			@ApiResponse(responseCode = "500", description = "Erro interno no servidor") })
	public ResponseEntity<Void> ativarDesativarProduto(@PathVariable Long id, @RequestParam("status") boolean status)
			throws IOException {
		produtoService.ativarDesativarProduto(id, status);
		return ResponseEntity.noContent().build();
	}

}
