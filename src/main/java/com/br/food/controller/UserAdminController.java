package com.br.food.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.br.food.enums.Types.StaffRole;
import com.br.food.request.UserActiveRequest;
import com.br.food.request.UserInviteRequest;
import com.br.food.request.UserRoleRequest;
import com.br.food.response.UserAdminResponse;
import com.br.food.service.UserAdminService;
import com.br.food.util.PublicBaseUrl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/** CMD-017. Only the administrator reaches these routes (see {@code SecurityConfigurations}). */
@RestController
@Validated
@RequestMapping("/users")
@Tag(name = "Users", description = "Usuários da equipe: lista, convite, papel, situação e senha. Só o administrador.")
public class UserAdminController {

	private final UserAdminService userAdminService;

	public UserAdminController(UserAdminService userAdminService) {
		this.userAdminService = userAdminService;
	}

	@Operation(summary = "Lista os usuários com busca, filtro por papel e por situação")
	@GetMapping
	public ResponseEntity<Page<UserAdminResponse>> list(@RequestParam(required = false) String search,
			@RequestParam(required = false) StaffRole role, @RequestParam(required = false) Boolean active,
			@PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
		return ResponseEntity.ok(userAdminService.search(search, role, active, pageable));
	}

	@Operation(summary = "Convida um usuário: cria a conta com o papel e envia o link para definir a senha")
	@PostMapping
	public ResponseEntity<UserAdminResponse> invite(@Valid @RequestBody UserInviteRequest request,
			HttpServletRequest http) {
		return ResponseEntity.status(201).body(userAdminService.invite(request.name(), request.email(), request.role(),
				PublicBaseUrl.resolve(http)));
	}

	@Operation(summary = "Troca o papel do usuário")
	@PutMapping("/{id}/role")
	public ResponseEntity<UserAdminResponse> changeRole(@PathVariable Long id,
			@Valid @RequestBody UserRoleRequest request) {
		return ResponseEntity.ok(userAdminService.changeRole(id, request.role()));
	}

	@Operation(summary = "Ativa ou desativa o acesso do usuário")
	@PutMapping("/{id}/active")
	public ResponseEntity<UserAdminResponse> setActive(@PathVariable Long id,
			@Valid @RequestBody UserActiveRequest request) {
		return ResponseEntity.ok(userAdminService.setActive(id, request.active()));
	}

	@Operation(summary = "Envia ao usuário o link para definir uma nova senha")
	@PostMapping("/{id}/password-reset")
	public ResponseEntity<Void> sendPasswordLink(@PathVariable Long id, HttpServletRequest http) {
		userAdminService.sendPasswordLink(id, PublicBaseUrl.resolve(http));
		return ResponseEntity.noContent().build();
	}
}
