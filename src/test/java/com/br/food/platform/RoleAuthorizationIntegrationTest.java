package com.br.food.platform;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.br.food.authentication.models.Role;
import com.br.food.enums.Types.StaffRole;
import com.br.food.migration.EmbeddedPostgresSupport;
import com.br.food.models.User;
import com.br.food.platform.CapturingMailConfig.CapturedMail;
import com.br.food.platform.PlatformApp.Reply;
import com.br.food.repository.RoleRepository;
import com.br.food.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;

import br.com.gloomy.platform.model.TenantInfo;
import br.com.gloomy.platform.tenant.TenantContext;

/**
 * CMD-017. One signed-in user per role against the real filter chain: what each role may reach, and the users screen
 * an administrator uses to give the roles (invite, change role, disable, last administrator).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RoleAuthorizationIntegrationTest {

	private static final String HOST = "localhost";
	private static final String PASSWORD = "Staff@1234pw";

	private static PlatformApp app;
	private static final Map<StaffRole, String> COOKIES = new EnumMap<>(StaffRole.class);

	/** One route and the roles that may call it. Any role not listed must get 403; a listed one anything but 401/403. */
	private record Route(String method, String path, List<StaffRole> allowed) {
	}

	private static final List<StaffRole> ADMIN = List.of(StaffRole.ADMIN);
	private static final List<StaffRole> MANAGEMENT = List.of(StaffRole.ADMIN, StaffRole.MANAGER);
	private static final List<StaffRole> CASHIER_UP = List.of(StaffRole.ADMIN, StaffRole.MANAGER, StaffRole.CASHIER);
	private static final List<StaffRole> FRONT = List.of(StaffRole.ADMIN, StaffRole.MANAGER, StaffRole.CASHIER,
			StaffRole.WAITER);
	private static final List<StaffRole> KITCHEN_UP = List.of(StaffRole.ADMIN, StaffRole.MANAGER, StaffRole.KITCHEN);
	private static final List<StaffRole> ALL = List.of(StaffRole.values());

	private static final List<Route> MATRIX = List.of(
			new Route("GET", "/user/me", ALL),
			new Route("GET", "/users", ADMIN),
			new Route("POST", "/users", ADMIN),
			new Route("PUT", "/users/1/role", ADMIN),
			new Route("GET", "/role", ADMIN),
			new Route("PATCH", "/role/link/1/1", ADMIN),
			new Route("POST", "/auth/permissions", ADMIN),
			new Route("GET", "/auth/resources", ADMIN),
			new Route("GET", "/kitchen/pending", ALL),
			new Route("POST", "/kitchen/accept/999", KITCHEN_UP),
			new Route("POST", "/kitchen/mark-ready/999", KITCHEN_UP),
			new Route("POST", "/orders", FRONT),
			new Route("PUT", "/orders/999", FRONT),
			new Route("POST", "/orders/999/items", FRONT),
			new Route("POST", "/orders/999/items/1/serve", FRONT),
			new Route("POST", "/orders/999/transfer", FRONT),
			new Route("POST", "/orders/999/close", CASHIER_UP),
			new Route("POST", "/orders/999/service-fee", CASHIER_UP),
			new Route("POST", "/orders/999/reopen", CASHIER_UP),
			new Route("DELETE", "/orders/999", CASHIER_UP),
			new Route("GET", "/orders", FRONT),
			new Route("GET", "/tables", FRONT),
			new Route("GET", "/tables/999", FRONT),
			new Route("POST", "/tables", MANAGEMENT),
			new Route("POST", "/tables/batch", MANAGEMENT),
			new Route("GET", "/tables/access-token?tableNumber=1", MANAGEMENT),
			new Route("POST", "/tables/999/access-token/regenerate", MANAGEMENT),
			new Route("GET", "/customers", FRONT),
			new Route("GET", "/customers/by-document?documentNumber=1", FRONT),
			new Route("PATCH", "/customers/999/blocked?blocked=true", MANAGEMENT),
			new Route("DELETE", "/customers/999", MANAGEMENT),
			new Route("POST", "/products", MANAGEMENT),
			new Route("PUT", "/products/999", MANAGEMENT),
			new Route("GET", "/products/999/cost-history", MANAGEMENT),
			new Route("POST", "/product-categories", MANAGEMENT),
			new Route("POST", "/promotions", MANAGEMENT),
			new Route("PUT", "/company-profile", MANAGEMENT),
			new Route("GET", "/stock", MANAGEMENT),
			new Route("POST", "/stock", MANAGEMENT),
			new Route("GET", "/supply-invoices", MANAGEMENT),
			new Route("GET", "/financial/overview", MANAGEMENT),
			new Route("POST", "/financial/entries", MANAGEMENT),
			new Route("GET", "/product-profit", MANAGEMENT),
			new Route("GET", "/system-settings/financial", FRONT),
			new Route("PUT", "/system-settings/financial", MANAGEMENT),
			new Route("GET", "/push/diagnostics", MANAGEMENT),
			new Route("POST", "/push/test?endpoint=x", MANAGEMENT));

	@BeforeAll
	static void boot() throws Exception {
		CapturingMailConfig.MAILS.clear();
		String jdbcUrl = EmbeddedPostgresSupport.createDatabase("roles");
		app = PlatformApp.startOwnerEdition(jdbcUrl, new Class<?>[] { CapturingMailConfig.class });

		try (TenantContext.Scope ignored = TenantContext.bind(TenantInfo.owner("Test"))) {
			for (StaffRole staffRole : StaffRole.values()) {
				Role role = app.bean(RoleRepository.class).findByNameIgnoreCase(staffRole.authority()).orElseThrow();
				User user = new User(staffRole.label(), null, staffRole.name().toLowerCase(), PASSWORD,
						staffRole.name().toLowerCase() + "@example.com", null);
				user.getRoles().add(role);
				app.bean(UserRepository.class).save(user);
			}
		}
		for (StaffRole staffRole : StaffRole.values()) {
			Reply login = app.postJson(HOST, "/auth/login",
					Map.of("login", staffRole.name().toLowerCase(), "password", PASSWORD), null);
			assertThat(login.status()).as(staffRole.name()).isEqualTo(200);
			COOKIES.put(staffRole, "token=" + login.cookies().get("token"));
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
	void theSeedCreatesTheFiveRoles() throws Exception {
		try (TenantContext.Scope ignored = TenantContext.bind(TenantInfo.owner("Test"))) {
			for (StaffRole staffRole : StaffRole.values()) {
				assertThat(app.bean(RoleRepository.class).findByNameIgnoreCase(staffRole.authority())).isPresent();
			}
		}
	}

	@Test
	@Order(2)
	void eachRoleReachesOnlyWhatTheMatrixAllows() {
		for (Route route : MATRIX) {
			for (StaffRole role : StaffRole.values()) {
				Reply reply = app.call(route.method(), HOST, route.path(), bodyOf(route), COOKIES.get(role), Map.of());
				String what = route.method() + " " + route.path() + " as " + role;
				if (route.allowed().contains(role)) {
					assertThat(reply.status()).as(what).isNotIn(401, 403);
				} else {
					assertThat(reply.status()).as(what).isEqualTo(403);
				}
			}
			Reply anonymous = app.call(route.method(), HOST, route.path(), bodyOf(route), null, Map.of());
			assertThat(anonymous.status()).as(route.method() + " " + route.path() + " anonymous").isIn(401, 403);
		}
	}

	@Test
	@Order(3)
	void aUserWithoutAnyStaffRoleReachesNothingButItsOwnProfile() throws Exception {
		try (TenantContext.Scope ignored = TenantContext.bind(TenantInfo.owner("Test"))) {
			app.bean(UserRepository.class)
					.save(new User("Sem papel", null, "norole", PASSWORD, "norole@example.com", null));
		}
		Reply login = app.postJson(HOST, "/auth/login", Map.of("login", "norole", "password", PASSWORD), null);
		String cookie = "token=" + login.cookies().get("token");

		assertThat(app.get(HOST, "/user/me", cookie).status()).isEqualTo(200);
		Reply denied = app.get(HOST, "/tables", cookie);
		assertThat(denied.status()).as(denied.body() + denied.cookies()).isEqualTo(403);
		assertThat(app.get(HOST, "/kitchen/pending", cookie).status()).isEqualTo(403);
	}

	@Test
	@Order(4)
	void theKitchenAndTheWaiterCannotUseTheCustomerRoutesAsStaff() {
		// the customer routes are open to the filter chain; the guard still keeps the kitchen out
		assertThat(app.get(HOST, "/customers/1", COOKIES.get(StaffRole.KITCHEN)).status()).isEqualTo(403);
		assertThat(app.get(HOST, "/customers/1", COOKIES.get(StaffRole.WAITER)).status()).isEqualTo(404);
		assertThat(app.get(HOST, "/orders/1", COOKIES.get(StaffRole.KITCHEN)).status()).isEqualTo(403);
	}

	@Test
	@Order(5)
	void theAdministratorInvitesChangesTheRoleAndDisablesFromTheUsersScreen() {
		String admin = COOKIES.get(StaffRole.ADMIN);

		Reply invited = app.call("POST", HOST, "/users", Map.of("name", "Novo Garçom", "email", "Novo@Example.com",
				"role", "WAITER"), admin, Map.of("X-Forwarded-Proto", "https", "X-Forwarded-Prefix", "/zil"));
		assertThat(invited.status()).as(invited.body()).isEqualTo(201);
		long id = invited.json().path("id").asLong();
		assertThat(invited.json().path("role").asText()).isEqualTo("WAITER");
		assertThat(invited.json().path("email").asText()).isEqualTo("novo@example.com");

		CapturedMail mail = CapturingMailConfig.MAILS.get(CapturingMailConfig.MAILS.size() - 1);
		assertThat(mail.to()).isEqualTo("novo@example.com");
		assertThat(mail.html()).contains("https://localhost/zil/recuperar-senha?email=novo%40example.com&amp;code=");

		// an e-mail already registered is refused
		assertThat(app.postJson(HOST, "/users", Map.of("name", "Outro", "email", "novo@example.com", "role", "KITCHEN"), admin)
				.status()).isEqualTo(409);

		Reply promoted = app.putJson(HOST, "/users/" + id + "/role", Map.of("role", "CASHIER"), admin);
		assertThat(promoted.json().path("role").asText()).isEqualTo("CASHIER");

		Reply disabled = app.putJson(HOST, "/users/" + id + "/active", Map.of("active", false), admin);
		assertThat(disabled.json().path("active").asBoolean()).isFalse();

		Reply list = app.get(HOST, "/users?search=novo&role=CASHIER&active=false", admin);
		assertThat(list.json().path("content")).hasSize(1);
		JsonNode first = list.json().path("content").get(0);
		assertThat(first.path("self").asBoolean()).isFalse();

		assertThat(app.call("POST", HOST, "/users/" + id + "/password-reset", null, admin, Map.of()).status())
				.isEqualTo(204);
	}

	@Test
	@Order(6)
	void theAdministratorKeepsHisOwnRoleAndAccess() {
		String admin = COOKIES.get(StaffRole.ADMIN);
		long selfId = app.get(HOST, "/user/me", admin).json().path("id").asLong();

		assertThat(app.putJson(HOST, "/users/" + selfId + "/role", Map.of("role", "WAITER"), admin).status())
				.isEqualTo(409);
		assertThat(app.putJson(HOST, "/users/" + selfId + "/active", Map.of("active", false), admin).status())
				.isEqualTo(409);
		assertThat(app.get(HOST, "/users", admin).json().path("content").findValues("self")).anyMatch(JsonNode::asBoolean);
	}

	@Test
	@Order(7)
	void aManagerCannotReachTheUsersScreenEvenToPromoteHimself() {
		long managerId = app.get(HOST, "/user/me", COOKIES.get(StaffRole.MANAGER)).json().path("id").asLong();

		assertThat(app.putJson(HOST, "/users/" + managerId + "/role", Map.of("role", "ADMIN"),
				COOKIES.get(StaffRole.MANAGER)).status()).isEqualTo(403);
	}

	private static Map<String, Object> bodyOf(Route route) {
		return List.of("POST", "PUT", "PATCH").contains(route.method()) ? Map.of() : null;
	}
}
