package com.br.food.migration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Order-independent description of a PostgreSQL schema (columns, constraints, indexes) taken from the
 * catalogs, used to compare a database built by Hibernate with one built by migrations.
 */
public final class SchemaSnapshot {

	private static final String COLUMNS = "SELECT c.relname, a.attname, format_type(a.atttypid, a.atttypmod), "
			+ "a.attnotnull, a.attidentity, pg_get_expr(d.adbin, d.adrelid) "
			+ "FROM pg_attribute a JOIN pg_class c ON c.oid = a.attrelid "
			+ "JOIN pg_namespace n ON n.oid = c.relnamespace "
			+ "LEFT JOIN pg_attrdef d ON d.adrelid = a.attrelid AND d.adnum = a.attnum "
			+ "WHERE n.nspname = 'public' AND c.relkind = 'r' AND a.attnum > 0 AND NOT a.attisdropped "
			+ "AND c.relname <> 'flyway_schema_history'";

	private static final String CONSTRAINTS = "SELECT c.relname, k.conname, pg_get_constraintdef(k.oid) "
			+ "FROM pg_constraint k JOIN pg_class c ON c.oid = k.conrelid "
			+ "JOIN pg_namespace n ON n.oid = c.relnamespace "
			+ "WHERE n.nspname = 'public' AND c.relname <> 'flyway_schema_history'";

	private static final String INDEXES = "SELECT tablename, indexdef FROM pg_indexes "
			+ "WHERE schemaname = 'public' AND tablename <> 'flyway_schema_history'";

	private static final String SEQUENCES = "SELECT sequencename FROM pg_sequences WHERE schemaname = 'public'";

	private static final Pattern CHECK_VALUE = Pattern.compile("'([^']*)'::character varying");
	private static final Pattern PRIMARY_KEY_COLUMNS = Pattern.compile("PRIMARY KEY \\(([^)]*)\\)");
	private static final Pattern INDEX_HEAD = Pattern.compile("CREATE (UNIQUE )?INDEX (\\S+) ON (.*)");

	private SchemaSnapshot() {
	}

	/**
	 * Drops what is irrelevant to the application: names of unique constraints/indexes (Hibernate
	 * generates different ones across versions), the textual shape of enum CHECK lists and the column
	 * order of composite primary keys.
	 */
	static String normalizeConstraint(String table, String name, String definition) {
		if (definition.startsWith("CHECK") && definition.contains("ARRAY[")) {
			TreeSet<String> values = new TreeSet<>();
			Matcher m = CHECK_VALUE.matcher(definition);
			while (m.find()) {
				values.add(m.group(1));
			}
			return "CONSTRAINT " + table + " " + name + " CHECK IN " + values;
		}
		if (definition.startsWith("UNIQUE")) {
			return "CONSTRAINT " + table + " " + definition;
		}
		Matcher pk = PRIMARY_KEY_COLUMNS.matcher(definition);
		if (pk.matches()) {
			String[] cols = pk.group(1).split(", ");
			Arrays.sort(cols);
			return "CONSTRAINT " + table + " " + name + " PRIMARY KEY " + Arrays.toString(cols);
		}
		return "CONSTRAINT " + table + " " + name + " " + definition;
	}

	static String normalizeIndex(String table, String definition) {
		Matcher m = INDEX_HEAD.matcher(definition);
		if (m.matches() && m.group(1) != null) {
			String rest = m.group(3);
			Matcher pk = Pattern.compile("^\\S+ USING btree \\(([^)]*)\\)$").matcher(rest);
			if (pk.matches() && m.group(2).endsWith("_pkey")) {
				String[] cols = pk.group(1).split(", ");
				Arrays.sort(cols);
				return "INDEX " + table + " PKEY " + Arrays.toString(cols);
			}
			return "INDEX " + table + " UNIQUE " + rest;
		}
		return "INDEX " + table + " " + definition;
	}

	public static List<String> take(Connection connection) throws SQLException {
		return take(connection, "public");
	}

	/** Same snapshot for any schema (a tenant schema must converge to the entities exactly like {@code public}). */
	public static List<String> take(Connection connection, String schema) throws SQLException {
		if (!schema.matches("[a-z_][a-z0-9_]*")) {
			throw new IllegalArgumentException("Unsafe schema name: " + schema);
		}
		String in = "'" + schema + "'";
		TreeSet<String> lines = new TreeSet<>();
		try (Statement st = connection.createStatement()) {
			try (ResultSet rs = st.executeQuery(COLUMNS.replace("'public'", in))) {
				while (rs.next()) {
					lines.add("COLUMN " + rs.getString(1) + "." + rs.getString(2) + " " + rs.getString(3)
							+ (rs.getBoolean(4) ? " NOT NULL" : " NULL")
							+ (rs.getString(5).isEmpty() ? "" : " IDENTITY(" + rs.getString(5) + ")")
							+ (rs.getString(6) == null ? "" : " DEFAULT " + rs.getString(6)));
				}
			}
			try (ResultSet rs = st.executeQuery(CONSTRAINTS.replace("'public'", in))) {
				while (rs.next()) {
					lines.add(normalizeConstraint(rs.getString(1), rs.getString(2), rs.getString(3)));
				}
			}
			try (ResultSet rs = st.executeQuery(INDEXES.replace("'public'", in))) {
				while (rs.next()) {
					lines.add(normalizeIndex(rs.getString(1), rs.getString(2)));
				}
			}
			try (ResultSet rs = st.executeQuery(SEQUENCES.replace("'public'", in))) {
				while (rs.next()) {
					lines.add("SEQUENCE " + rs.getString(1));
				}
			}
		}
		// index definitions are always printed schema-qualified, and the constraint catalogs qualify what is outside
		// the search_path: compare as if the schema were unnamed
		TreeSet<String> unqualified = new TreeSet<>();
		for (String line : lines) {
			unqualified.add(line.replace(schema + ".", ""));
		}
		return new ArrayList<>(unqualified);
	}
}
