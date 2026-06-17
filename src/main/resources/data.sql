-- Initial PostgreSQL seed data. Hibernate creates/updates the schema first;
-- this script is idempotent so repeated application starts are safe.

INSERT INTO app_user (id, created_at, email, enabled, full_name, username, password_hash)
VALUES
    (1, CURRENT_TIMESTAMP, 'f@o.bar', TRUE, 'Administrator', 'admin', '$2a$10$AkKbCDsN1zgKgwuKFybnGethDXhi9zPTlv2LSNDbhdL2mIXCJKM1O'),
    (2, CURRENT_TIMESTAMP, 'j@hn.doe', TRUE, 'Scientist', 'citizen', '$2a$10$EVhuOoej.LDmiaHr.yBQP.m4NMmSuJl0lZ4CjiXFJVREJ7W9Jwaxy')
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_roles (user_id, role)
SELECT seed.user_id, seed.role
FROM (VALUES
    (1, 'ADMIN'),
    (2, 'USER')
) AS seed(user_id, role)
WHERE NOT EXISTS (
    SELECT 1
    FROM user_roles existing
    WHERE existing.user_id = seed.user_id
      AND existing.role = seed.role
);

INSERT INTO menu (id, enabled, href, order_index, title, icon, parent_id)
VALUES
    (1, TRUE, '/dashboard', 1, 'Dashboard', NULL, NULL),
    (2, TRUE, '/projects', 2, 'Project', NULL, NULL),
    (3, TRUE, '/data', 3, 'Data', NULL, NULL),
    (4, TRUE, '/admin/users', 4, 'User', NULL, NULL),
    (5, TRUE, '/profile', 5, 'Profile', NULL, NULL)
ON CONFLICT (id) DO NOTHING;

INSERT INTO menu_roles (menu_id, role)
SELECT seed.menu_id, seed.role
FROM (VALUES
    (1, 'ADMIN'),
    (2, 'ADMIN'),
    (3, 'ADMIN'),
    (4, 'ADMIN'),
    (5, 'ADMIN'),
    (1, 'USER'),
    (2, 'USER'),
    (5, 'USER')
) AS seed(menu_id, role)
WHERE NOT EXISTS (
    SELECT 1
    FROM menu_roles existing
    WHERE existing.menu_id = seed.menu_id
      AND existing.role = seed.role
);

SELECT setval(pg_get_serial_sequence('app_user', 'id'), (SELECT MAX(id) FROM app_user), TRUE);
SELECT setval(pg_get_serial_sequence('menu', 'id'), (SELECT MAX(id) FROM menu), TRUE);
