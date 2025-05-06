--liquibase formatted sql

--changeset etaradaev:1
CREATE TABLE user_following (
    chat_id SERIAL,
    payment BOOLEAN,
    date_started TIMESTAMP,
    date_notification TIMESTAMP,
    sent_notification BOOLEAN,
    date_ended TIMESTAMP,
    sent_ended BOOLEAN
)

