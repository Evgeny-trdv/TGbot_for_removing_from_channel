--liquibase formatted sql

--changeset etaradaev:1
CREATE TABLE user_following (
    chat_id SERIAL,
    payment BOOLEAN,
    date_started TIMESTAMP,
    date_notification TIMESTAMP,
    date_ended TIMESTAMP,
    sent BOOLEAN
)