package com.br.food.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.br.food.response.ProductProfitOverviewResponse;
import com.br.food.service.ProfitabilityService;
import com.br.food.util.excel.HttpHeadersUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Validated
@RequestMapping("/product-profit")
@Tag(name = "Product Profit", description = "Per-product profitability analytics")
public class ProductProfitController {

	private final ProfitabilityService profitabilityService;
	private final HttpHeadersUtil httpHeadersUtil;

	public ProductProfitController(ProfitabilityService profitabilityService, HttpHeadersUtil httpHeadersUtil) {
		this.profitabilityService = profitabilityService;
		this.httpHeadersUtil = httpHeadersUtil;
	}

	@Operation(summary = "Consultar lucratividade por produto")
	@ApiResponse(responseCode = "200", description = "Overview returned successfully")
	@GetMapping
	public ResponseEntity<ProductProfitOverviewResponse> overview(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			@RequestParam(required = false) Long categoryId,
			@RequestParam(required = false) String term) {
		return ResponseEntity.ok(profitabilityService.overview(startDate, endDate, categoryId, term));
	}

	@Operation(summary = "Exportar lucratividade por produto em Excel")
	@GetMapping("/report")
	public ResponseEntity<byte[]> report(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			@RequestParam(required = false) Long categoryId,
			@RequestParam(required = false) String term) throws Exception {
		HttpHeaders headers = httpHeadersUtil.excel("lucratividade-produtos-");
		return ResponseEntity.ok().headers(headers)
				.body(profitabilityService.exportReport(startDate, endDate, categoryId, term));
	}
}
