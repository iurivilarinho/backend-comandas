package com.br.food.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.food.models.CompanyProfile;
import com.br.food.repository.CompanyProfileRepository;

import br.com.gloomy.platform.model.TenantInfo;
import br.com.gloomy.platform.spi.TenantBranding;
import br.com.gloomy.platform.spi.TenantBrandingProvider;
import br.com.gloomy.platform.spi.TenantManifest;

/**
 * CMD-016. What the hosting platform serves in {@code GET /platform/tenant}: the title, theme color and manifest names
 * of the restaurant, read from its company profile. Images are left to the front, which already knows the API base URL.
 */
@Service
public class ComandasTenantBrandingProvider implements TenantBrandingProvider {

	private final CompanyProfileRepository companyProfileRepository;

	public ComandasTenantBrandingProvider(CompanyProfileRepository companyProfileRepository) {
		this.companyProfileRepository = companyProfileRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public TenantBranding branding(TenantInfo tenant) {
		CompanyProfile profile = companyProfileRepository.findFirstByOrderByIdAsc().orElse(null);
		if (profile == null) {
			return TenantBranding.empty();
		}
		String name = blankToNull(profile.getCompanyName());
		String themeColor = blankToNull(profile.getPrimaryColor());
		if (name == null && themeColor == null) {
			return TenantBranding.empty();
		}
		return new TenantBranding(name, null, themeColor, null, new TenantManifest(name, name, null, null));
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
