-- Roles del sistema. Se insertan solo si no existen (se ejecuta en cada arranque).
INSERT INTO roles (id, name) SELECT CAST('8f2b8a6e-0c1a-4d6e-9a51-1f0a7c3e0001' AS UUID), 'TRABAJADOR'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'TRABAJADOR');
INSERT INTO roles (id, name) SELECT CAST('8f2b8a6e-0c1a-4d6e-9a51-1f0a7c3e0002' AS UUID), 'CONTRATANTE'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'CONTRATANTE');
INSERT INTO roles (id, name) SELECT CAST('8f2b8a6e-0c1a-4d6e-9a51-1f0a7c3e0003' AS UUID), 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ADMIN');

-- Catálogo de zonas (barrios de Bogotá). Ids fijos: Worker los guarda. Se insertan solo si no existen.
INSERT INTO zones (id, name, locality) SELECT 1, 'Cedritos', 'Usaquén' WHERE NOT EXISTS (SELECT 1 FROM zones WHERE id = 1);
INSERT INTO zones (id, name, locality) SELECT 2, 'Santa Bárbara', 'Usaquén' WHERE NOT EXISTS (SELECT 1 FROM zones WHERE id = 2);
INSERT INTO zones (id, name, locality) SELECT 3, 'Toberín', 'Usaquén' WHERE NOT EXISTS (SELECT 1 FROM zones WHERE id = 3);
INSERT INTO zones (id, name, locality) SELECT 4, 'Chicó Norte', 'Chapinero' WHERE NOT EXISTS (SELECT 1 FROM zones WHERE id = 4);
INSERT INTO zones (id, name, locality) SELECT 5, 'Chapinero Central', 'Chapinero' WHERE NOT EXISTS (SELECT 1 FROM zones WHERE id = 5);
INSERT INTO zones (id, name, locality) SELECT 6, 'Rosales', 'Chapinero' WHERE NOT EXISTS (SELECT 1 FROM zones WHERE id = 6);
INSERT INTO zones (id, name, locality) SELECT 7, 'Niza', 'Suba' WHERE NOT EXISTS (SELECT 1 FROM zones WHERE id = 7);
INSERT INTO zones (id, name, locality) SELECT 8, 'Colina Campestre', 'Suba' WHERE NOT EXISTS (SELECT 1 FROM zones WHERE id = 8);
INSERT INTO zones (id, name, locality) SELECT 9, 'Minuto de Dios', 'Engativá' WHERE NOT EXISTS (SELECT 1 FROM zones WHERE id = 9);
INSERT INTO zones (id, name, locality) SELECT 10, 'Normandía', 'Engativá' WHERE NOT EXISTS (SELECT 1 FROM zones WHERE id = 10);
INSERT INTO zones (id, name, locality) SELECT 11, 'Modelia', 'Fontibón' WHERE NOT EXISTS (SELECT 1 FROM zones WHERE id = 11);
INSERT INTO zones (id, name, locality) SELECT 12, 'Ciudad Salitre', 'Fontibón' WHERE NOT EXISTS (SELECT 1 FROM zones WHERE id = 12);
INSERT INTO zones (id, name, locality) SELECT 13, 'Teusaquillo', 'Teusaquillo' WHERE NOT EXISTS (SELECT 1 FROM zones WHERE id = 13);
INSERT INTO zones (id, name, locality) SELECT 14, 'Galerías', 'Teusaquillo' WHERE NOT EXISTS (SELECT 1 FROM zones WHERE id = 14);
INSERT INTO zones (id, name, locality) SELECT 15, 'Quinta Paredes', 'Teusaquillo' WHERE NOT EXISTS (SELECT 1 FROM zones WHERE id = 15);
INSERT INTO zones (id, name, locality) SELECT 16, 'La Candelaria', 'La Candelaria' WHERE NOT EXISTS (SELECT 1 FROM zones WHERE id = 16);
INSERT INTO zones (id, name, locality) SELECT 17, 'Las Nieves', 'Santa Fe' WHERE NOT EXISTS (SELECT 1 FROM zones WHERE id = 17);
INSERT INTO zones (id, name, locality) SELECT 18, 'Restrepo', 'Antonio Nariño' WHERE NOT EXISTS (SELECT 1 FROM zones WHERE id = 18);
INSERT INTO zones (id, name, locality) SELECT 19, 'Policarpa', 'Antonio Nariño' WHERE NOT EXISTS (SELECT 1 FROM zones WHERE id = 19);
INSERT INTO zones (id, name, locality) SELECT 20, 'Veinte de Julio', 'San Cristóbal' WHERE NOT EXISTS (SELECT 1 FROM zones WHERE id = 20);
INSERT INTO zones (id, name, locality) SELECT 21, 'Venecia', 'Tunjuelito' WHERE NOT EXISTS (SELECT 1 FROM zones WHERE id = 21);
INSERT INTO zones (id, name, locality) SELECT 22, 'Kennedy Central', 'Kennedy' WHERE NOT EXISTS (SELECT 1 FROM zones WHERE id = 22);
INSERT INTO zones (id, name, locality) SELECT 23, 'Castilla', 'Kennedy' WHERE NOT EXISTS (SELECT 1 FROM zones WHERE id = 23);
INSERT INTO zones (id, name, locality) SELECT 24, 'Patio Bonito', 'Kennedy' WHERE NOT EXISTS (SELECT 1 FROM zones WHERE id = 24);
INSERT INTO zones (id, name, locality) SELECT 25, 'Bosa Centro', 'Bosa' WHERE NOT EXISTS (SELECT 1 FROM zones WHERE id = 25);
