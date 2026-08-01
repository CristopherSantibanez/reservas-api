INSERT INTO venues (name, address) VALUES
                                       ('Complejo Deportivo Las Condes', 'Av. Apoquindo 1234, Las Condes'),
                                       ('Centro Padel Ñuñoa', 'Irarrázaval 890, Ñuñoa');

INSERT INTO courts (venue_id, name, sport, price_per_hour) VALUES
                                                               (1, 'Cancha Fútbol 1', 'FUTBOL', 25000.00),
                                                               (1, 'Cancha Fútbol 2', 'FUTBOL', 25000.00),
                                                               (1, 'Cancha Tenis 1', 'TENIS', 15000.00),
                                                               (2, 'Cancha Padel 1', 'PADEL', 18000.00),
                                                               (2, 'Cancha Padel 2', 'PADEL', 18000.00);