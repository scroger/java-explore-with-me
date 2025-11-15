DROP TABLE IF EXISTS compilation_events;
DROP TABLE IF EXISTS compilations;
DROP TABLE IF EXISTS participation_requests;
DROP TYPE IF EXISTS request_status;
DROP TABLE IF EXISTS events;
DROP TYPE IF EXISTS event_state;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;

CREATE TABLE users
(
    id    BIGSERIAL PRIMARY KEY,
    email VARCHAR(254) NOT NULL UNIQUE,
    name  VARCHAR(250) NOT NULL
);

CREATE TABLE categories
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TYPE event_state AS ENUM ('PENDING','PUBLISHED','CANCELED');

CREATE TABLE events
(
    id                 BIGSERIAL PRIMARY KEY,
    annotation         VARCHAR(2000) NOT NULL,
    description        VARCHAR(7000) NOT NULL,
    title              VARCHAR(120)  NOT NULL,
    event_date         TIMESTAMP     NOT NULL,
    created_on         TIMESTAMP     NOT NULL DEFAULT NOW(),
    published_on       TIMESTAMP,
    state              event_state   NOT NULL DEFAULT 'PENDING',
    paid               BOOLEAN       NOT NULL DEFAULT FALSE,
    participant_limit  INTEGER       NOT NULL DEFAULT 0,
    request_moderation BOOLEAN       NOT NULL DEFAULT TRUE,
    initiator_id       BIGINT        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    category_id        BIGINT        NOT NULL REFERENCES categories (id) ON DELETE RESTRICT,
    lat                DOUBLE PRECISION,
    lon                DOUBLE PRECISION
);

CREATE TYPE request_status AS ENUM ('PENDING','CONFIRMED','REJECTED','CANCELED');

CREATE TABLE participation_requests
(
    id           BIGSERIAL PRIMARY KEY,
    created      TIMESTAMP      NOT NULL DEFAULT NOW(),
    status       request_status NOT NULL DEFAULT 'PENDING',
    event_id     BIGINT         NOT NULL REFERENCES events (id) ON DELETE CASCADE,
    requester_id BIGINT         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    UNIQUE (event_id, requester_id)
);

CREATE TABLE compilations
(
    id     BIGSERIAL PRIMARY KEY,
    title  VARCHAR(50) NOT NULL,
    pinned BOOLEAN     NOT NULL DEFAULT FALSE
);

CREATE TABLE compilation_events
(
    compilation_id BIGINT NOT NULL REFERENCES compilations (id) ON DELETE CASCADE,
    event_id       BIGINT NOT NULL REFERENCES events (id) ON DELETE CASCADE,
    PRIMARY KEY (compilation_id, event_id)
);
