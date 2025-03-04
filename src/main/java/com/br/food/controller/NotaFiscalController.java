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

import com.br.food.enums.Status.StatusNotaFiscal;
import com.br.food.forms.NotaFiscalForm;
import com.br.food.models.NotaFiscal;
import com.br.food.service.NotaFiscalService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/notas-fiscais")
@Tag(name = "Notas Fiscais", description = "Endpoints para gerenciamento de notas fiscais")
public class NotaFiscalController {

	@Autowired
	private NotaFiscalService notaFiscalService;

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "Cadastra uma nova nota fiscal", description = "Cria uma nova nota fiscal com base nos dados fornecidos")
	@ApiResponses({ @ApiResponse(responseCode = "201", description = "Nota fiscal criada com sucesso"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos no formulário"),
			@ApiResponse(responseCode = "409", description = "Conflito: chave da NFE já registrada"),
			@ApiResponse(responseCode = "500", description = "Erro interno no servidor") })
	public ResponseEntity<NotaFiscal> cadastrarNotaFiscal(@Valid @RequestPart NotaFiscalForm form,
			@RequestPart(required = false) MultipartFile anexo) throws IOException {
		NotaFiscal notaFiscal = notaFiscalService.cadastroNotaFiscal(form, anexo);

		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(notaFiscal.getId())
				.toUri();

		return ResponseEntity.created(location).body(notaFiscal);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Busca uma nota fiscal por ID", description = "Retorna os detalhes de uma nota fiscal específica")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Nota fiscal encontrada"),
			@ApiResponse(responseCode = "404", description = "Nota fiscal não encontrada"),
			@ApiResponse(responseCode = "500", description = "Erro interno no servidor") })
	public ResponseEntity<NotaFiscal> buscarNotaFiscalPorId(@PathVariable Long id) {
		NotaFiscal notaFiscal = notaFiscalService.buscarNotaFiscalPorId(id);
		return ResponseEntity.ok(notaFiscal);
	}

	@GetMapping
	@Operation(summary = "Busca as notas fiscais cadastradas", description = "Retorna uma pagina de notas fiscais")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Notas fiscais encontradas"),
			@ApiResponse(responseCode = "500", description = "Erro interno no servidor") })
	public ResponseEntity<Page<NotaFiscal>> buscarNotasFiscais(Pageable page) {

		return ResponseEntity.ok(notaFiscalService.buscarNotaFiscal(page));
	}

	@PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "Atualiza uma nota fiscal existente", description = "Atualiza os dados de uma nota fiscal com base no ID")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Nota fiscal atualizada com sucesso"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos no formulário"),
			@ApiResponse(responseCode = "404", description = "Nota fiscal não encontrada"),
			@ApiResponse(responseCode = "409", description = "Conflito: chave da NFE já registrada"),
			@ApiResponse(responseCode = "500", description = "Erro interno no servidor") })
	public ResponseEntity<NotaFiscal> atualizarNotaFiscal(@PathVariable Long id,
			@Valid @RequestPart NotaFiscalForm form, @RequestPart(required = false) MultipartFile anexo)
			throws IOException {
		NotaFiscal notaFiscalAtualizada = notaFiscalService.atualizarNotaFiscal(form, anexo, id);
		return ResponseEntity.ok(notaFiscalAtualizada);
	}

	@PatchMapping("/{id}/status")
	@Operation(summary = "Altera o status de uma nota fiscal", description = "Atualiza o status de uma nota fiscal existente")
	@ApiResponses({ @ApiResponse(responseCode = "204", description = "Status alterado com sucesso"),
			@ApiResponse(responseCode = "400", description = "Transição de status inválida"),
			@ApiResponse(responseCode = "404", description = "Nota fiscal não encontrada"),
			@ApiResponse(responseCode = "500", description = "Erro interno no servidor") })
	public ResponseEntity<Void> alterarStatusNotaFiscal(@PathVariable Long id,
			@RequestParam("status") StatusNotaFiscal status) {
		notaFiscalService.alterarStatusNotaFiscal(status, id);
		return ResponseEntity.noContent().build();
	}

}