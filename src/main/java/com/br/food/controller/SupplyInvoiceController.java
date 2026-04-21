package com.br.food.controller;

import java.io.IOException;
import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.br.food.enums.Types.SupplyInvoiceStatus;
import com.br.food.request.SupplyInvoiceRequest;
import com.br.food.response.SupplyInvoiceResponse;
import com.br.food.service.SupplyInvoiceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/supply-invoices")
@Tag(name = "Supply Invoices", description = "Endpoints for supply invoice management")
public class SupplyInvoiceController {

	private final SupplyInvoiceService supplyInvoiceService;

	public SupplyInvoiceController(SupplyInvoiceService supplyInvoiceService) {
		this.supplyInvoiceService = supplyInvoiceService;
	}

	@Operation(summary = "Create supply invoice")
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<SupplyInvoiceResponse> create(
			@Valid @RequestPart("request") SupplyInvoiceRequest request,
			@RequestPart(value = "attachment", required = false) MultipartFile attachment) throws IOException {
		return ResponseEntity.status(201).body(new SupplyInvoiceResponse(supplyInvoiceService.create(request, attachment)));
	}

	@Operation(summary = "Find supply invoice by id")
	@GetMapping("/{id}")
	public ResponseEntity<SupplyInvoiceResponse> findById(@PathVariable Long id) {
		return ResponseEntity.ok(new SupplyInvoiceResponse(supplyInvoiceService.findById(id)));
	}

	@Operation(summary = "List supply invoices")
	@GetMapping
	public ResponseEntity<Page<SupplyInvoiceResponse>> findAll(
			@RequestParam(required = false) String invoiceNumber,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate issueDateStart,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate issueDateEnd,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate launchDateStart,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate launchDateEnd,
			Pageable pageable) {
		return ResponseEntity.ok(
				supplyInvoiceService.findAll(invoiceNumber, issueDateStart, issueDateEnd, launchDateStart, launchDateEnd, pageable)
						.map(SupplyInvoiceResponse::new));
	}

	@Operation(summary = "Update supply invoice")
	@PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<SupplyInvoiceResponse> update(
			@PathVariable Long id,
			@Valid @RequestPart("request") SupplyInvoiceRequest request,
			@RequestPart(value = "attachment", required = false) MultipartFile attachment) throws IOException {
		return ResponseEntity.ok(new SupplyInvoiceResponse(supplyInvoiceService.update(id, request, attachment)));
	}

	@Operation(summary = "Update supply invoice status")
	@PatchMapping("/{id}/status")
	public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestParam SupplyInvoiceStatus status) {
		supplyInvoiceService.updateStatus(id, status);
		return ResponseEntity.noContent().build();
	}
}

