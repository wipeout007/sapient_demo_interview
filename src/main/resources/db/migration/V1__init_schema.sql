-- V1__init_schema.sql

CREATE TABLE city (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL,
    state       VARCHAR(100),
    country     VARCHAR(100) NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_city_name ON city(name);

CREATE TABLE movie_details (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title             VARCHAR(255) NOT NULL,
    genre             VARCHAR(100) NOT NULL,
    duration_minutes  INT NOT NULL CHECK (duration_minutes > 0),
    description       TEXT,
    is_active         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_movie_title ON movieDetails(title);

CREATE TABLE movie_theatre (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    city_id     UUID NOT NULL REFERENCES city(id),
    name        VARCHAR(255) NOT NULL,
    address     TEXT NOT NULL,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_theatre_city_id ON movieTheatre(city_id);


CREATE TABLE movie_shows (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    theatre_id       UUID NOT NULL REFERENCES movieTheatre(id),
    movie_id         UUID NOT NULL REFERENCES movieDetails(id),
    language         VARCHAR(100) NOT NULL,
    show_date        DATE NOT NULL,
    start_time       TIME NOT NULL,
    end_time         TIME NOT NULL,
    total_seats      INT NOT NULL CHECK (total_seats > 0),
    available_seats  INT NOT NULL CHECK (available_seats >= 0),
    status           VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED'
                        CHECK (status IN ('SCHEDULED', 'CANCELLED', 'COMPLETED')),
    version          BIGINT NOT NULL DEFAULT 0,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_show_seats CHECK (available_seats <= total_seats),
    CONSTRAINT chk_show_time  CHECK (end_time > start_time),
    CONSTRAINT uq_show_theatre_date_time UNIQUE (theatre_id, show_date, start_time)
);

CREATE INDEX idx_show_theatre_id        ON movieShows(theatre_id);
CREATE INDEX idx_show_movie_id          ON movieShows(movie_id);
CREATE INDEX idx_show_date              ON movieShows(show_date);
CREATE INDEX idx_show_movie_city_date   ON movieShows(movie_id, show_date);

CREATE TABLE movie_show_seat (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    show_id      UUID NOT NULL REFERENCES movieShows(id),
    row_label    VARCHAR(5) NOT NULL,
    seat_number  INT NOT NULL CHECK (seat_number > 0),
    seat_type    VARCHAR(20) NOT NULL DEFAULT 'REGULAR'
                    CHECK (seat_type IN ('REGULAR', 'PREMIUM', 'RECLINER')),
    status       VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE'
                    CHECK (status IN ('AVAILABLE', 'BOOKED')),
    version      BIGINT NOT NULL DEFAULT 0,
    updated_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_movie_show_seat UNIQUE (show_id, row_label, seat_number)
);

CREATE INDEX idx_movie_show_seat_status         ON movie_show_seat(show_id, status);

CREATE TABLE movie_booking_details (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    show_id         UUID NOT NULL REFERENCES movieShows(id),
    customer_name   VARCHAR(255) NOT NULL,
    customer_email  VARCHAR(255) NOT NULL,
    customer_phone  VARCHAR(20) NOT NULL,
    total_seats     INT NOT NULL CHECK (total_seats > 0),
    total_amount    NUMERIC(10,2) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED'
                        CHECK (status IN ('CONFIRMED', 'CANCELLED')),
    payment_Status          VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('DONE', 'PENDING', 'FAILED')),
    booked_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_movie_booking_show_id       ON movieBookingDetails(show_id);

CREATE TABLE movie_booking_item (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id    UUID NOT NULL REFERENCES movieBookingDetails(id),
    show_seat_id  UUID NOT NULL REFERENCES show_seat(id),
    price         NUMERIC(10,2) NOT NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_booking_item_seat UNIQUE (show_seat_id)
);
