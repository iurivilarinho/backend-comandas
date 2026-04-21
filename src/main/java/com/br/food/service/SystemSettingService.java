package com.br.food.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.models.SystemSetting;
import com.br.food.repository.SystemSettingRepository;

@Service
public class SystemSettingService {

	public static final String SERVICE_FEE_PERCENT = "SERVICE_FEE_PERCENT";
	public static final String COVER_CHARGE_AMOUNT = "COVER_CHARGE_AMOUNT";

	private final SystemSettingRepository systemSettingRepository;

	public SystemSettingService(SystemSettingRepository systemSettingRepository) {
		this.systemSettingRepository = systemSettingRepository;
	}

	@Transactional(readOnly = true)
	public BigDecimal getDecimal(String key, BigDecimal defaultValue) {
		return systemSettingRepository.findByKey(key)
				.map(SystemSetting::getValue)
				.map(BigDecimal::new)
				.orElse(defaultValue);
	}

	@Transactional
	public void upsert(String key, String value) {
		SystemSetting setting = systemSettingRepository.findByKey(key).orElseGet(() -> new SystemSetting(key, value));
		setting.setValue(value);
		systemSettingRepository.save(setting);
	}

	@Transactional
	public void createIfAbsent(String key, String value) {
		if (systemSettingRepository.findByKey(key).isPresent()) {
			return;
		}
		systemSettingRepository.save(new SystemSetting(key, value));
	}
}
