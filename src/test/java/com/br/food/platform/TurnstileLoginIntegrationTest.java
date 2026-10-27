package com.br.food.platform;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.br.food.migration.EmbeddedPostgresSupport;
import com.br.food.platform.PlatformApp.Reply;
import com.sun.net.httpserver.HttpServer;

/**
 * PLT-033 on Comandas (CMD-016). The real application with the Turnstile keys set, talking to a local stand-in of
 * Cloudflare siteverify: login and the recovery request need a token, the recovery steps that carry the e-mailed code do
 * not, and an unreachable Cloudflare fails closed.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TurnstileLoginIntegrationTest {

	private static final String HOST = "localhost";
	private static final String HEADER = "X-Turnstile-Token";
	private static final Map<String, Object> LOGIN = Map.of("login", "nobody", "password", "Nobody@1234pw");

	private static HttpServer cloudflare;
	private static PlatformApp app;

	@BeforeAll
	static void boot() throws Exception {
		cloudflare = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		cloudflare.createContext("/siteverify", exchange -> {
			String form = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
			String json = form.contains("response=good") && form.contains("secret=secret-test")
					? "{\"success\":true}"
					: "{\"success\":false,\"error-codes\":[\"invalid-input-response\"]}";
			byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
			exchange.getResponseHeaders().add("Content-Type", "application/json");
			exchange.sendResponseHeaders(200, bytes.length);
			exchange.getResponseBody().write(bytes);
			exchange.close();
		});
		cloudflare.start();

		String jdbcUrl = EmbeddedPostgresSupport.createDatabase("turnstile");
		app = PlatformApp.startOwnerEdition(jdbcUrl, new Class<?>[0], "--turnstile.site-key=site-test",
				"--turnstile.secret-key=secret-test",
				"--turnstile.verify-url=http://127.0.0.1:" + cloudflare.getAddress().getPort() + "/siteverify");
	}

	@AfterAll
	static void shutdown() {
		if (app != null) {
			app.close();
		}
		cloudflare.stop(0);
	}

	@Test
	@Order(1)
	void theFrontLearnsThePublicKeyAndNeverTheSecret() {
		Reply reply = app.get(HOST, "/platform/turnstile", null);

		assertThat(reply.status()).as(reply.body()).isEqualTo(200);
		assertThat(reply.json().path("enabled").asBoolean()).isTrue();
		assertThat(reply.json().path("siteKey").asText()).isEqualTo("site-test");
		assertThat(reply.body()).doesNotContain("secret-test");
	}

	@Test
	@Order(2)
	void loginNeedsATokenAndANoTokenAttemptNeverReachesTheCredentialCheck() {
		Reply without = app.postJson(HOST, "/auth/login", LOGIN, null);
		assertThat(without.status()).isEqualTo(400);
		assertThat(without.json().path("code").asText()).isEqualTo("human-verification-required");

		Reply refused = app.call("POST", HOST, "/auth/login", LOGIN, null, Map.of(HEADER, "bad"));
		assertThat(refused.status()).isEqualTo(400);
		assertThat(refused.json().path("code").asText()).isEqualTo("human-verification-failed");

		// a good token gets to the credential check, which answers 401 for the unknown user
		Reply accepted = app.call("POST", HOST, "/auth/login", LOGIN, null, Map.of(HEADER, "good"));
		assertThat(accepted.status()).isEqualTo(401);
	}

	@Test
	@Order(3)
	void passwordRecoveryAsksForTheTokenOnlyWhereTheCodeIsNotInHand() {
		Reply send = app.postJson(HOST, "/auth/password-recovery/send", Map.of("email", "nobody@example.com"), null);
		assertThat(send.status()).isEqualTo(400);
		assertThat(send.json().path("code").asText()).isEqualTo("human-verification-required");

		// validate carries the e-mailed code: it is judged on the code, not on a token
		Reply validate = app.postJson(HOST, "/auth/password-recovery/validate",
				Map.of("email", "nobody@example.com", "code", "0000"), null);
		assertThat(validate.status()).isNotEqualTo(400).isNotEqualTo(503);
	}

	@Test
	@Order(4)
	void anUnreachableCloudflareFailsClosedWithA503() {
		cloudflare.stop(0);

		Reply reply = app.call("POST", HOST, "/auth/login", LOGIN, null, Map.of(HEADER, "good"));

		assertThat(reply.status()).isEqualTo(503);
		assertThat(reply.json().path("code").asText()).isEqualTo("human-verification-unavailable");
	}
}
