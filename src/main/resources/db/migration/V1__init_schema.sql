CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       email VARCHAR(150) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(20) NOT NULL DEFAULT 'USER',
                       created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE venues (
                        id BIGSERIAL PRIMARY KEY,
                        name VARCHAR(150) NOT NULL,
                        address VARCHAR(255) NOT NULL
);

CREATE TABLE courts (
                        id BIGSERIAL PRIMARY KEY,
                        venue_id BIGINT NOT NULL REFERENCES venues(id),
                        name VARCHAR(100) NOT NULL,
                        sport VARCHAR(50) NOT NULL,
                        price_per_hour NUMERIC(10,2) NOT NULL
);

CREATE TABLE reservations (
                              id BIGSERIAL PRIMARY KEY,
                              user_id BIGINT NOT NULL REFERENCES users(id),
                              court_id BIGINT NOT NULL REFERENCES courts(id),
                              reservation_date DATE NOT NULL,
                              start_time TIME NOT NULL,
                              end_time TIME NOT NULL,
                              status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                              created_at TIMESTAMP NOT NULL DEFAULT now()
);