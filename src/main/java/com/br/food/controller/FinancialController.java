package com.br.food.controller;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.br.food.enums.Types.FinanceCategory;
import com.br.food.enums.Types.FinanceEntryType;
import com.br.food.request.FinancialEntryRequest;
import com.br.food.response.FinancialEntryResponse;
import com.br.food.response.FinancialOverviewResponse;
import com.br.food.service.FinancialService;

import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/financial")
public class FinancialController {

	private final FinancialService financialService;

	public FinancialController(FinancialService financialService) {
		this.financialService = financialService;
	}

	@GetMapping("/overview")
	public ResponseEntity<FinancialOverviewResponse> overview(
			@RequestParam(required = false) LocalDate startDate,
			@RequestParam(required = false) LocalDate endDate,
			@RequestParam(required = false) FinanceEntryType type,
			@RequestParam(required = false) FinanceCategory category) {
		return ResponseEntity.ok(financialService.overview(startDate, endDate, type, category));
	}

	@PostMapping("/entries")
	public ResponseEntity<FinancialEntryResponse> create(
			@Valid @RequestBody FinancialEntryRequest request,
			@RequestHeader(name = "X-Actor", required = false) String actorName) {
		return ResponseEntity.status(201).body(financialService.create(request, actorName));
	}
}
