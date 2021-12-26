CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS tb_wallet(
    id                      UUID DEFAULT gen_random_uuid(),
    user_id                 UUID NOT NULL,
    surname                 VARCHAR(64) NOT NULL,
    is_active               BOOLEAN DEFAULT TRUE,
    is_default              BOOLEAN DEFAULT FALSE,
    min_balance             NUMERIC(15, 2) DEFAULT 0,
    accept_bank_transfer    BOOLEAN DEFAULT TRUE,
    accept_payments         BOOLEAN DEFAULT TRUE,
    accept_withdrawing      BOOLEAN DEFAULT TRUE,
    accept_deposit          BOOLEAN DEFAULT TRUE,
    created_at              TIMESTAMP WITH TIME ZONE,
    updated_at              TIMESTAMP WITH TIME ZONE,
    deleted_at              TIMESTAMP WITH TIME ZONE,
    PRIMARY KEY (id),
    CONSTRAINT unique__user_id__surname
        UNIQUE (user_id, surname)
);