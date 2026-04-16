package com.br.food.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.br.food.request.StockEntryRequest;
import com.br.food.response.StockEntryResponse;
import com.br.food.service.StockEntryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/stock")
@Tag(name = "Stock", description = "Endpoints for stock entry management")
public class StockEntryController {

	private final StockEntryService stockEntryService;

	public StockEntryController(StockEntryService stockEntryService) {
		this.stockEntryService = stockEntryService;
	}

	@Operation(summary = "List stock entries")
	@GetMapping
	public ResponseEntity<Page<StockEntryResponse>> search(
			@RequestParam(required = false) String productCode,
			@RequestParam(required = false) String term,
			Pageable pageable) {
		return ResponseEntity.ok(stockEntryService.search(productCode, term, pageable).map(StockEntryResponse::new));
	}

	@Operation(summary = "Create manual stock entry")
	@PostMapping
	public ResponseEntity<StockEntryResponse> create(@Valid @RequestBody StockEntryRequest request) {
		return ResponseEntity.status(201).body(new StockEntryResponse(stockEntryService.create(request)));
	}

	@Operation(summary = "Retain or release a stock entry")
	@PatchMapping("/{id}/retention")
	public ResponseEntity<StockEntryResponse> updateRetention(
			@PathVariable Long id,
			@RequestParam boolean retained) {
		return ResponseEntity.ok(new StockEntryResponse(stockEntryService.updateRetention(id, retained)));
	}
}

