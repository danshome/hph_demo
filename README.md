# Human Presence Handshake Demo

This repository provides a minimal demonstration of combining Google reCAPTCHA v3
with a server‑enforced Human Presence Handshake (HPH).  The sample application
includes:

* **Angular front‑end** with a service that watches user interactions, obtains a
  handshake token, and sends it in a header for sensitive API calls.
* **Spring Boot 3 back‑end** providing endpoints to issue and validate HPH tokens
  and a checkout endpoint gated by both reCAPTCHA and HPH verification.

## Structure

```
frontend/  - Angular example code and TypeScript build
backend/   - Spring Boot application implementing the HPH endpoints
```

## Running the demo

### Front‑end

```
cd frontend
npm install
npm test        # compiles the TypeScript sources
```

### Back‑end

```
cd backend
mvn test        # compiles the Spring Boot application
```

The back‑end build requires internet access to download dependencies from Maven
Central.

### Docker

To run the full demo using Docker (requires Docker and Docker Compose):

```
docker-compose build
docker-compose up
```

The Angular front‑end will be available at [http://localhost:4200](http://localhost:4200) and the Spring Boot back‑end at [http://localhost:8080](http://localhost:8080).
