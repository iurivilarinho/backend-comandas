package com.br.food.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
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
import com.br.food.util.excel.HttpHeadersUtil;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/financial")
public class FinancialController {

	private final FinancialService financialService;
	private final HttpHeadersUtil httpHeadersUtil;

	public FinancialController(FinancialService financialService, HttpHeadersUtil httpHeadersUtil) {
		this.financialService = financialService;
		this.httpHeadersUtil = httpHeadersUtil;
	}

	@Operation(summary = "Consultar visao financeira consolidada")
	@GetMapping("/overview")
	public ResponseEntity<FinancialOverviewResponse> overview(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			@RequestParam(required = false) FinanceEntryType type,
			@RequestParam(required = false) FinanceCategory category,
			Pageable pageable) {
		return ResponseEntity.ok(financialService.overview(startDate, endDate, type, category, pageable));
	}

	@Operation(summary = "Exportar relatorio financeiro em Excel")
	@GetMapping("/report")
	public ResponseEntity<byte[]> report(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			@RequestParam(required = false) FinanceEntryType type,
			@RequestParam(required = false) FinanceCategory category) throws Exception {
		HttpHeaders headers = httpHeadersUtil.excel("relatorio-financeiro-");
		return ResponseEntity.ok()
				.headers(headers)
				.body(financialService.exportReport(startDate, endDate, type, category));
	}

	@Operation(summary = "Registrar lancamento financeiro manual")
	@PostMapping("/entries")
	public ResponseEntity<FinancialEntryResponse> create(
			@Valid @RequestBody FinancialEntryRequest request,
			@RequestHeader(name = "X-Actor", required = false) String actorName) {
		return ResponseEntity.status(201).body(financialService.create(request, actorName));
	}
}
