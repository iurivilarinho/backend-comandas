package com.br.food.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class OrderChannelSchemaRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(OrderChannelSchemaRunner.class);

	private static final String EXPECTED_CHECK = "CHECK ((channel = ANY (ARRAY['DINE_IN'::text, 'DELIVERY'::text, 'TAKEAWAY'::text])))";

	private final JdbcTemplate jdbcTemplate;

	public OrderChannelSchemaRunner(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void run(ApplicationArguments args) {
		try {
			alignChannelCheck();
			relaxTableForeignKeyNullability();
		} catch (Exception ex) {
			log.warn("Skipping orders schema alignment: {}", ex.getMessage());
		}
	}

	private void alignChannelCheck() {
		List<String> existingChecks = jdbcTemplate.queryForList(
				"SELECT con.conname FROM pg_constraint con "
						+ "JOIN pg_class rel ON rel.oid = con.conrelid "
						+ "JOIN pg_attribute att ON att.attrelid = rel.oid AND att.attnum = ANY(con.conkey) "
						+ "WHERE rel.relname = 'orders' AND att.attname = 'channel' AND con.contype = 'c'",
				String.class);

		String currentDefinition = jdbcTemplate.queryForList(
				"SELECT pg_get_constraintdef(con.oid) FROM pg_constraint con "
						+ "JOIN pg_class rel ON rel.oid = con.conrelid "
						+ "JOIN pg_attribute att ON att.attrelid = rel.oid AND att.attnum = ANY(con.conkey) "
						+ "WHERE rel.relname = 'orders' AND att.attname = 'channel' AND con.contype = 'c' LIMIT 1",
				String.class).stream().findFirst().orElse(null);

		if (existingChecks.size() == 1 && EXPECTED_CHECK.equalsIgnoreCase(currentDefinition)) {
			return;
		}

		for (String name : existingChecks) {
			jdbcTemplate.execute("ALTER TABLE orders DROP CONSTRAINT IF EXISTS \"" + name + "\"");
		}

		jdbcTemplate.execute(
				"ALTER TABLE orders ADD CONSTRAINT orders_channel_check "
						+ "CHECK (channel IN ('DINE_IN', 'DELIVERY', 'TAKEAWAY'))");
		log.info("orders.channel CHECK constraint atualizada para incluir TAKEAWAY.");
	}

	private void relaxTableForeignKeyNullability() {
		Boolean isNullable = jdbcTemplate.queryForList(
				"SELECT is_nullable = 'YES' FROM information_schema.columns "
						+ "WHERE table_name = 'orders' AND column_name = 'fk_table_id'",
				Boolean.class).stream().findFirst().orElse(null);

		if (Boolean.TRUE.equals(isNullable)) {
			return;
		}

		jdbcTemplate.execute("ALTER TABLE orders ALTER COLUMN fk_table_id DROP NOT NULL");
		log.info("orders.fk_table_id agora aceita NULL para pedidos delivery/retirada.");
	}
}
