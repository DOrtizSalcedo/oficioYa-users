-- Roles del sistema. Se insertan solo si no existen (se ejecuta en cada arranque).
INSERT INTO roles (id, name) SELECT CAST('8f2b8a6e-0c1a-4d6e-9a51-1f0a7c3e0001' AS UUID), 'TRABAJADOR'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'TRABAJADOR');
INSERT INTO roles (id, name) SELECT CAST('8f2b8a6e-0c1a-4d6e-9a51-1f0a7c3e0002' AS UUID), 'CONTRATANTE'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'CONTRATANTE');
INSERT INTO roles (id, name) SELECT CAST('8f2b8a6e-0c1a-4d6e-9a51-1f0a7c3e0003' AS UUID), 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ADMIN');
