--liquibase formatted sql

--changeset etaradaev:1
CREATE TABLE user_following (
    user_id SERIAL,
    chat_id SERIAL,
    payment BOOLEAN,
    date_started TIMESTAMP,
    date_ended TIMESTAMP
)

--changeset etaradaev:2
ALTER TABLE user_following ADD sent BOOLEAN