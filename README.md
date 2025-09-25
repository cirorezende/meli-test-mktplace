# meli-test-mktplace

## Order Processing Model (Async)

The application uses a pure asynchronous processing model:

- POST `/api/v1/orders` creates an order, publishes an `ORDER_CREATED` event, and returns `202 Accepted` with the initial state (`RECEIVED`).
- Clients should poll `GET /api/v1/orders/{id}` or subscribe to events to observe state transitions (`RECEIVED` -> `PROCESSING` -> `PROCESSED` or `FAILED`).

### Idempotency Guard

To avoid duplicate side-effects from retries or duplicate events, the processing use case only performs work when the order status is `RECEIVED`. For any other non-final status, the call is a no-op and returns the current state. This ensures:

- Only one external call to the Distribution Centers API per order lifecycle
- Stable behavior under event replays and at-least-once delivery

### Caching Strategy

- Distribution centers are cached in Redis using a versioned key: `distribution-centers:v2:{STATE}`.
- Values are stored as a typed array (`DistributionCenter[]`) to avoid `List` polymorphic deserialization issues.
- The model is resilient to unknown fields with Jackson annotations.

## Running Locally

Use Docker Compose to bring up dependencies (Postgres, Redis, Kafka). Distribution Centers are now mocked in‑process (no external WireMock container required). Then run the app with Maven or your IDE.

### Project Structure

The project was flattened into a single Maven module (`orders`) for simplicity. Legacy `service/` module artifacts were removed; all source now lives under the standard `src/main` and `src/test` roots.

## Testing

Integration tests use Testcontainers (Kafka/Postgres/Redis). The Distribution Center data is generated deterministically/randomly by an in‑process service, removing the need for WireMock. The suite expects `202` on order creation and validates end-to-end async processing.
