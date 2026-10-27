package com.br.food.platform;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import com.br.food.FoodApplication;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * CMD-016 test harness: boots the real application (the real {@code application.properties}, the real filters, a real
 * HTTP port) on an embedded PostgreSQL and talks to it with a {@code Host} header of choice, which is how the hosting
 * platform tells tenants apart.
 */
public final class PlatformApp implements AutoCloseable {

	public static final String LOCAL_TOKEN = "local-token-for-tests";
	public static final String JWT_SECRET = "test-only-jwt-secret-not-for-production-0123456789";
	public static final ObjectMapper JSON = new ObjectMapper().registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

	private static final Path MAIN_PROPERTIES = Path.of("src/main/resources/application.properties");

	/** A response, with the cookies it set. */
	public record Reply(int status, String body, Map<String, String> cookies, byte[] raw) {
		public JsonNode json() {
			try {
				return JSON.readTree(body);
			} catch (IOException e) {
				throw new IllegalStateException("Not JSON: " + body, e);
			}
		}
	}

	private final ConfigurableApplicationContext context;
	private final int port;
	private final CloseableHttpClient client = HttpClientBuilder.create().disableCookieManagement()
			.disableRedirectHandling().build();

	private PlatformApp(ConfigurableApplicationContext context) {
		this.context = context;
		this.port = Integer.parseInt(context.getEnvironment().getProperty("local.server.port"));
	}

	/** The own instance: {@code CENTRAL_MODE=off}, the single fixed tenant on the {@code public} schema. */
	public static PlatformApp startOwnerEdition(String jdbcUrl, Class<?>[] extraSources, String... overrides) {
		return start(jdbcUrl, extraSources, concat(new String[] { "--CENTRAL_MODE=off" }, overrides));
	}

	/** Central mode: one schema per tenant, the default address by path. The Central is never contacted. */
	public static PlatformApp startHosted(String jdbcUrl, Class<?>[] extraSources, String... overrides) {
		return startHosted("path", jdbcUrl, extraSources, overrides);
	}

	/** Central mode with the address by subdomain, the way the demo tenant is reached. */
	public static PlatformApp startHostedBySubdomain(String jdbcUrl, Class<?>[] extraSources, String... overrides) {
		return startHosted("subdomain", jdbcUrl, extraSources, overrides);
	}

	private static PlatformApp startHosted(String addressing, String jdbcUrl, Class<?>[] extraSources, String... overrides) {
		return start(jdbcUrl, extraSources, concat(new String[] { "--CENTRAL_MODE=central", "--APP_CODE=comandas",
				"--APP_BASE_DOMAIN=comandas.test", "--CENTRAL_URL=http://127.0.0.1:1",
				"--CENTRAL_CLIENT_ID=comandas-web", "--CENTRAL_CLIENT_SECRET=client-secret",
				"--CENTRAL_WEBHOOK_SECRET=whsec_MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
				"--PLATFORM_LOCAL_TOKEN=" + LOCAL_TOKEN, "--PLATFORM_TENANT_ADDRESSING=" + addressing }, overrides));
	}

	private static PlatformApp start(String jdbcUrl, Class<?>[] extraSources, String[] overrides) {
		String[] base = { "--spring.config.location=file:" + MAIN_PROPERTIES, "--spring.datasource.url=" + jdbcUrl,
				"--spring.datasource.username=postgres", "--spring.datasource.password=", "--server.port=0",
				"--spring.main.banner-mode=off", "--api.security.token.secret=" + JWT_SECRET,
				"--JWT_SECRET=" + JWT_SECRET, "--spring.mail.username=test@example.com",
				"--spring.mail.password=test", "--cors.allowed-origins=http://localhost:*" };
		List<Class<?>> sources = new ArrayList<>();
		sources.add(FoodApplication.class);
		sources.addAll(List.of(extraSources));
		ConfigurableApplicationContext context = new SpringApplicationBuilder(sources.toArray(Class<?>[]::new))
				.run(concat(base, overrides));
		return new PlatformApp(context);
	}

	private static String[] concat(String[] a, String[] b) {
		String[] out = new String[a.length + b.length];
		System.arraycopy(a, 0, out, 0, a.length);
		System.arraycopy(b, 0, out, a.length, b.length);
		return out;
	}

	public <T> T bean(Class<T> type) {
		return context.getBean(type);
	}

	@Override
	public void close() {
		try {
			client.close();
		} catch (IOException ignored) {
			// nothing to recover
		}
		context.close();
	}

	// ---- HTTP --------------------------------------------------------------------------------------------------

	public Reply get(String host, String path, String cookie) {
		return call("GET", host, path, null, cookie, Map.of());
	}

	public Reply postJson(String host, String path, Object body, String cookie) {
		return call("POST", host, path, body, cookie, Map.of());
	}

	public Reply putJson(String host, String path, Object body, String cookie) {
		return call("PUT", host, path, body, cookie, Map.of());
	}

	public Reply delete(String host, String path, String cookie) {
		return call("DELETE", host, path, null, cookie, Map.of());
	}

	/** Any call with extra request headers (the customer token, the Turnstile token, the nginx tenant header). */
	public Reply call(String method, String host, String path, Object body, String cookie, Map<String, String> headers) {
		HttpRequestBase request = switch (method) {
		case "GET" -> new HttpGet(url(path));
		case "POST" -> new HttpPost(url(path));
		case "PUT" -> new HttpPut(url(path));
		case "PATCH" -> new HttpPatch(url(path));
		case "DELETE" -> new HttpDelete(url(path));
		default -> throw new IllegalArgumentException(method);
		};
		if (body != null) {
			((HttpEntityEnclosingRequestBase) request).setEntity(new StringEntity(toJson(body), ContentType.APPLICATION_JSON));
		}
		headers.forEach(request::setHeader);
		return send(request, host, cookie);
	}

	public Reply getLocal(String path) {
		HttpGet request = new HttpGet(url(path));
		request.setHeader("X-Platform-Token", LOCAL_TOKEN);
		return send(request, "localhost", null);
	}

	public Reply postLocalJson(String path, Object body) {
		HttpPost request = new HttpPost(url(path));
		request.setHeader("X-Platform-Token", LOCAL_TOKEN);
		request.setEntity(new StringEntity(toJson(body), ContentType.APPLICATION_JSON));
		return send(request, "localhost", null);
	}

	private String url(String path) {
		return "http://127.0.0.1:" + port + path;
	}

	private static String toJson(Object body) {
		try {
			return body instanceof String text ? text : JSON.writeValueAsString(body);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private Reply send(HttpRequestBase request, String host, String cookie) {
		request.setHeader("Host", host);
		request.setHeader("Accept", "application/json, application/problem+json, */*");
		if (cookie != null) {
			request.setHeader("Cookie", cookie);
		}
		try (CloseableHttpResponse response = client.execute(request)) {
			byte[] bytes = response.getEntity() == null ? new byte[0] : EntityUtils.toByteArray(response.getEntity());
			Map<String, String> cookies = new LinkedHashMap<>();
			for (org.apache.http.Header header : response.getHeaders("Set-Cookie")) {
				String pair = header.getValue().split(";", 2)[0];
				int eq = pair.indexOf('=');
				if (eq > 0) {
					cookies.put(pair.substring(0, eq), pair.substring(eq + 1));
				}
			}
			return new Reply(response.getStatusLine().getStatusCode(), new String(bytes, StandardCharsets.UTF_8),
					cookies, bytes);
		} catch (IOException e) {
			throw new IllegalStateException("HTTP call failed: " + request.getURI(), e);
		}
	}
}
