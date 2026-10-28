-- CMD-017: the five roles of the staff, and every user that exists today becomes administrator, which is what they
-- could do before roles. Idempotent; the SQL does not qualify the schema, so the platform runs it in every tenant.
INSERT INTO roles (status, nome, description)
SELECT true, wanted.nome, wanted.description
FROM (VALUES ('ROLE_ADMIN', 'ROLE_ADMIN'),
             ('ROLE_MANAGER', 'Gerente'),
             ('ROLE_CASHIER', 'Caixa'),
             ('ROLE_WAITER', 'Garçom'),
             ('ROLE_KITCHEN', 'Cozinha')) AS wanted (nome, description)
WHERE NOT EXISTS (SELECT 1 FROM roles existing WHERE existing.nome = wanted.nome);

INSERT INTO user_roles (fk_id_user, fk_id_role)
SELECT u.id, admin.id
FROM users u
CROSS JOIN (SELECT id FROM roles WHERE nome = 'ROLE_ADMIN' ORDER BY id LIMIT 1) admin
WHERE NOT EXISTS (SELECT 1
                  FROM user_roles ur
                  JOIN roles r ON r.id = ur.fk_id_role
                  WHERE ur.fk_id_user = u.id
                    AND r.nome IN ('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_CASHIER', 'ROLE_WAITER', 'ROLE_KITCHEN'));
