# Walmart Checkout Challenge

This project implements a **production-grade backend** for an e-commerce checkout system using **Spring Boot** and **PostgreSQL**. It showcases a clean separation of concerns (Cart, Checkout, Products, Facilities, Coupons), extensible pricing/discount logic, and a fully documented REST API via **SpringDoc OpenAPI (Swagger UI)**.

---

## API Documentation (OpenAPI)

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

> Swagger UI is the **source of truth** for request/response schemas, validation, and the full endpoint list.

---

## Key Features

- **Cart management**
    - Create carts, add items, and fetch cart details.

- **Checkout orchestration**
    - Quote and confirmation flows for checkout pricing.
    - Designed to keep pricing logic testable and extendable.

- **Product CRUD**
    - Manage products and their physical dimensions (useful for shipping/volume rules).

- **Facility CRUD**
    - Manage facilities (used for pickup and/or shipping origin calculations).
    - Includes logistic/shipping address information.

- **Coupon CRUD**
    - Manage coupons (create/update/list/delete).
    - Supports typical coupon attributes needed for checkout rules (e.g., scope/type, stacking, expiration, usage limits) depending on your implementation.

- **API-first**
    - All endpoints are documented through SpringDoc OpenAPI.

---

## Tech Stack

- **Java:** JDK 25
- **Framework:** Spring Boot
- **Database:** PostgreSQL
- **Build:** Gradle (wrapper)
- **Docs:** SpringDoc OpenAPI (Swagger UI)

---

## Prerequisites

| Component | Notes |
|---|---|
| **Java (JDK 25)** | Required to build and run |
| **PostgreSQL** | Local or containerized |
| **Docker (optional)** | If you run PostgreSQL with Docker |

---

## Configuration

The application uses environment variables (commonly via a `.env` file) for local development.

### Environment Variables

Use these variables:

```bash
DB_HOST=localhost
DB_PORT=5432
DB_NAME=walmart-challenge
DB_USERNAME=root
DB_PASSWORD=123456789
HIBERNATE_DDL=update
```

> `HIBERNATE_DDL=update` is intended for local/dev usage.

---

## Running the Application (Dev Profile)

Use the following command exactly:

```bash
export $(cat .env | xargs) && ./gradlew bootRun --args='--spring.profiles.active=dev'
```

Once running:

- Base URL: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## Demo Data (Dev Profile)

When running with `--spring.profiles.active=dev`, the project is expected to be usable end-to-end without manual setup.

Typical demo data includes (depending on your initialization scripts):
- A small catalog of **Products**
- A set of **Facilities**
- A set of **Coupons**
- (Optional) baseline rules for pricing/discounting

To verify seeded data quickly, call:

- `GET /v1/products`
- `GET /v1/facilities`
- `GET /v1/coupons`

---

## API Overview (from OpenAPI)

All endpoints are versioned under `/v1` and exchange JSON. For full details (schemas, validation, examples), use Swagger UI.

### Products

| Method | Path | Description |
|---|---|---|
| GET | `/v1/products` | List products |
| POST | `/v1/products` | Create product |
| GET | `/v1/products/{sku}` | Get product by SKU |
| PUT | `/v1/products/{sku}` | Update product by SKU |
| DELETE | `/v1/products/{sku}` | Delete product by SKU |

### Facilities

| Method | Path | Description |
|---|---|---|
| GET | `/v1/facilities` | List facilities |
| POST | `/v1/facilities` | Create facility |
| GET | `/v1/facilities/{id}` | Get facility by ID |
| PUT | `/v1/facilities/{id}` | Update facility by ID |
| DELETE | `/v1/facilities/{id}` | Delete facility by ID |

### Coupons

| Method | Path | Description |
|---|---|---|
| GET | `/v1/coupons` | List coupons |
| POST | `/v1/coupons` | Create coupon |
| GET | `/v1/coupons/{code}` | Get coupon by code |
| PUT | `/v1/coupons/{code}` | Update coupon by code |
| DELETE | `/v1/coupons/{code}` | Delete coupon by code |

### Cart

| Method | Path | Description |
|---|---|---|
| POST | `/v1/cart` | Create a new cart |
| POST | `/v1/cart/{cartId}/items` | Add item(s) to cart |
| GET | `/v1/cart/{cartId}` | Get cart details |

### Checkout

The `checkout-controller` is exposed in OpenAPI and provides checkout flows (e.g., quote and confirm).  
Please refer to Swagger UI for the exact paths and payloads defined by your implementation:

- `http://localhost:8080/swagger-ui.html`

---

## Troubleshooting

- **Database connection errors**
    - Ensure PostgreSQL is running and reachable at `DB_HOST:DB_PORT`.
    - Confirm `DB_NAME`, `DB_USERNAME`, and `DB_PASSWORD` match your local database.

- **Port already in use**
    - Free port `8080` or configure the server port in your Spring configuration.

- **Schema issues in dev**
    - With `HIBERNATE_DDL=update`, schema changes are applied automatically.
    - For a clean reset, drop/recreate the database and restart the application.

---

## Reviewer Notes

- Swagger UI is the quickest way to validate the API contract, schemas, and all available endpoints.
- The codebase is organized by responsibility (Cart, Checkout, Products, Facilities, Coupons) to keep it maintainable and easy to extend.
