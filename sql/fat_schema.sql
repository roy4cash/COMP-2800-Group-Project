-- ============================================================
-- FAT - Financial Activity Tracker
-- PostgreSQL Schema (for Supabase)
-- ============================================================

-- Users of the application
CREATE TABLE IF NOT EXISTS users (
    id       SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE
);

-- Expense categories (Food, Transport, etc.)
CREATE TABLE IF NOT EXISTS categories (
    id   SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Monthly budget per user
CREATE TABLE IF NOT EXISTS budgets (
    id      SERIAL PRIMARY KEY,
    user_id INT            NOT NULL REFERENCES users(id),
    month   INT            NOT NULL,
    year    INT            NOT NULL,
    amount  DECIMAL(10, 2) NOT NULL,
    UNIQUE (user_id, month, year)
);

-- Individual expense records
CREATE TABLE IF NOT EXISTS expenses (
    id           SERIAL PRIMARY KEY,
    user_id      INT            NOT NULL REFERENCES users(id),
    category_id  INT            NOT NULL REFERENCES categories(id),
    description  VARCHAR(255),
    amount       DECIMAL(10, 2) NOT NULL,
    expense_date DATE           NOT NULL,
    created_at   TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- Seed Data
-- ============================================================

INSERT INTO users (username) VALUES ('default_user')
    ON CONFLICT (username) DO NOTHING;

INSERT INTO categories (name) VALUES
    ('Food'), ('Transport'), ('Entertainment'),
    ('Utilities'), ('Health'), ('Other')
    ON CONFLICT (name) DO NOTHING;

-- Default $1000 budget for the current month
INSERT INTO budgets (user_id, month, year, amount)
VALUES (1, EXTRACT(MONTH FROM CURRENT_DATE), EXTRACT(YEAR FROM CURRENT_DATE), 1000.00)
    ON CONFLICT (user_id, month, year) DO NOTHING;
