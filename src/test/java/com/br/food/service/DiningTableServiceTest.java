package com.br.food.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import com.br.food.config.PlanFeatures;
import com.br.food.models.DiningTable;
import com.br.food.platform.TestTenants;
import com.br.food.repository.DiningTableRepository;
import com.br.food.repository.OrderRepository;
import com.br.food.request.DiningTableRequest;

import br.com.gloomy.platform.entitlements.Entitlements;
import br.com.gloomy.platform.exception.PlanLimitReachedException;
import br.com.gloomy.platform.tenant.TenantContext;

@ExtendWith(MockitoExtension.class)
class DiningTableServiceTest {

	private static final String SECRET = "test-only-jwt-secret-not-for-production-0123456789";

	@Mock
	private DiningTableRepository diningTableRepository;

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private Entitlements entitlements;

	private DiningTableService service;
	private TenantContext.Scope scope;

	@BeforeEach
	void setUp() {
		scope = TenantContext.bind(TestTenants.tenant("zil"));
		service = new DiningTableService(diningTableRepository, orderRepository, new TableAccessTokenService(SECRET),
				entitlements);
	}

	@AfterEach
	void tearDown() {
		scope.close();
	}

	@Test
	void regeneratingTheQrInvalidatesTheTokenPrintedBefore() {
		DiningTable table = table("10", true);
		when(diningTableRepository.findByNumber("10")).thenReturn(Optional.of(table));
		when(diningTableRepository.findById(1L)).thenReturn(Optional.of(table));
		String printed = service.generateAccessToken("10");
		assertThat(service.resolveAccessToken(printed)).isEqualTo("10");

		String regenerated = service.regenerateAccessToken(1L);

		assertThat(table.getAccessVersion()).isEqualTo(2);
		assertThat(service.resolveAccessToken(regenerated)).isEqualTo("10");
		assertThatThrownBy(() -> service.resolveAccessToken(printed)).isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void aTokenOfAnInactiveOrUnknownTableIsRefused() {
		DiningTable table = table("10", true);
		when(diningTableRepository.findByNumber("10")).thenReturn(Optional.of(table));
		String token = service.generateAccessToken("10");

		table.setActive(false);
		assertThatThrownBy(() -> service.findByAccessToken(token)).isInstanceOf(AccessDeniedException.class);

		when(diningTableRepository.findByNumber("10")).thenReturn(Optional.empty());
		assertThatThrownBy(() -> service.findByAccessToken(token)).isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void creatingATableStopsAtThePlanLimit() {
		when(diningTableRepository.count()).thenReturn(10L);
		doThrow(new PlanLimitReachedException(PlanFeatures.TABLES_MAX, 10, 10)).when(entitlements)
				.requireBelow(PlanFeatures.TABLES_MAX, 10L);

		assertThatThrownBy(() -> service.create(new DiningTableRequest())).isInstanceOf(PlanLimitReachedException.class);

		verify(diningTableRepository, never()).save(any());
	}

	@Test
	void creatingManyTablesCountsAllOfThemAgainstThePlanLimit() {
		when(diningTableRepository.count()).thenReturn(8L);
		doThrow(new PlanLimitReachedException(PlanFeatures.TABLES_MAX, 10, 10)).when(entitlements)
				.requireBelow(PlanFeatures.TABLES_MAX, 10L);

		assertThatThrownBy(() -> service.createMany(3)).isInstanceOf(PlanLimitReachedException.class);

		verify(diningTableRepository, never()).saveAll(any());
	}

	private DiningTable table(String number, boolean active) {
		DiningTable table = new DiningTable(number);
		table.setActive(active);
		ReflectionTestUtils.setField(table, "id", 1L);
		return table;
	}
}
