package com.br.food.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

import com.br.food.request.ProductCategoryRequest;
import com.br.food.response.ProductCategoryResponse;
import com.br.food.service.ProductCategoryService;

import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/product-categories")
public class ProductCategoryController {

	private final ProductCategoryService productCategoryService;

	public ProductCategoryController(ProductCategoryService productCategoryService) {
		this.productCategoryService = productCategoryService;
	}

	@GetMapping
	public ResponseEntity<Page<ProductCategoryResponse>> findAll(
			Pageable pageable,
			@RequestParam(required = false) Boolean active,
			@RequestParam(required = false) String term) {
		return ResponseEntity.ok(productCategoryService.findAll(pageable, active, term).map(ProductCategoryResponse::new));
	}

	@PostMapping
	public ResponseEntity<ProductCategoryResponse> create(@Valid @RequestBody ProductCategoryRequest request) {
		return ResponseEntity.status(201).body(new ProductCategoryResponse(productCategoryService.create(request)));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ProductCategoryResponse> update(@PathVariable Long id,
			@Valid @RequestBody ProductCategoryRequest request) {
		return ResponseEntity.ok(new ProductCategoryResponse(productCategoryService.update(id, request)));
	}

	@PatchMapping("/{id}/status")
	public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestParam boolean active) {
		productCategoryService.updateStatus(id, active);
		return ResponseEntity.noContent().build();
	}
}
