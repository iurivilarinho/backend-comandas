package com.br.food.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.br.food.models.CompanyProfile;
import com.br.food.platform.TestTenants;
import com.br.food.repository.CompanyProfileRepository;

import br.com.gloomy.platform.spi.TenantBranding;

class ComandasTenantBrandingProviderTest {

	private final CompanyProfileRepository repository = mock(CompanyProfileRepository.class);
	private final ComandasTenantBrandingProvider provider = new ComandasTenantBrandingProvider(repository);

	@Test
	void titleColorAndManifestNamesComeFromTheCompanyProfile() {
		CompanyProfile profile = new CompanyProfile();
		profile.setCompanyName("  Zil Restaurante ");
		profile.setPrimaryColor("#aa0000");
		when(repository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));

		TenantBranding branding = provider.branding(TestTenants.tenant("zil"));

		assertThat(branding.title()).isEqualTo("Zil Restaurante");
		assertThat(branding.themeColor()).isEqualTo("#aa0000");
		assertThat(branding.manifest().name()).isEqualTo("Zil Restaurante");
		assertThat(branding.manifest().shortName()).isEqualTo("Zil Restaurante");
	}

	@Test
	void withoutAProfileOrWithBlankValuesTheFrontKeepsItsDefaults() {
		when(repository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());
		assertThat(provider.branding(TestTenants.tenant("zil"))).isEqualTo(TenantBranding.empty());

		CompanyProfile blank = new CompanyProfile();
		blank.setCompanyName(" ");
		when(repository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(blank));
		assertThat(provider.branding(TestTenants.tenant("zil"))).isEqualTo(TenantBranding.empty());
	}
}
