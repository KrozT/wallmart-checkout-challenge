# Changelog

All notable changes to this project will be documented in this file.

The format is based on **Keep a Changelog**, and this project adheres to **Semantic Versioning**.

## [0.1.0] - 2026-02-15

### Added
- Spring Boot REST API foundation for the Walmart Checkout Challenge.
- Cart endpoints to create carts, add items, and retrieve cart details.
- Product CRUD endpoints (`/v1/products`) including product dimensions support.
- Facility CRUD endpoints (`/v1/facilities`) including shipping address data.
- Coupon CRUD endpoints (`/v1/coupons`) for managing coupon definitions.
- Checkout controller exposed via OpenAPI (see Swagger UI for available operations).
- SpringDoc OpenAPI integration:
    - Swagger UI available at `/swagger-ui.html`
    - OpenAPI definition available at `/v3/api-docs`
- PostgreSQL configuration via environment variables (DB host/port/name/user/password).
- Dev profile run command documented for local execution using `.env`.
- Initial README with setup, configuration, and API overview.
