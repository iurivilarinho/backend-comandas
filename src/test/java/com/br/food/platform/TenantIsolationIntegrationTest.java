package com.br.food.platform;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.br.food.enums.Types.ProductType;
import com.br.food.migration.EmbeddedPostgresSupport;
import com.br.food.models.CompanyProfile;
import com.br.food.models.DiningTable;
import com.br.food.models.Product;
import com.br.food.models.User;
import com.br.food.platform.CapturingMailConfig.CapturedMail;
import com.br.food.platform.PlatformApp.Reply;
import com.br.food.repository.CompanyProfileRepository;
import com.br.food.repository.DiningTableRepository;
import com.br.food.repository.ProductRepository;
import com.br.food.repository.UserRepository;

import br.com.gloomy.platform.model.TenantInfo;
import br.com.gloomy.platform.service.TenantResolver;
import br.com.gloomy.platform.tenant.TenantContext;

/**
 * CMD-016. Two restaurants in one database (central mode, schema per tenant, address by path) behind the real filters
 * and a real HTTP port: administrators created with a set-password e-mail, then proof that users, tokens, menu cache,
 * company profile and QR codes never cross from one tenant to the other, that plan limits come from the entitlements,
 * and how a suspended tenant behaves.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TenantIsolationIntegrationTest {

	private static final String BASE = "comandas.test";
	private static final String ZIL = "zil";
	private static final String JANTINHA = "jantinha";
	private static final String ADMIN_EMAIL = "boss@example.com";
	private static final String ZIL_PASSWORD = "Zil@1234pw";
	private static final String JANTINHA_PASSWORD = "Jantinha@1234pw";
	private static final Pattern LINK = Pattern
			.compile("href=\"https://comandas\\.test/([a-z]+)/recuperar-senha\\?email=([^&]+)&amp;code=([A-Za-z0-9]+)\"");

	private static String jdbcUrl;
	private static PlatformApp app;
	private static String zilCookie;
	private static String jantinhaCookie;

	@BeforeAll
	static void boot() throws Exception {
		// MAILS is static and shared: drop what earlier test classes captured, the set-password mails must be 0 and 1
		CapturingMailConfig.MAILS.clear();
		jdbcUrl = EmbeddedPostgresSupport.createDatabase("hosted");
		app = PlatformApp.startHosted(jdbcUrl, new Class<?>[] { CapturingMailConfig.class, TestPlanConfig.class });
	}

	@AfterAll
	static void shutdown() {
		if (app != null) {
			app.close();
		}
	}

	@BeforeEach
	void resetPlans() {
		TestPlanConfig.reset();
	}

	@Test
	@Order(1)
	void ownerCreatesTwoTenantsEachWithItsOwnAdministratorAndBaseData() throws Exception {
		assertThat(app.postLocalJson("/platform/local/tenants", tenantBody(ZIL, "Zil Restaurante", "Zil Boss")).status())
				.isEqualTo(201);
		assertThat(app.postLocalJson("/platform/local/tenants", tenantBody(JANTINHA, "Jantinha Familia", "Jantinha Boss"))
				.status()).isEqualTo(201);

		assertThat(scalar("SELECT count(*) FROM information_schema.schemata WHERE schema_name IN ('platform','t_zil','t_jantinha')"))
				.isEqualTo("3");
		assertThat(scalar("SELECT count(*) FROM information_schema.tables WHERE table_schema = 'public'")).isEqualTo("0");

		for (String schema : List.of("t_zil", "t_jantinha")) {
			assertThat(scalar("SELECT count(*) FROM " + schema + ".roles WHERE nome = 'ROLE_ADMIN'")).isEqualTo("1");
			assertThat(scalar("SELECT count(*) FROM " + schema + ".payment_methods")).isNotEqualTo("0");
			assertThat(scalar("SELECT count(*) FROM " + schema + ".users WHERE email = '" + ADMIN_EMAIL + "'"))
					.isEqualTo("1");
		}

		List<CapturedMail> mails = CapturingMailConfig.MAILS;
		assertThat(mails).extracting(CapturedMail::subject).containsExactly("Defina sua senha - Zil Restaurante",
				"Defina sua senha - Jantinha Familia");
		assertThat(link(mails.get(0)).slug).isEqualTo(ZIL);
		assertThat(link(mails.get(1)).slug).isEqualTo(JANTINHA);
		assertThat(link(mails.get(0)).code).hasSize(12);

		Reply tenant = get(ZIL, "/platform/tenant", null);
		assertThat(tenant.status()).isEqualTo(200);
		assertThat(tenant.json().path("name").asText()).isEqualTo("Zil Restaurante");
		assertThat(tenant.json().path("status").asText()).isEqualTo("active");
		assertThat(get("unknown", "/platform/tenant", null).status()).isEqualTo(404);
	}

	@Test
	@Order(2)
	void administratorsChooseThePasswordWithTheLinkOnceAndLogIn() {
		setPassword(CapturingMailConfig.MAILS.get(0), ZIL_PASSWORD);
		setPassword(CapturingMailConfig.MAILS.get(1), JANTINHA_PASSWORD);

		Reply zil = login(ZIL, ZIL_PASSWORD);
		Reply jantinha = login(JANTINHA, JANTINHA_PASSWORD);
		assertThat(zil.status()).isEqualTo(200);
		assertThat(jantinha.status()).isEqualTo(200);
		zilCookie = "token=" + zil.cookies().get("token");
		jantinhaCookie = "token=" + jantinha.cookies().get("token");

		assertThat(get(ZIL, "/user/me", zilCookie).json().path("email").asText()).isEqualTo(ADMIN_EMAIL);
		// the stored password is a hash, and a wrong one is refused
		assertThat(login(ZIL, JANTINHA_PASSWORD).status()).isEqualTo(403);
		// the link works once
		Link used = link(CapturingMailConfig.MAILS.get(0));
		Reply again = call("POST", ZIL, "/auth/password-recovery/reset",
				Map.of("email", used.email, "code", used.code, "newPassword", "Other@1234pw"), null);
		assertThat(again.status()).isEqualTo(403);
	}

	@Test
	@Order(3)
	void aTokenOfOneTenantDoesNotOpenTheOtherEvenWithTheSameUserId() {
		// both administrators are user 1 of their own schema
		assertThat(get(JANTINHA, "/user/me", zilCookie).status()).isEqualTo(401);
		assertThat(get(ZIL, "/user/me", jantinhaCookie).status()).isEqualTo(401);
		assertThat(get(JANTINHA, "/user/me", jantinhaCookie).status()).isEqualTo(200);
	}

	@Test
	@Order(4)
	void menuCompanyProfileAndTablesStayInTheirTenant() throws Exception {
		inTenant(ZIL, () -> {
			app.bean(DiningTableRepository.class).save(new DiningTable("1"));
			app.bean(ProductRepository.class).save(menuProduct("FIN-Z", "Burger do Zil"));
			CompanyProfile profile = new CompanyProfile();
			profile.setCompanyName("Zil Restaurante");
			profile.setPrimaryColor("#aa0000");
			profile.setDineInEnabled(true);
			profile.setDeliveryEnabled(true);
			profile.setTakeawayEnabled(true);
			app.bean(CompanyProfileRepository.class).save(profile);
		});
		inTenant(JANTINHA, () -> {
			app.bean(DiningTableRepository.class).save(new DiningTable("1"));
			app.bean(ProductRepository.class).save(menuProduct("FIN-J", "Pizza da Jantinha"));
		});

		// the first request fills the cache of each tenant; the second must not return the other tenant's data
		for (int round = 0; round < 2; round++) {
			Reply zilMenu = get(ZIL, "/menu/products", null);
			Reply jantinhaMenu = get(JANTINHA, "/menu/products", null);
			assertThat(zilMenu.json().path("content")).hasSize(1);
			assertThat(zilMenu.json().path("content").get(0).path("description").asText()).isEqualTo("Burger do Zil");
			assertThat(jantinhaMenu.json().path("content")).hasSize(1);
			assertThat(jantinhaMenu.json().path("content").get(0).path("description").asText())
					.isEqualTo("Pizza da Jantinha");
		}
		assertThat(get(ZIL, "/company-profile", null).json().path("companyName").asText()).isEqualTo("Zil Restaurante");
		assertThat(get(JANTINHA, "/company-profile", null).body()).doesNotContain("Zil");

		assertThat(get(ZIL, "/tables", zilCookie).json()).hasSize(1);
		assertThat(scalar("SELECT count(*) FROM t_jantinha.dining_tables")).isEqualTo("1");
	}

	@Test
	@Order(5)
	void brandingComesFromTheCompanyProfileOfEachTenant() {
		Reply zil = get(ZIL, "/platform/tenant", null);
		Reply jantinha = get(JANTINHA, "/platform/tenant", null);

		assertThat(zil.json().path("branding").path("title").asText()).isEqualTo("Zil Restaurante");
		assertThat(zil.json().path("branding").path("themeColor").asText()).isEqualTo("#aa0000");
		assertThat(jantinha.json().path("branding").path("themeColor").asText("")).isIn("", "null");
	}

	@Test
	@Order(6)
	void qrCodeAndCustomerTokensDoNotCrossTenantsEvenWithTheSameIds() {
		String zilQr = get(ZIL, "/tables/access-token?tableNumber=1", zilCookie).body();
		assertThat(get(ZIL, "/tables/resolve-access-token?token=" + zilQr, null).body()).isEqualTo("1");
		// table "1" exists in both tenants at version 1: only the tenant claim tells them apart
		assertThat(get(JANTINHA, "/tables/resolve-access-token?token=" + zilQr, null).status()).isEqualTo(403);

		Reply customer = call("POST", ZIL, "/customers",
				Map.of("name", "Maria", "documentNumber", "12345678901", "phone", "31999990001"), null);
		call("POST", JANTINHA, "/customers", Map.of("name", "Joao", "documentNumber", "12345678902", "phone", "31999990002"),
				null);
		String zilCustomerToken = customer.json().path("accessToken").asText();
		long id = customer.json().path("id").asLong();

		assertThat(customerGet(ZIL, "/customers/" + id, zilCustomerToken).status()).isEqualTo(200);
		assertThat(customerGet(JANTINHA, "/customers/" + id, zilCustomerToken).status()).isEqualTo(403);
	}

	@Test
	@Order(7)
	void planLimitsAndFeaturesComeFromTheEntitlementsOfEachTenant() throws Exception {
		TestPlanConfig.LIMITS.put(ZIL, Map.of("tables.max", 1L));
		TestPlanConfig.MISSING_FEATURES.add(ZIL + ":delivery");
		TestPlanConfig.MISSING_FEATURES.add(ZIL + ":financial");

		Reply blocked = call("POST", ZIL, "/tables", Map.of("number", "2"), zilCookie);
		assertThat(blocked.status()).isEqualTo(403);
		assertThat(blocked.json().path("type").asText()).isEqualTo("/problems/plan-limit-reached");
		assertThat(blocked.json().path("code").asText()).isEqualTo("tables.max");
		// the other tenant has no limit
		assertThat(call("POST", JANTINHA, "/tables", Map.of("number", "2"), jantinhaCookie).status()).isEqualTo(201);

		long productId = Long.parseLong(scalar("SELECT id FROM t_zil.product LIMIT 1"));
		Reply delivery = call("POST", ZIL, "/orders",
				Map.of("channel", "TAKEAWAY", "items", List.of(Map.of("productId", productId, "quantity", 1))), zilCookie);
		assertThat(delivery.status()).isEqualTo(403);
		assertThat(delivery.json().path("type").asText()).isEqualTo("/problems/feature-not-in-plan");
		assertThat(delivery.json().path("code").asText()).isEqualTo("delivery");

		Reply financial = get(ZIL, "/financial/overview", zilCookie);
		assertThat(financial.status()).isEqualTo(403);
		assertThat(financial.json().path("code").asText()).isEqualTo("financial");
		assertThat(get(JANTINHA, "/financial/overview", jantinhaCookie).status()).isNotEqualTo(403);
	}

	@Test
	@Order(8)
	void onlyTheAdministratorOfTheTenantManagesItsAddress() {
		inTenant(ZIL, () -> app.bean(UserRepository.class)
				.save(new User("Garcom", null, "garcom", "Garcom@1234pw", "garcom@example.com", null)));
		Reply waiter = call("POST", ZIL, "/auth/login", Map.of("login", "garcom", "password", "Garcom@1234pw"), null);
		assertThat(waiter.status()).isEqualTo(200);

		assertThat(get(ZIL, "/platform/tenant/domains", zilCookie).status()).isEqualTo(200);
		assertThat(get(ZIL, "/platform/tenant/domains", "token=" + waiter.cookies().get("token")).status())
				.isEqualTo(403);
		assertThat(get(ZIL, "/platform/tenant/domains", null).status()).isEqualTo(403);
		// the administrator of another tenant is not the administrator of this one
		assertThat(get(ZIL, "/platform/tenant/domains", jantinhaCookie).status()).isEqualTo(403);
	}

	@Test
	@Order(9)
	void aSuspendedTenantBlocksTheBusinessRoutesButTheAdministratorCanStillSignIn() throws Exception {
		execute("UPDATE platform.tenant SET status = 'suspended' WHERE slug = 'zil'");
		app.bean(TenantResolver.class).invalidate();

		Reply tenant = get(ZIL, "/platform/tenant", null);
		assertThat(tenant.status()).isEqualTo(200);
		assertThat(tenant.json().path("status").asText()).isEqualTo("suspended");

		Reply menu = get(ZIL, "/menu/products", null);
		assertThat(menu.status()).isEqualTo(402);
		assertThat(menu.json().path("type").asText()).isEqualTo("/problems/tenant-suspended");
		assertThat(get(ZIL, "/tables", zilCookie).status()).isEqualTo(402);

		// the administrator signs in and reads the own profile, to reach the "manage subscription" screen
		Reply login = login(ZIL, ZIL_PASSWORD);
		assertThat(login.status()).isEqualTo(200);
		assertThat(get(ZIL, "/user/me", "token=" + login.cookies().get("token")).status()).isEqualTo(200);

		// the other tenant is untouched
		assertThat(get(JANTINHA, "/menu/products", null).status()).isEqualTo(200);

		execute("UPDATE platform.tenant SET status = 'active' WHERE slug = 'zil'");
		app.bean(TenantResolver.class).invalidate();
		assertThat(get(ZIL, "/menu/products", null).status()).isEqualTo(200);
	}

	// ---- helpers ------------------------------------------------------------------------------------------------

	private static Reply get(String slug, String path, String cookie) {
		return call("GET", slug, path, null, cookie);
	}

	private static Reply call(String method, String slug, String path, Object body, String cookie) {
		return app.call(method, BASE, path, body, cookie, Map.of("X-Platform-Tenant", slug));
	}

	private static Reply customerGet(String slug, String path, String customerToken) {
		return app.call("GET", BASE, path, null, null, Map.of("X-Platform-Tenant", slug, "X-Customer-Token", customerToken));
	}

	private static Reply login(String slug, String password) {
		return call("POST", slug, "/auth/login", Map.of("login", ADMIN_EMAIL, "password", password), null);
	}

	private static Map<String, Object> tenantBody(String slug, String name, String adminName) {
		return Map.of("slug", slug, "name", name, "adminName", adminName, "adminEmail", ADMIN_EMAIL);
	}

	private record Link(String slug, String email, String code) {
	}

	private static Link link(CapturedMail mail) {
		Matcher m = LINK.matcher(mail.html());
		assertThat(m.find()).as(mail.html()).isTrue();
		return new Link(m.group(1), URLDecoder.decode(m.group(2), StandardCharsets.UTF_8), m.group(3));
	}

	private static void setPassword(CapturedMail mail, String password) {
		Link link = link(mail);
		Reply reset = call("POST", link.slug, "/auth/password-recovery/reset",
				Map.of("email", link.email, "code", link.code, "newPassword", password), null);
		assertThat(reset.status()).as(reset.body()).isEqualTo(204);
	}

	private static Product menuProduct(String code, String description) {
		Product product = new Product();
		product.setCode(code);
		product.setDescription(description);
		product.setType(ProductType.FINISHED);
		product.setPrice(new BigDecimal("25.00"));
		product.setActive(true);
		product.setComplement(false);
		product.setVisibleOnMenu(true);
		product.setSendToKitchen(false);
		product.setRequiresPreparation(false);
		return product;
	}

	private static void inTenant(String slug, Runnable action) {
		TenantInfo tenant = app.bean(TenantResolver.class).resolve(BASE, slug).orElseThrow();
		TenantContext.runAs(tenant, action);
	}

	private static String scalar(String sql) throws Exception {
		DataSource ds = EmbeddedPostgresSupport.dataSource(jdbcUrl);
		try (Connection c = ds.getConnection(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
			return rs.next() ? rs.getString(1) : null;
		}
	}

	private static void execute(String sql) throws Exception {
		DataSource ds = EmbeddedPostgresSupport.dataSource(jdbcUrl);
		try (Connection c = ds.getConnection(); Statement st = c.createStatement()) {
			st.execute(sql);
		}
	}
}
