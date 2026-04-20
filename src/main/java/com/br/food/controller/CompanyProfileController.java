package com.br.food.controller;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.br.food.request.CompanyProfileRequest;
import com.br.food.response.CompanyProfileResponse;
import com.br.food.service.CompanyProfileService;

import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/company-profile")
public class CompanyProfileController {

	private final CompanyProfileService companyProfileService;

	public CompanyProfileController(CompanyProfileService companyProfileService) {
		this.companyProfileService = companyProfileService;
	}

	@GetMapping
	public ResponseEntity<CompanyProfileResponse> findCurrent() {
		if (companyProfileService.findCurrent() == null) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(new CompanyProfileResponse(companyProfileService.findCurrent()));
	}

	@PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<CompanyProfileResponse> upsert(
			@Valid @RequestPart("request") CompanyProfileRequest request,
			@RequestPart(value = "logo", required = false) MultipartFile logo,
			@RequestPart(value = "banner", required = false) MultipartFile banner) throws IOException {
		return ResponseEntity.ok(new CompanyProfileResponse(companyProfileService.upsert(request, logo, banner)));
	}
}
