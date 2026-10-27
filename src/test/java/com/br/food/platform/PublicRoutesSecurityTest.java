package com.br.food.platform;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.br.food.enums.Types.ProductType;
import com.br.food.migration.EmbeddedPostgresSupport;
import com.br.food.models.DiningTable;
import com.br.food.models.Product;
import com.br.food.platform.PlatformApp.Reply;
import com.br.food.repository.DiningTableRepository;
import com.br.food.repository.ProductRepository;

import br.com.gloomy.platform.model.TenantInfo;
import br.com.gloomy.platform.tenant.TenantContext;

/**
 * CMD-015. The own instance behind the real filter chain and a real HTTP port: what an anonymous visitor, a customer
 * of the digital menu and the staff can and cannot reach.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PublicRoutesSecurityTest {

	private static final String HOST = "localhost";
	private static final String ADMIN_PASSWORD = "Admin@12345pw";
	private static final String CUSTOMER_HEADER = "X-Customer-Token";

	private static PlatformApp app;
	private static String staffCookie;
	private static Long productId;
	private static String customerOneToken;
	private static Long customerOneId;
	private static String customerTwoToken;
	private static Long customerTwoId;

	@BeforeAll
	static void boot() throws Exception {
		String jdbcUrl = EmbeddedPostgresSupport.createDatabase("routes");
		app = PlatformApp.startOwnerEdition(jdbcUrl, new Class<?>[0], "--comandas.admin.initial-password=" + ADMIN_PASSWORD);

		Reply login = app.postJson(HOST, "/auth/login", Map.of("login", "admin", "password", ADMIN_PASSWORD), null);
		assertThat(login.status()).isEqualTo(200);
		staffCookie = "token=" + login.cookies().get("token");

		try (TenantContext.Scope ignored = TenantContext.bind(TenantInfo.owner("Test"))) {
			DiningTable table = new DiningTable("10");
			app.bean(DiningTableRepository.class).save(table);
			Product product = new Product();
			product.setCode("FIN-1");
			product.setDescription("Burger");
			product.setType(ProductType.FINISHED);
			product.setPrice(new BigDecimal("25.00"));
			product.setCostPrice(new BigDecimal("9.00"));
			product.setActive(true);
			product.setComplement(false);
			product.setVisibleOnMenu(true);
			product.setSendToKitchen(false);
			product.setRequiresPreparation(false);
			productId = app.bean(ProductRepository.class).save(product).getId();
		}
	}

	@AfterAll
	static void shutdown() {
		if (app != null) {
			app.close();
		}
	}

	@Test
	@Order(1)
	void anonymousVisitorCannotReadOrdersCustomersOrCostsOrTheQrToken() {
		for (String path : List.of("/orders", "/orders/1", "/customers/1", "/customers/by-document?documentNumber=1",
				"/customers/by-phone?phone=1", "/products/" + productId + "/cost-history",
				"/tables/access-token?tableNumber=10", "/tables", "/tables/1")) {
			assertThat(app.get(HOST, path, null).status()).as("GET " + path).isIn(401, 403);
		}
		assertThat(app.putJson(HOST, "/customers/1", customerBody("A", "11111111111", "31999990001"), null).status())
				.isIn(401, 403);
		assertThat(app.postJson(HOST, "/push/subscriptions", pushBody("kitchen", null), null).status()).isIn(401, 403);
		assertThat(app.postJson(HOST, "/digital-orders", Map.of("customerId", 1, "channel", "TAKEAWAY", "items", List.of(Map.of("productId", 1, "quantity", 1))), null)
				.status())
				.isIn(401, 403);
	}

	@Test
	@Order(2)
	void protectedRouteWithoutLoginAnswers401WithJson() {
		Reply reply = app.get(HOST, "/user/me", null);

		assertThat(reply.status()).isEqualTo(401);
		assertThat(reply.json().path("message").get(0).asText()).contains("Invalid or expired token");
	}

	@Test
	@Order(3)
	void tokensOfOtherSystemsAndOtherTenantsAreRefused() {
		Algorithm sameSecret = Algorithm.HMAC256(PlatformApp.JWT_SECRET);
		String otherSystem = JWT.create().withIssuer("API Authentication").withSubject("1").sign(sameSecret);
		String otherTenant = JWT.create().withIssuer("comandas").withSubject("1").withClaim("tenant", "somebody-else")
				.sign(sameSecret);
		String noTenant = JWT.create().withIssuer("comandas").withSubject("1").sign(sameSecret);

		for (String forged : List.of(otherSystem, otherTenant, noTenant)) {
			assertThat(app.get(HOST, "/user/me", "token=" + forged).status()).isEqualTo(401);
		}
		assertThat(app.get(HOST, "/user/me", staffCookie).status()).isEqualTo(200);
	}

	@Test
	@Order(4)
	void costPriceIsHiddenFromVisitorsAndShownToStaff() {
		Reply visitor = app.get(HOST, "/products/" + productId, null);
		Reply staff = app.get(HOST, "/products/" + productId, staffCookie);

		assertThat(visitor.status()).isEqualTo(200);
		assertThat(visitor.json().path("price").decimalValue()).isEqualByComparingTo("25.00");
		assertThat(visitor.json().path("costPrice").isNull()).isTrue();
		assertThat(staff.json().path("costPrice").decimalValue()).isEqualByComparingTo("9.00");
		assertThat(app.get(HOST, "/products/" + productId + "/cost-history", staffCookie).status()).isEqualTo(200);
	}

	@Test
	@Order(5)
	void customerRegistersAndGetsATokenThatOpensOnlyOwnRegistration() {
		Reply one = app.postJson(HOST, "/customers", customerBody("Customer One", "12345678901", "31999990001"), null);
		Reply two = app.postJson(HOST, "/customers", customerBody("Customer Two", "12345678902", "31999990002"), null);
		assertThat(one.status()).isEqualTo(201);
		customerOneId = one.json().path("id").asLong();
		customerOneToken = one.json().path("accessToken").asText();
		customerTwoId = two.json().path("id").asLong();
		customerTwoToken = two.json().path("accessToken").asText();
		assertThat(customerOneToken).isNotBlank();

		assertThat(customerGet("/customers/" + customerOneId, customerOneToken).status()).isEqualTo(200);
		assertThat(customerGet("/customers/" + customerTwoId, customerOneToken).status()).isEqualTo(403);
		assertThat(customerGet("/customers/" + customerOneId, "forged").status()).isEqualTo(403);
		assertThat(customerGet("/customers/" + customerOneId, null).status()).isIn(401, 403);

		Reply update = app.call("PUT", HOST, "/customers/" + customerOneId,
				customerBody("Customer One Renamed", "12345678901", "31999990001"), null, Map.of(CUSTOMER_HEADER, customerOneToken));
		assertThat(update.status()).isEqualTo(200);
		assertThat(update.json().path("name").asText()).isEqualTo("Customer One Renamed");
		assertThat(app.call("PUT", HOST, "/customers/" + customerTwoId, customerBody("Hijacked", "12345678902", "31999990002"),
				null, Map.of(CUSTOMER_HEADER, customerOneToken)).status()).isEqualTo(403);

		// the staff reads any customer without a token
		assertThat(app.get(HOST, "/customers/" + customerTwoId, staffCookie).status()).isEqualTo(200);
	}

	@Test
	@Order(6)
	void identifyNeedsPhoneAndCpfThatMatchAndSaysNothingElse() {
		Reply right = app.postJson(HOST, "/customers/identify",
				Map.of("phone", "(31) 99999-0001", "documentNumber", "123.456.789-01"), null);
		assertThat(right.status()).isEqualTo(200);
		assertThat(right.json().path("id").asLong()).isEqualTo(customerOneId);
		assertThat(right.json().path("accessToken").asText()).isNotBlank();

		Reply wrongPhone = app.postJson(HOST, "/customers/identify",
				Map.of("phone", "31999990009", "documentNumber", "12345678901"), null);
		Reply unknownCpf = app.postJson(HOST, "/customers/identify",
				Map.of("phone", "31999990001", "documentNumber", "00000000000"), null);
		assertThat(wrongPhone.status()).isEqualTo(404);
		assertThat(unknownCpf.status()).isEqualTo(404);
		assertThat(wrongPhone.body()).isEqualTo(unknownCpf.body());
	}

	@Test
	@Order(7)
	void dineInOrderNeedsTheCustomerTokenAndTheCurrentTableQr() {
		String firstQr = app.get(HOST, "/tables/access-token?tableNumber=10", staffCookie).body();
		assertThat(firstQr).isNotBlank();
		assertThat(app.get(HOST, "/tables/resolve-access-token?token=" + firstQr, null).body()).isEqualTo("10");

		Map<String, Object> order = dineInOrder(customerOneId, firstQr, "999");
		// without the customer token, with another customer's token: refused
		assertThat(app.postJson(HOST, "/digital-orders", order, null).status()).isIn(401, 403);
		assertThat(digitalOrder(order, customerTwoToken).status()).isEqualTo(403);
		// without a table token, with a forged one: refused
		assertThat(digitalOrder(dineInOrder(customerOneId, null, "10"), customerOneToken).status()).isEqualTo(403);
		assertThat(digitalOrder(dineInOrder(customerOneId, "forged", "10"), customerOneToken).status()).isEqualTo(403);

		// the table comes from the QR, never from the body
		Reply created = digitalOrder(order, customerOneToken);
		assertThat(created.status()).isEqualTo(201);
		assertThat(created.json().path("table").path("number").asText()).isEqualTo("10");
		long orderId = created.json().path("id").asLong();

		// only the owner reads the order and asks for the bill
		assertThat(customerGet("/orders/" + orderId, customerOneToken).status()).isEqualTo(200);
		assertThat(customerGet("/orders/" + orderId, customerTwoToken).status()).isEqualTo(403);
		assertThat(customerGet("/orders/" + orderId, null).status()).isIn(401, 403);
		assertThat(app.call("POST", HOST, "/orders/" + orderId + "/request-close", Map.of("paymentMethod", "PIX"), null,
				Map.of(CUSTOMER_HEADER, customerTwoToken)).status()).isEqualTo(403);

		// the list is the own orders only
		assertThat(customerGet("/orders?customerId=" + customerOneId, customerOneToken).json().path("content").size())
				.isEqualTo(1);
		assertThat(customerGet("/orders?customerId=" + customerTwoId, customerOneToken).status()).isEqualTo(403);
		assertThat(customerGet("/orders", customerTwoToken).json().path("content").size()).isZero();

		// regenerating the QR kills the printed one
		long tableId = app.get(HOST, "/tables/resolve-access-token?token=" + firstQr, null).status() == 200
				? tableIdOfNumber10() : -1;
		Reply regenerated = app.call("POST", HOST, "/tables/" + tableId + "/access-token/regenerate", null, staffCookie, Map.of());
		assertThat(regenerated.status()).isEqualTo(200);
		assertThat(app.get(HOST, "/tables/resolve-access-token?token=" + firstQr, null).status()).isEqualTo(403);
		assertThat(app.get(HOST, "/tables/resolve-access-token?token=" + regenerated.body(), null).body()).isEqualTo("10");
		assertThat(app.call("POST", HOST, "/tables/" + tableId + "/access-token/regenerate", null, null, Map.of()).status())
				.isIn(401, 403);
	}

	@Test
	@Order(8)
	void pushSubscriptionsOfStaffTopicsNeedStaffAndTheCustomerOneNeedsItsToken() {
		assertThat(app.postJson(HOST, "/push/subscriptions", pushBody("kitchen", null), null).status()).isIn(401, 403);
		assertThat(app.postJson(HOST, "/push/subscriptions", pushBody("kitchen", null), staffCookie).status()).isEqualTo(204);

		assertThat(app.call("POST", HOST, "/push/subscriptions", pushBody("customer", customerOneId), null,
				Map.of(CUSTOMER_HEADER, customerOneToken)).status()).isEqualTo(204);
		assertThat(app.call("POST", HOST, "/push/subscriptions", pushBody("customer", customerTwoId), null,
				Map.of(CUSTOMER_HEADER, customerOneToken)).status()).isEqualTo(403);
		assertThat(app.postJson(HOST, "/push/subscriptions", pushBody("customer", customerOneId), null).status())
				.isIn(401, 403);
	}

	@Test
	@Order(9)
	void ownInstanceAnswersOnAnyHostWithoutTheCentral() {
		Reply tenant = app.get("restaurante.example.com", "/platform/tenant", null);

		assertThat(tenant.status()).isEqualTo(200);
		assertThat(tenant.json().path("ownerEdition").asBoolean()).isTrue();
		assertThat(app.get("restaurante.example.com", "/company-profile", null).status()).isIn(200, 204);
	}

	private Reply customerGet(String path, String customerToken) {
		return app.call("GET", HOST, path, null, null,
				customerToken == null ? Map.of() : Map.of(CUSTOMER_HEADER, customerToken));
	}

	private Reply digitalOrder(Map<String, Object> order, String customerToken) {
		return app.call("POST", HOST, "/digital-orders", order, null, Map.of(CUSTOMER_HEADER, customerToken));
	}

	private Map<String, Object> dineInOrder(Long customerId, String tableAccessToken, String tableNumber) {
		Map<String, Object> order = new java.util.LinkedHashMap<>();
		order.put("customerId", customerId);
		order.put("tableNumber", tableNumber);
		if (tableAccessToken != null) {
			order.put("tableAccessToken", tableAccessToken);
		}
		order.put("channel", "DINE_IN");
		order.put("discountPercentage", 0);
		order.put("items", List.of(Map.of("productId", productId, "quantity", 1)));
		return order;
	}

	private long tableIdOfNumber10() {
		for (com.fasterxml.jackson.databind.JsonNode table : app.get(HOST, "/tables", staffCookie).json()) {
			if ("10".equals(table.path("number").asText())) {
				return table.path("id").asLong();
			}
		}
		throw new IllegalStateException("table 10 not found");
	}

	private static Map<String, Object> customerBody(String name, String cpf, String phone) {
		return Map.of("name", name, "documentNumber", cpf, "phone", phone);
	}

	private static Map<String, Object> pushBody(String topic, Long customerId) {
		Map<String, Object> body = new java.util.LinkedHashMap<>();
		body.put("endpoint", "https://push.example.com/" + topic + "/" + customerId);
		body.put("p256dh", "key");
		body.put("auth", "auth");
		body.put("topic", topic);
		if (customerId != null) {
			body.put("customerId", customerId);
		}
		return body;
	}
}
