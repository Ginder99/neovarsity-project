# Database Schema Design

Source of truth: `/home/runner/work/neovarsity-project/neovarsity-project/db-migration.sql`

## Tables

### users
- `id` BIGINT UNSIGNED PK AUTO_INCREMENT
- `email` VARCHAR(255) UNIQUE NOT NULL
- `password_hash` VARCHAR(255) NOT NULL
- `name` VARCHAR(100) NOT NULL
- `phone` VARCHAR(20)
- `created_at` DATETIME(6) NOT NULL
- `updated_at` DATETIME(6) NOT NULL
- `is_active` TINYINT(1) NOT NULL
- `role` VARCHAR(20)

### refresh_tokens
- `id` BIGINT UNSIGNED PK AUTO_INCREMENT
- `user_id` BIGINT UNSIGNED FK -> `users.id` NOT NULL
- `token_hash` VARCHAR(255) UNIQUE NOT NULL
- `expires_at` DATETIME NOT NULL
- `created_at` DATETIME NOT NULL

### password_reset_tokens
- `id` BIGINT UNSIGNED PK AUTO_INCREMENT
- `user_id` BIGINT UNSIGNED FK -> `users.id` NOT NULL
- `token_hash` VARCHAR(255) UNIQUE NOT NULL
- `purpose` VARCHAR(255) NOT NULL
- `expires_at` DATETIME(6) NOT NULL
- `created_at` DATETIME(6) NOT NULL

### vending_machines
- `id` BIGINT UNSIGNED PK AUTO_INCREMENT
- `name` VARCHAR(100) NOT NULL
- `address` VARCHAR(255) NOT NULL
- `latitude` DOUBLE NOT NULL
- `longitude` DOUBLE NOT NULL
- `location` POINT SRID 4326 NOT NULL
- `status` VARCHAR(15) NOT NULL
- `last_heartbeat_at` DATETIME(6)
- `created_at` DATETIME(6) NOT NULL
- `updated_at` DATETIME(6) NOT NULL

### products
- `id` BIGINT UNSIGNED PK AUTO_INCREMENT
- `name` VARCHAR(100) NOT NULL
- `description` TEXT
- `category` VARCHAR(50) NOT NULL
- `image_url` VARCHAR(512)
- `base_price` DECIMAL(10,2) NOT NULL
- `created_at` DATETIME(6) NOT NULL

### machine_inventory
- `id` BIGINT UNSIGNED PK AUTO_INCREMENT
- `machine_id` BIGINT UNSIGNED FK -> `vending_machines.id` NOT NULL
- `product_id` BIGINT UNSIGNED FK -> `products.id` NOT NULL
- `slot_id` VARCHAR(10) NOT NULL
- `quantity` INTEGER NOT NULL
- `price` DECIMAL(10,2) NOT NULL
- `updated_at` DATETIME(6) NOT NULL

### orders
- `id` BIGINT UNSIGNED PK AUTO_INCREMENT
- `user_id` BIGINT UNSIGNED FK -> `users.id`
- `machine_id` BIGINT UNSIGNED FK -> `vending_machines.id` NOT NULL
- `status` ENUM(...) NOT NULL
- `total_amount` DECIMAL(10,2) NOT NULL
- `created_at` DATETIME(6) NOT NULL
- `updated_at` DATETIME(6) NOT NULL
- `expires_at` DATETIME(6) NOT NULL

### order_items
- `id` BIGINT UNSIGNED PK AUTO_INCREMENT
- `order_id` BIGINT UNSIGNED FK -> `orders.id` NOT NULL
- `product_id` BIGINT UNSIGNED FK -> `products.id` NOT NULL
- `machine_inventory_id` BIGINT UNSIGNED FK -> `machine_inventory.id` NOT NULL
- `quantity` INTEGER NOT NULL
- `unit_price` DECIMAL(10,2) NOT NULL

### payments
- `id` BIGINT UNSIGNED PK AUTO_INCREMENT
- `order_id` BIGINT UNSIGNED FK -> `orders.id` UNIQUE NOT NULL
- `payment_intent_id` VARCHAR(255) UNIQUE NOT NULL
- `amount` DECIMAL(10,2) NOT NULL
- `currency` CHAR(3) NOT NULL
- `status` ENUM(...) NOT NULL
- `payment_method` VARCHAR(50)
- `created_at` DATETIME(6) NOT NULL
- `updated_at` DATETIME(6) NOT NULL

