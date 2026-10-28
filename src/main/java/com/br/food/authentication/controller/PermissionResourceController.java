package com.br.food.authentication.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.br.food.authentication.models.PermissionResource;
import com.br.food.authentication.request.PermissionRequest;
import com.br.food.authentication.response.PermissionResponse;
import com.br.food.authentication.service.PermissionResourceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Permissions", description = "Endpoints para gerenciamento de permissões por role e recurso.")
@RestController
@RequestMapping("/auth/permissions")
public class PermissionResourceController {

	private final PermissionResourceService permissionService;

	public PermissionResourceController(PermissionResourceService permissionService) {
		this.permissionService = permissionService;
	}

	@Operation(summary = "Listar permissões", description = "Retorna a lista de permissões cadastradas.")
	@ApiResponse(responseCode = "200", description = "Lista retornada com sucesso.")
	@GetMapping
	public ResponseEntity<List<PermissionResponse>> findAll(@RequestParam(required = false) Long resourceId) {
		List<PermissionResponse> result = permissionService.findAll(resourceId).stream().map(PermissionResponse::new)
				.collect(Collectors.toList());

		return ResponseEntity.ok(result);
	}

	@Operation(summary = "Buscar permissão por ID", description = "Retorna uma permissão pelo identificador.")
	@ApiResponse(responseCode = "200", description = "Permissão encontrada com sucesso.")
	@ApiResponse(responseCode = "404", description = "Permissão não encontrada.")
	@GetMapping("/{id}")
	public ResponseEntity<PermissionResponse> findById(@PathVariable Long id) {
		PermissionResource permission = permissionService.findById(id);
		return ResponseEntity.ok(new PermissionResponse(permission));
	}

	@Operation(summary = "Criar permissão", description = "Cria um vínculo de permissões (CRUD) entre role e recurso.")
	@ApiResponse(responseCode = "201", description = "Permissão criada com sucesso.")
	@ApiResponse(responseCode = "400", description = "Payload inválido.")
	@PostMapping
	public ResponseEntity<PermissionResponse> create(@Valid @RequestBody PermissionRequest payload) {
		PermissionResource created = permissionService.create(payload);
		return ResponseEntity.status(HttpStatus.CREATED).body(new PermissionResponse(created));
	}

	@Operation(summary = "Atualizar permissão", description = "Atualiza um vínculo de permissões (CRUD).")
	@ApiResponse(responseCode = "200", description = "Permissão atualizada com sucesso.")
	@ApiResponse(responseCode = "404", description = "Permissão não encontrada.")
	@PutMapping("/{id}")
	public ResponseEntity<PermissionResponse> update(@PathVariable Long id,
			@Valid @RequestBody PermissionRequest payload) {
		PermissionResource updated = permissionService.update(id, payload);
		return ResponseEntity.ok(new PermissionResponse(updated));
	}

	@Operation(summary = "Excluir permissão", description = "Remove uma permissão pelo identificador.")
	@ApiResponse(responseCode = "204", description = "Permissão removida com sucesso.")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		permissionService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
