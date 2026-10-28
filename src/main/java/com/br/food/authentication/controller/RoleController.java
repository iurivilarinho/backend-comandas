package com.br.food.authentication.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.br.food.authentication.models.Role;
import com.br.food.authentication.response.RoleResponse;
import com.br.food.authentication.service.RoleService;
import com.br.food.request.RoleRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/role")
@Tag(name = "Perfil", description = "Endpoints responsáveis pelo gerenciamento de perfis")
public class RoleController {

	private final RoleService roleService;

	public RoleController(RoleService roleService) {
		this.roleService = roleService;
	}

	@Operation(summary = "Listar perfis", description = "Retorna a lista de perfis. Pode filtrar pelo status quando informado.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Perfis listados com sucesso") })
	@GetMapping
	public ResponseEntity<List<RoleResponse>> findAll(
			@Parameter(description = "Status do perfil para filtro. Quando não informado, retorna todos os perfis.") @RequestParam(required = false) Boolean status) {
		return ResponseEntity.ok(roleService.findAll(status).stream().map(RoleResponse::new).toList());
	}

	@Operation(summary = "Buscar perfil por ID", description = "Retorna os dados de um perfil com base no identificador informado.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Perfil encontrado com sucesso"),
			@ApiResponse(responseCode = "404", description = "Perfil não encontrado", content = @Content) })
	@GetMapping("/{id}")
	public ResponseEntity<RoleResponse> findById(
			@Parameter(description = "ID do perfil", required = true) @PathVariable Long id) {
		return ResponseEntity.ok(new RoleResponse(roleService.findById(id)));
	}

	@Operation(summary = "Cadastrar perfil", description = "Cria um novo perfil com os dados informados.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Perfil criado com sucesso"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos para criação do perfil", content = @Content) })
	@PostMapping
	public ResponseEntity<Role> create(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados para criação do perfil", required = true) @RequestBody RoleRequest form) {
		return ResponseEntity.ok(roleService.create(form));
	}

	@Operation(summary = "Atualizar perfil", description = "Atualiza os dados de um perfil existente.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Perfil atualizado com sucesso"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos para atualização do perfil", content = @Content),
			@ApiResponse(responseCode = "404", description = "Perfil não encontrado", content = @Content) })
	@PutMapping("/{id}")
	public ResponseEntity<Role> update(@Parameter(description = "ID do perfil", required = true) @PathVariable Long id,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados para atualização do perfil", required = true) @RequestBody RoleRequest form) {
		return ResponseEntity.ok(roleService.update(form, id));
	}

	@Operation(summary = "Alterar status do perfil", description = "Ativa ou desativa um perfil com base no status informado.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Status do perfil alterado com sucesso"),
			@ApiResponse(responseCode = "404", description = "Perfil não encontrado", content = @Content) })
	@PutMapping("/status/{status}/{id}")
	public ResponseEntity<?> enableDisable(
			@Parameter(description = "ID do perfil", required = true) @PathVariable Long id,
			@Parameter(description = "Status do perfil. true para ativar, false para desativar", required = true) @PathVariable Boolean status) {
		roleService.enableDisable(id, status);
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "Vincular perfil ao usuário", description = "Realiza o vínculo de um perfil com um usuário.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Perfil vinculado ao usuário com sucesso"),
			@ApiResponse(responseCode = "404", description = "Usuário ou perfil não encontrado", content = @Content) })
	@PatchMapping("/link/{userId}/{roleId}")
	public ResponseEntity<?> linkRoleWithUser(
			@Parameter(description = "ID do usuário", required = true) @PathVariable Long userId,
			@Parameter(description = "ID do perfil", required = true) @PathVariable Long roleId) {
		roleService.linkRoleWithUser(userId, roleId);
		return ResponseEntity.ok().build();
	}

	@Operation(summary = "Desvincular perfil do usuário", description = "Remove o vínculo entre um perfil e um usuário.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Perfil desvinculado do usuário com sucesso"),
			@ApiResponse(responseCode = "404", description = "Usuário ou perfil não encontrado", content = @Content) })
	@PatchMapping("/unlink/{userId}/{roleId}")
	public ResponseEntity<?> unlinkRoleWithUser(
			@Parameter(description = "ID do usuário", required = true) @PathVariable Long userId,
			@Parameter(description = "ID do perfil", required = true) @PathVariable Long roleId) {
		roleService.unlinkRoleWithUser(userId, roleId);
		return ResponseEntity.ok().build();
	}
}

