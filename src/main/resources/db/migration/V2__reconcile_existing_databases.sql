-- CMD-016: brings every database to the schema the entities expect today. Idempotent, so it is safe on
--   * a database created by V1 (empty database),
--   * a database created by the old ddl-auto=update and baselined at V1, which only had the CHECK of orders.channel and the
--     nullability of orders.fk_table_id fixed at boot by the removed OrderChannelSchemaRunner.
-- The SQL does not qualify the schema: the platform runs it in the schema of each tenant.

-- orders.channel accepts TAKEAWAY (the old CHECK listed only DINE_IN and DELIVERY).
DO $$
DECLARE
    old_check RECORD;
BEGIN
    FOR old_check IN
        SELECT con.conname, pg_get_constraintdef(con.oid) AS definition
        FROM pg_constraint con
        JOIN pg_attribute att ON att.attrelid = con.conrelid AND att.attnum = ANY (con.conkey)
        WHERE con.conrelid = 'orders'::regclass AND con.contype = 'c' AND att.attname = 'channel'
    LOOP
        IF old_check.definition NOT LIKE '%TAKEAWAY%' THEN
            EXECUTE format('ALTER TABLE orders DROP CONSTRAINT %I', old_check.conname);
        END IF;
    END LOOP;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint con
        JOIN pg_attribute att ON att.attrelid = con.conrelid AND att.attnum = ANY (con.conkey)
        WHERE con.conrelid = 'orders'::regclass AND con.contype = 'c' AND att.attname = 'channel'
    ) THEN
        ALTER TABLE orders ADD CONSTRAINT orders_channel_check CHECK (channel IN ('DINE_IN', 'DELIVERY', 'TAKEAWAY'));
    END IF;
END
$$;

-- delivery and takeaway orders have no table.
ALTER TABLE orders ALTER COLUMN fk_table_id DROP NOT NULL;

-- CMD-015: version of the QR code of a table, so regenerating it invalidates the printed one.
ALTER TABLE dining_tables ADD COLUMN IF NOT EXISTS access_version integer NOT NULL DEFAULT 1;
ALTER TABLE dining_tables ALTER COLUMN access_version DROP DEFAULT;

-- CMD-016: the set-password link of a new administrator uses a 12 character code (the recovery code has 4 digits).
ALTER TABLE tbrecoverypassword ALTER COLUMN code TYPE varchar(16);

-- CMD-015: the administrator role used to be stored only in the description, leaving the name (the authority) empty.
UPDATE roles SET nome = 'ROLE_ADMIN' WHERE nome IS NULL AND description = 'ROLE_ADMIN';