### processed_webhook_event
- `stripe_event_id` VARCHAR(255) PK
- `processed_at` DATETIME(6) NOT NULL

### qr_codes
- `id` BIGINT UNSIGNED PK AUTO_INCREMENT
- `order_id` BIGINT UNSIGNED FK -> `orders.id` UNIQUE NOT NULL
- `payload` TEXT NOT NULL
- `expires_at` DATETIME(6) NOT NULL
- `scanned_at` DATETIME(6)
- `is_used` TINYINT(1) NOT NULL
- `created_at` DATETIME(6) NOT NULL

## Mermaid ER Diagram

```mermaid
erDiagram
    USERS {
        BIGINT id PK
        VARCHAR email UK
        VARCHAR password_hash
        VARCHAR name
        VARCHAR phone
        DATETIME created_at
        DATETIME updated_at
        BOOLEAN is_active
        VARCHAR role
    }

    REFRESH_TOKENS {
        BIGINT id PK
        BIGINT user_id FK
        VARCHAR token_hash UK
        DATETIME expires_at
        DATETIME created_at
    }

    PASSWORD_RESET_TOKENS {
        BIGINT id PK
        BIGINT user_id FK
        VARCHAR token_hash UK
        VARCHAR purpose
        DATETIME expires_at
        DATETIME created_at
    }

    VENDING_MACHINES {
        BIGINT id PK
        VARCHAR name
        VARCHAR address
        DOUBLE latitude
        DOUBLE longitude
        POINT location
        VARCHAR status
        DATETIME last_heartbeat_at
        DATETIME created_at
        DATETIME updated_at
    }

    PRODUCTS {
        BIGINT id PK
        VARCHAR name
        TEXT description
        VARCHAR category
        VARCHAR image_url
        DECIMAL base_price
        DATETIME created_at
    }

    MACHINE_INVENTORY {
        BIGINT id PK
        BIGINT machine_id FK
        BIGINT product_id FK
        VARCHAR slot_id
        INTEGER quantity
        DECIMAL price
        DATETIME updated_at
    }

    ORDERS {
        BIGINT id PK
        BIGINT user_id FK
        BIGINT machine_id FK
        ENUM status
        DECIMAL total_amount
        DATETIME created_at
        DATETIME updated_at
        DATETIME expires_at
    }

    ORDER_ITEMS {
        BIGINT id PK
        BIGINT order_id FK
        BIGINT product_id FK
        BIGINT machine_inventory_id FK
        INTEGER quantity
        DECIMAL unit_price
    }

    PAYMENTS {
        BIGINT id PK
        BIGINT order_id FK
        VARCHAR payment_intent_id UK
        DECIMAL amount
        CHAR currency
        ENUM status
        VARCHAR payment_method
        DATETIME created_at
        DATETIME updated_at
    }

    PROCESSED_WEBHOOK_EVENT {
        VARCHAR stripe_event_id PK
        DATETIME processed_at
    }

    QR_CODES {
        BIGINT id PK
        BIGINT order_id FK
        TEXT payload
        DATETIME expires_at
        DATETIME scanned_at
        BOOLEAN is_used
        DATETIME created_at
    }

    USERS ||--o{ REFRESH_TOKENS : has
    USERS ||--o{ PASSWORD_RESET_TOKENS : has
    USERS ||--o{ ORDERS : places

    VENDING_MACHINES ||--o{ MACHINE_INVENTORY : stocks
    VENDING_MACHINES ||--o{ ORDERS : serves

    PRODUCTS ||--o{ MACHINE_INVENTORY : listed_as
    PRODUCTS ||--o{ ORDER_ITEMS : purchased_as

    MACHINE_INVENTORY ||--o{ ORDER_ITEMS : selected_from

    ORDERS ||--o{ ORDER_ITEMS : contains
    ORDERS ||--o| PAYMENTS : paid_by
    ORDERS ||--o| QR_CODES : issues
```
