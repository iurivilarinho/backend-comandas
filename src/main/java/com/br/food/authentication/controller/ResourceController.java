package com.br.food.authentication.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.br.food.authentication.models.Resource;
import com.br.food.authentication.request.ResourceRequest;
import com.br.food.authentication.response.ResourceBasicResponse;
import com.br.food.authentication.response.ResourceResponse;
import com.br.food.authentication.service.ResourceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Resources", description = "Endpoints para gerenciamento de recursos.")
@RestController
@RequestMapping("/auth/resources")
public class ResourceController {

	private final ResourceService resourceService;

	public ResourceController(ResourceService resourceService) {
		this.resourceService = resourceService;
	}

	@Operation(summary = "Listar recursos", description = "Retorna a lista de recursos.")
	@ApiResponse(responseCode = "200", description = "Lista retornada com sucesso.")
	@GetMapping
	public ResponseEntity<List<ResourceBasicResponse>> findAll() {
		List<ResourceBasicResponse> result = resourceService.findAll().stream().map(ResourceBasicResponse::new)
				.collect(Collectors.toList());

		return ResponseEntity.ok(result);
	}

	@Operation(summary = "Buscar recurso por ID", description = "Retorna um recurso pelo identificador.")
	@ApiResponse(responseCode = "200", description = "Recurso encontrado com sucesso.")
	@ApiResponse(responseCode = "404", description = "Recurso nÃ£o encontrado.")
	@GetMapping("/{id}")
	public ResponseEntity<ResourceResponse> findById(@PathVariable Long id) {
		Resource resource = resourceService.findById(id);
		return ResponseEntity.ok(new ResourceResponse(resource));
	}

	@Operation(summary = "Criar recurso", description = "Cria um novo recurso.")
	@ApiResponse(responseCode = "201", description = "Recurso criado com sucesso.")
	@ApiResponse(responseCode = "400", description = "Payload invÃ¡lido.")
	@PostMapping
	public ResponseEntity<ResourceResponse> create(@Valid @RequestBody ResourceRequest payload) {
		Resource created = resourceService.create(payload);
		return ResponseEntity.status(HttpStatus.CREATED).body(new ResourceResponse(created));
	}

	@Operation(summary = "Atualizar recurso", description = "Atualiza um recurso existente.")
	@ApiResponse(responseCode = "200", description = "Recurso atualizado com sucesso.")
	@ApiResponse(responseCode = "404", description = "Recurso nÃ£o encontrado.")
	@PutMapping("/{id}")
	public ResponseEntity<ResourceResponse> update(@PathVariable Long id, @Valid @RequestBody ResourceRequest payload) {
		Resource updated = resourceService.update(id, payload);
		return ResponseEntity.ok(new ResourceResponse(updated));
	}

	@Operation(summary = "Alterar status do recurso", description = "Ativa ou inativa um recurso.")
	@ApiResponse(responseCode = "204", description = "Status alterado com sucesso.")
	@PatchMapping("/{id}/enable-disable/{active}")
	public ResponseEntity<Void> enableDisable(@PathVariable Long id, @PathVariable Boolean active) {
		resourceService.enableDisable(id, active);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Excluir recurso", description = "Remove um recurso pelo identificador.")
	@ApiResponse(responseCode = "204", description = "Recurso removido com sucesso.")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		resourceService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
