package com.br.food.migration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

/** In-process PostgreSQL (no Docker) shared by the migration tests of one JVM. */
public final class EmbeddedPostgresSupport {

	private static EmbeddedPostgres postgres;

	private EmbeddedPostgresSupport() {
	}

	public static synchronized EmbeddedPostgres server() throws IOException {
		if (postgres == null) {
			postgres = EmbeddedPostgres.builder().start();
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				try {
					postgres.close();
				} catch (IOException ignored) {
					// best effort cleanup of the temporary data directory
				}
			}));
		}
		return postgres;
	}

	/** Creates a new empty database and returns its JDBC URL. */
	public static String createDatabase(String prefix) throws IOException, SQLException {
		String name = prefix + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
		try (Connection c = server().getPostgresDatabase().getConnection(); Statement st = c.createStatement()) {
			st.execute("CREATE DATABASE " + name);
		}
		return "jdbc:postgresql://localhost:" + server().getPort() + "/" + name + "?user=postgres";
	}

	public static DataSource dataSource(String jdbcUrl) {
		org.postgresql.ds.PGSimpleDataSource ds = new org.postgresql.ds.PGSimpleDataSource();
		ds.setUrl(jdbcUrl);
		return ds;
	}

	/** Loads a pg_dump schema-only file, dropping the psql meta-command lines (those starting with a backslash). */
	public static void loadDump(DataSource ds, String classpathResource) throws IOException, SQLException {
		String sql;
		try (InputStream in = EmbeddedPostgresSupport.class.getResourceAsStream(classpathResource)) {
			if (in == null) {
				throw new IOException("Missing test resource " + classpathResource);
			}
			sql = new String(in.readAllBytes(), StandardCharsets.UTF_8).lines()
					.filter(line -> !line.startsWith("\\")).collect(Collectors.joining("\n"));
		}
		try (Connection c = ds.getConnection()) {
			ScriptUtils.executeSqlScript(c, new ByteArrayResource(sql.getBytes(StandardCharsets.UTF_8)));
			try (Statement st = c.createStatement()) {
				st.execute("SELECT pg_catalog.set_config('search_path', 'public', false)");
			}
		}
	}

}
