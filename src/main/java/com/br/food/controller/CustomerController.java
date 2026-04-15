package com.br.food.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.br.food.request.CustomerRequest;
import com.br.food.response.CustomerResponse;
import com.br.food.service.CustomerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/customers")
@Tag(name = "Customers", description = "Endpoints for customer management")
public class CustomerController {

	private final CustomerService customerService;

	public CustomerController(CustomerService customerService) {
		this.customerService = customerService;
	}

	@Operation(summary = "Create customer")
	@PostMapping
	public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
		return ResponseEntity.status(201).body(new CustomerResponse(customerService.create(request)));
	}

	@Operation(summary = "Find customer by id")
	@GetMapping("/{id}")
	public ResponseEntity<CustomerResponse> findById(@PathVariable Long id) {
		return ResponseEntity.ok(new CustomerResponse(customerService.findById(id)));
	}

	@Operation(summary = "List customers")
	@GetMapping
	public ResponseEntity<Page<CustomerResponse>> findAll(Pageable pageable) {
		return ResponseEntity.ok(customerService.findAll(pageable).map(CustomerResponse::new));
	}

	@Operation(summary = "Update customer")
	@PutMapping("/{id}")
	public ResponseEntity<CustomerResponse> update(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
		return ResponseEntity.ok(new CustomerResponse(customerService.update(id, request)));
	}

	@Operation(summary = "Update customer blocked status")
	@PatchMapping("/{id}/blocked")
	public ResponseEntity<Void> updateBlocked(@PathVariable Long id, @RequestParam Boolean blocked) {
		customerService.updateBlockedStatus(id, blocked);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Delete customer")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		customerService.delete(id);
		return ResponseEntity.noContent().build();
	}
}

