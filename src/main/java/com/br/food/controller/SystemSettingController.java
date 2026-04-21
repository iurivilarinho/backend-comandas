package com.br.food.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.br.food.request.FinancialSettingsRequest;
import com.br.food.response.FinancialSettingsResponse;
import com.br.food.service.SystemSettingService;

import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/system-settings")
public class SystemSettingController {

	private final SystemSettingService systemSettingService;

	public SystemSettingController(SystemSettingService systemSettingService) {
		this.systemSettingService = systemSettingService;
	}

	@GetMapping("/financial")
	public ResponseEntity<FinancialSettingsResponse> getFinancialSettings() {
		return ResponseEntity.ok(systemSettingService.getFinancialSettings());
	}

	@PutMapping("/financial")
	public ResponseEntity<FinancialSettingsResponse> updateFinancialSettings(
			@Valid @RequestBody FinancialSettingsRequest request) {
		systemSettingService.upsert(
				SystemSettingService.SERVICE_FEE_PERCENT,
				request.getServiceFeePercent().toPlainString());
		return ResponseEntity.ok(systemSettingService.getFinancialSettings());
	}
}
