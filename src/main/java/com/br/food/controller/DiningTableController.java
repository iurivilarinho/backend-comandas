package com.br.food.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.br.food.request.DiningTableRequest;
import com.br.food.response.DiningTableResponse;
import com.br.food.service.DiningTableService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/tables")
@Tag(name = "Tables", description = "Endpoints for dining table management")
public class DiningTableController {

	private final DiningTableService diningTableService;

	public DiningTableController(DiningTableService diningTableService) {
		this.diningTableService = diningTableService;
	}

	@Operation(summary = "Create dining table")
	@PostMapping
	public ResponseEntity<DiningTableResponse> create(@Valid @RequestBody DiningTableRequest request) {
		return ResponseEntity.status(201).body(new DiningTableResponse(diningTableService.create(request)));
	}

	@Operation(summary = "Create multiple dining tables")
	@PostMapping("/batch")
	public ResponseEntity<List<DiningTableResponse>> createMany(@RequestParam Integer count) {
		return ResponseEntity.status(201)
				.body(diningTableService.createMany(count).stream().map(DiningTableResponse::new).toList());
	}

	@Operation(summary = "Find dining table by id")
	@GetMapping("/{id}")
	public ResponseEntity<DiningTableResponse> findById(@PathVariable Long id) {
		return ResponseEntity.ok(new DiningTableResponse(diningTableService.findById(id)));
	}

	@Operation(summary = "List dining tables")
	@GetMapping
	public ResponseEntity<List<DiningTableResponse>> findAll() {
		return ResponseEntity.ok(diningTableService.findAll().stream().map(DiningTableResponse::new).toList());
	}

	@Operation(summary = "Update dining table")
	@PutMapping("/{id}")
	public ResponseEntity<DiningTableResponse> update(@PathVariable Long id, @Valid @RequestBody DiningTableRequest request) {
		return ResponseEntity.ok(new DiningTableResponse(diningTableService.update(id, request)));
	}

	@Operation(summary = "Update dining table status")
	@PatchMapping("/{id}/status")
	public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestParam Boolean active) {
		diningTableService.updateStatus(id, active);
		return ResponseEntity.noContent().build();
	}
}

