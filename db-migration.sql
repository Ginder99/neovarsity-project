show databases;

-- ============================================================================
-- CREATE DATABASE VMS_DB
-- ============================================================================

CREATE DATABASE vms_db;
USE vms_db;

-- ============================================================================
-- USERS TABLE
-- ============================================================================

CREATE TABLE users (
  id            BIGINT UNSIGNED	NOT NULL AUTO_INCREMENT PRIMARY KEY,
  email         VARCHAR(255)  NOT NULL UNIQUE,
  password_hash VARCHAR(255)  NOT NULL,
  name          VARCHAR(100)  NOT NULL,
  phone         VARCHAR(20),
  created_at    DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at    DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  is_active     TINYINT(1)    NOT NULL DEFAULT TRUE
);

ALTER TABLE users ADD COLUMN role varchar(20);

-- ============================================================================
-- REFRESH TOKENS TABLE
-- ============================================================================

CREATE TABLE refresh_tokens (
	id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ============================================================================
-- PASSWORD RESET TOKENS TABLE
-- ============================================================================

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    purpose VARCHAR(255) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_password_reset_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_password_reset_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_password_reset_tokens_user_id ON password_reset_tokens(user_id);


-- ============================================================================
-- VENDING MACHINES TABLE
-- ============================================================================

CREATE TABLE vending_machines (
  id				 BIGINT UNSIGNED	NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name               VARCHAR(100)   NOT NULL,
  address            VARCHAR(255)   NOT NULL,
  latitude           DOUBLE			NOT NULL,
  longitude          DOUBLE			NOT NULL,
  location           POINT          NOT NULL SRID 4326,
  status             VARCHAR(15)	NOT NULL DEFAULT 'OFFLINE',
  last_heartbeat_at  DATETIME(6),
  created_at         DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at         DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  SPATIAL INDEX idx_machines_location (location)
);

CREATE INDEX idx_machines_status ON vending_machines(status);
CREATE INDEX idx_machines_name ON vending_machines(name);

-- ============================================================================
-- PRODUCTS TABLE
-- ============================================================================

CREATE TABLE products (
  id		  BIGINT UNSIGNED	NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name        VARCHAR(100)   NOT NULL,
  description TEXT,
  category    VARCHAR(50)    NOT NULL,
  image_url   VARCHAR(512),
  base_price  DECIMAL(10,2)  NOT NULL,
  created_at  DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
);

CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_products_name ON products(name);

-- ============================================================================
-- MACHINE INVENTORY TABLE
-- ============================================================================

CREATE TABLE machine_inventory (
  id		  BIGINT UNSIGNED	NOT NULL AUTO_INCREMENT PRIMARY KEY,
  machine_id  BIGINT UNSIGNED	NOT NULL,
  product_id  BIGINT UNSIGNED	NOT NULL,
  slot_id     VARCHAR(10)    NOT NULL,
  quantity    INTEGER        NOT NULL DEFAULT 0 CHECK (quantity >= 0),
  price       DECIMAL(10,2)  NOT NULL,
  updated_at  DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  UNIQUE KEY uk_machine_slot (machine_id, slot_id),
  CONSTRAINT fk_machine_inventory_machine_id FOREIGN KEY (machine_id) REFERENCES vending_machines(id) ON DELETE CASCADE,
  CONSTRAINT fk_machine_inventory_product_id FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE INDEX idx_machine_inventory_updated_at ON machine_inventory(updated_at);

-- ============================================================================
-- ORDERS TABLE
-- ============================================================================

CREATE TABLE orders (
  id	 		   BIGINT UNSIGNED	NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id          BIGINT UNSIGNED,
  machine_id       BIGINT UNSIGNED	NOT NULL,
  status           ENUM('pending_payment', 'paid', 'ready', 'dispensing', 'completed', 'expired', 'failed') NOT NULL DEFAULT 'pending_payment',
  total_amount     DECIMAL(10,2)  NOT NULL,
  created_at       DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at       DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  expires_at       DATETIME(6)    NOT NULL,
  CONSTRAINT fk_orders_user_id FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_orders_machine_id FOREIGN KEY (machine_id) REFERENCES vending_machines(id),
  INDEX idx_orders_user_id (user_id),
  INDEX idx_orders_machine_id (machine_id),
  INDEX idx_orders_status (status),
  INDEX idx_orders_expires_at (expires_at),
  INDEX idx_orders_created_at (created_at)
);

-- ============================================================================
-- ORDER ITEMS TABLE
-- ============================================================================

CREATE TABLE order_items (
  id                  BIGINT UNSIGNED	NOT NULL AUTO_INCREMENT PRIMARY KEY,
  order_id            BIGINT UNSIGNED       NOT NULL,
  product_id          BIGINT UNSIGNED       NOT NULL,
  machine_inventory_id BIGINT UNSIGNED      NOT NULL,
  quantity            INTEGER        NOT NULL CHECK (quantity > 0),
  unit_price          DECIMAL(10,2)  NOT NULL,
  CONSTRAINT fk_order_items_order_id FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
  CONSTRAINT fk_order_items_product_id FOREIGN KEY (product_id) REFERENCES products(id),
  CONSTRAINT fk_order_items_machine_inventory_id FOREIGN KEY (machine_inventory_id) REFERENCES machine_inventory(id),
  INDEX idx_order_items_order_id (order_id),
  INDEX idx_order_items_product_id (product_id),
  INDEX idx_order_items_machine_inventory_id (machine_inventory_id)
);

-- ============================================================================
-- PAYMENTS TABLE
-- ============================================================================

CREATE TABLE payments (
  id                BIGINT UNSIGNED	NOT NULL AUTO_INCREMENT PRIMARY KEY,
  order_id          BIGINT UNSIGNED       NOT NULL UNIQUE,
  payment_intent_id VARCHAR(255)   NOT NULL UNIQUE,
  amount            DECIMAL(10,2)  NOT NULL,
  currency          CHAR(3)        NOT NULL DEFAULT 'INR',
  status            ENUM('PENDING', 'SUCCEEDED', 'FAILED', 'REFUNDED', 'ORDER_NOTIFIED') NOT NULL DEFAULT 'PENDING',
  payment_method    VARCHAR(50),
  created_at        DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at        DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_payments_order_id FOREIGN KEY (order_id) REFERENCES orders(id),
  INDEX idx_payments_status (status),
  INDEX idx_payments_payment_intent_id (payment_intent_id),
  INDEX idx_payments_created_at (created_at)
);


-- ============================================================================
-- PAYMENTS WEBHOOK EVENT TABLE
-- ============================================================================

CREATE TABLE processed_webhook_event (
    stripe_event_id VARCHAR(255) PRIMARY KEY,
    processed_at    DATETIME(6) NOT NULL
);


-- ============================================================================
-- QR CODES TABLE
-- ============================================================================

CREATE TABLE qr_codes (
  id         BIGINT UNSIGNED	NOT NULL AUTO_INCREMENT PRIMARY KEY,
  order_id   BIGINT UNSIGNED    NOT NULL UNIQUE,
  payload    TEXT          NOT NULL,
  expires_at DATETIME(6)   NOT NULL,
  scanned_at DATETIME(6),
  is_used    TINYINT(1)    NOT NULL DEFAULT FALSE,
  created_at DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_qr_codes_order_id FOREIGN KEY (order_id) REFERENCES orders(id),
  INDEX idx_qr_codes_order_id (order_id),
  INDEX idx_qr_codes_expires_at (expires_at),
  INDEX idx_qr_codes_is_used (is_used)
);
