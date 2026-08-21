# Vending Machine Management System

Backend project implemented as four Spring Boot services:

- `auth-service` — users, roles, JWT access tokens, refresh tokens and password reset.
- `machine-service` — vending machines, products, inventory and nearby-machine search.
- `order-service` — orders, order state, signed QR payloads and dispensing.
- `payment-service` — Stripe PaymentIntent creation, webhooks and order callbacks.

## Stack

Java 21, Spring Boot, Spring Security, Spring Data JPA/Hibernate, MySQL/Google Cloud SQL, Stripe Java SDK 29.0.0, Gradle, Docker, GitHub Actions and Google Cloud Run.

## API overview

### auth-service
- `POST /api/v1/auth/signup` -> Create a user in the system and returns a JWT Token and refresh token
- `POST /api/v1/auth/login` -> Log a user into the system and returns a JWT Token and refresh token
- `POST /api/v1/auth/refresh` -> Issue a fresh JWT based on the refresh token
- `POST /api/v1/auth/admin/users` -> Create a user in the system in Pending state
- `POST /api/v1/auth/forgot-password` -> Create a password reset token
- `POST /api/v1/auth/reset-password` -> Validate a password reset token and creates new password
- `GET /api/v1/auth/test-token` -> Test endpoint to validate a JWT token

### machine-service
- `POST /api/v1/machines` -> Adds a Machine into the system
- `POST /api/v1/machines/{id}/enable` -> Enable a machine based on id
- `GET /api/v1/machines/nearby` -> Searche for machines nearby based on the location
- `GET /api/v1/machines/{id}` -> Get machine details by ID
- `GET /api/v1/machines/{id}/inventory` -> Get inventory details by machine ID
- `POST /api/v1/machines/{id}/inventory` -> Add inventory to the machine
- `POST /api/v1/products` -> Add a product into the system

### order-service
- `POST /api/v1/orders` -> Create an order based on the order items
- `GET /api/v1/orders/{id}` -> Get order details by ID
- `GET /api/v1/orders/{id}/qr-code` -> Get qr-code for dispensing items for a paid order
- `GET /api/v1/orders` -> Get all orders for the current user
- `POST /api/v1/orders/internal/{id}/payment` -> Webhook to update order status to paid/faild (Called by payment-service)
- `POST /api/v1/machines/{id}/dispense/authorize` -> Authorize a QR code and return dispensing items list (Called by machine controller to validate the qr code)
- `POST /api/v1/machines/{id}/dispense` -> Marks QR code as dispensed (Called by machine controller to notify dispensing completion)

### payment-service
- `POST /api/v1/payments/create-intent` -> Create a payment intent on Stripe based on order ID
- `POST /api/v1/payments/webhook/stripe` -> Webhook to process payment from Stripe (Called by Stripe)

## Repository structure

```text
neovarsity-project/
├── auth-service/
├── machine-service/
├── order-service/
├── payment-service/
└── .github/workflows/
```

Each service follows the common package pattern of `api`, `service`, `repository`, `entity`, `dto` and `security` where applicable.

## Running locally

From an individual service directory:

```bash
./gradlew clean build
./gradlew bootRun
```

The Dockerfiles build an executable `app.jar` and run it on port 8080 as a non-root `spring` user. A compatible MySQL schema is required because JPA uses `ddl-auto: validate`.

## Configuration

Sensitive configuration is externalized. Typical variables include:

- `DB_PASSWORD`
- `JWT_SECRET`
- `STRIPE_SECRET_KEY`
- `STRIPE_WEBHOOK_SECRET`
- `INTERNAL_SERVICE_SECRET`
- `ORDER_SERVICE_BASE_URL`

## Security

The project uses three distinct mechanisms:

1. JWT bearer authentication for user-facing authenticated APIs.
2. Stripe `Stripe-Signature` verification for payment webhooks.
3. HMAC signatures for payment-service → order-service callbacks.

## Database

Logical tables represented by the entity classes include:

- `users`
- `refresh_tokens`
- `password_reset_tokens`
- `vending_machines`
- `products`
- `machine_inventory`
- `orders`
- `order_items`
- `qr_codes`
- `payments`
- `processed_webhook_event`

Detailed schema design and Mermaid ER diagram are available in `/home/runner/work/neovarsity-project/neovarsity-project/DATABASE_SCHEMA.md`.
