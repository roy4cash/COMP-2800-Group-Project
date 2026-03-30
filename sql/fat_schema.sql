-- ============================================================
-- FAT - Financial Activity Tracker
-- MySQL Schema
-- Designed for a local MySQL instance on the Azure VM
-- ============================================================

CREATE TABLE IF NOT EXISTS users (
    id INT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS categories (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_categories_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS budgets (
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    month INT NOT NULL,
    year INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_budgets_user_month_year (user_id, month, year),
    CONSTRAINT fk_budgets_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS expenses (
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    category_id INT NOT NULL,
    description VARCHAR(255),
    amount DECIMAL(10, 2) NOT NULL,
    expense_date DATE NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_expenses_user_date (user_id, expense_date),
    CONSTRAINT fk_expenses_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_expenses_category FOREIGN KEY (category_id) REFERENCES categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS investments (
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    ticker VARCHAR(20),
    type VARCHAR(50) DEFAULT 'Stock',
    shares DECIMAL(15, 6) NOT NULL DEFAULT 0,
    buy_price DECIMAL(15, 2) NOT NULL,
    current_price DECIMAL(15, 2) DEFAULT 0,
    purchase_date DATE,
    notes VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_investments_user_created (user_id, created_at),
    CONSTRAINT fk_investments_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- Seed Data
-- ============================================================

INSERT IGNORE INTO users (id, username)
VALUES (1, 'default_user');

INSERT IGNORE INTO categories (id, name)
VALUES
    (1, 'Food'),
    (2, 'Transport'),
    (3, 'Entertainment'),
    (4, 'Utilities'),
    (5, 'Health'),
    (6, 'Other');

INSERT IGNORE INTO budgets (user_id, month, year, amount)
VALUES (1, MONTH(CURDATE()), YEAR(CURDATE()), 1000.00);
