-- ============================================================
-- V1: Core schema for the AI Banking Assistant
-- ============================================================
-- NOTE: vector and pgcrypto extensions must be installed by a superuser
-- before running migrations (e.g. via docker-entrypoint-initdb.d or manually).

-- ---------------------------------------------------------------
-- Users / Auth / RBAC
-- ---------------------------------------------------------------
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username        VARCHAR(100) NOT NULL UNIQUE,
    email           VARCHAR(150) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(150) NOT NULL,
    phone_number    VARCHAR(20),
    role            VARCHAR(30)  NOT NULL DEFAULT 'CUSTOMER',
    account_tier    VARCHAR(30)  NOT NULL DEFAULT 'STANDARD',
    enabled         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE kyc_details (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    id_proof_type       VARCHAR(50),
    id_proof_number     VARCHAR(100),
    address             VARCHAR(255),
    date_of_birth       DATE,
    annual_income       NUMERIC(15,2),
    employment_status   VARCHAR(50),
    credit_score        INTEGER,
    kyc_status          VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    verified_at         TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (user_id)
);

-- ---------------------------------------------------------------
-- Accounts & Transactions
-- ---------------------------------------------------------------
CREATE TABLE accounts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    account_number  VARCHAR(34) NOT NULL UNIQUE,
    account_type    VARCHAR(30) NOT NULL,
    balance         NUMERIC(18,2) NOT NULL DEFAULT 0,
    currency        VARCHAR(3)  NOT NULL DEFAULT 'INR',
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    opened_at       TIMESTAMP   NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP   NOT NULL DEFAULT now()
);

CREATE TABLE transactions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id      UUID NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    type            VARCHAR(20) NOT NULL,            -- DEBIT / CREDIT
    category        VARCHAR(50),
    amount          NUMERIC(18,2) NOT NULL,
    description     VARCHAR(255),
    balance_after   NUMERIC(18,2) NOT NULL,
    transacted_at   TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_transactions_account_id ON transactions(account_id);
CREATE INDEX idx_transactions_transacted_at ON transactions(transacted_at);

-- ---------------------------------------------------------------
-- Loans
-- ---------------------------------------------------------------
CREATE TABLE loans (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    loan_type           VARCHAR(30) NOT NULL,          -- PERSONAL / HOME / AUTO
    principal_amount    NUMERIC(18,2) NOT NULL,
    interest_rate       NUMERIC(6,3) NOT NULL,
    tenure_months       INTEGER NOT NULL,
    emi_amount          NUMERIC(18,2),
    status              VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    purpose             VARCHAR(255),
    created_at          TIMESTAMP NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP NOT NULL DEFAULT now(),
    decided_by          UUID REFERENCES users(id),
    decided_at          TIMESTAMP,
    decision_notes      VARCHAR(500)
);

-- ---------------------------------------------------------------
-- Generic Service Requests (loan apps reuse this workflow too)
-- ---------------------------------------------------------------
CREATE TABLE service_requests (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    request_type    VARCHAR(50) NOT NULL,    -- LOAN / ACCOUNT_UPGRADE / CARD / OTHER
    reference_id    UUID,                    -- e.g. loans.id when request_type = LOAN
    status          VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    payload         JSONB,
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now(),
    decided_by      UUID REFERENCES users(id),
    decided_at      TIMESTAMP,
    decision_notes  VARCHAR(500)
);

CREATE INDEX idx_service_requests_status ON service_requests(status);
CREATE INDEX idx_service_requests_user_id ON service_requests(user_id);

-- ---------------------------------------------------------------
-- Conversational Memory
-- ---------------------------------------------------------------
CREATE TABLE chat_sessions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE / EXPIRED
    started_at      TIMESTAMP NOT NULL DEFAULT now(),
    last_active_at  TIMESTAMP NOT NULL DEFAULT now(),
    expired_at      TIMESTAMP
);

CREATE TABLE chat_messages (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id      UUID NOT NULL REFERENCES chat_sessions(id) ON DELETE CASCADE,
    sender          VARCHAR(20) NOT NULL,   -- USER / ASSISTANT / SYSTEM
    intent          VARCHAR(50),
    content         TEXT NOT NULL,
    metadata        JSONB,
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_chat_messages_session_id ON chat_messages(session_id);

-- ---------------------------------------------------------------
-- Policy Knowledge Base (Semantic RAG)
-- ---------------------------------------------------------------
CREATE TABLE policy_documents (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title           VARCHAR(255) NOT NULL,
    category        VARCHAR(100),
    content         TEXT NOT NULL,
    embedding       vector(1536),
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);

-- HNSW index for fast approximate cosine-similarity search.
CREATE INDEX idx_policy_documents_embedding ON policy_documents
    USING hnsw (embedding vector_cosine_ops);
