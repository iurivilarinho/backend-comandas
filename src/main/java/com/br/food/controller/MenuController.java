package com.br.food.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.br.food.response.ProductResponse;
import com.br.food.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/menu")
@Tag(name = "Menu", description = "Public menu endpoints")
public class MenuController {

	private final ProductService productService;

	public MenuController(ProductService productService) {
		this.productService = productService;
	}

	@Operation(summary = "List menu products")
	@GetMapping("/products")
	public ResponseEntity<Page<ProductResponse>> findMenuProducts(
			Pageable pageable,
			@RequestParam(required = false) Long categoryId,
			@RequestParam(required = false) String term) {
		return ResponseEntity.ok(productService.findMenuProducts(pageable, categoryId, term));
	}
}
