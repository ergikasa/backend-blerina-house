# Blerina House — Booking Platform (Backend)

> Hotel management & reservation REST API for **Blerina House**, a small family hotel in Sarti, Halkidiki, Greece.
> Replaces manual booking workflows (WhatsApp / phone / Messenger) with an automated, race-condition-safe reservation system.

[![Java](https://img.shields.io/badge/Java-21-orange)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-brightgreen)]()
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16%2B-blue)]()
[![License](https://img.shields.io/badge/license-Proprietary-lightgrey)]()

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Environment Variables](#environment-variables)
- [Database Setup](#database-setup)
- [Running the Application](#running-the-application)
- [API Reference](#api-reference)
- [Booking Flow](#booking-flow)
- [Project Structure](#project-structure)
- [Security Notes](#security-notes)
- [Roadmap](#roadmap)
- [License](#license)

---

## Overview

Blerina House Backend is a production-grade Spring Boot REST API that powers the hotel's public booking website and its private admin panel.

The core problem it solves is **double-booking prevention**. Availability is enforced at the database level using PostgreSQL exclusion constraints (`btree_gist`) over half-open date ranges, so two concurrent requests for the same room and overlapping dates can never both succeed — the second one fails atomically with `409 Conflict`.

Payments are confirmed **server-side via Stripe webhooks**, never trusted from the client. A reservation only becomes `CONFIRMED` after Stripe notifies the backend.

---

## Features

- **Public booking engine** — browse rooms, check live availability, create reservations, pay via Stripe.
- **Race-condition-safe** — DB-level exclusion constraint guarantees no overlapping bookings.
- **Stripe payments** — Payment Intents + webhook-driven confirmation; refund support.
- **Automatic expiration** — `PENDING` reservations expire after ~15 minutes and release the room.
- **Admin panel API** — JWT-secured CRUD for rooms, image uploads (Cloudinary), reservation management, blocked dates.
- **Cloudinary integration** — multi-image room galleries served via CDN.
- **Clean architecture** — Controller → Service → Repository, DTOs isolated from entities via MapStruct.
- **Global error contract** — consistent `ApiError` response shape with field-level validation errors.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 (LTS) |
| Framework | Spring Boot 4.0.6 (Spring Web MVC, Tomcat embedded) |
| Persistence | Spring Data JPA + Hibernate 7 |
| Database | PostgreSQL 16+ (`btree_gist` extension required) |
| Security | Spring Security 7 + JWT (jjwt 0.13) |
| Payments | Stripe (stripe-java 20.37, API version `2026-04-22`) |
| Image storage | Cloudinary (cloudinary-http5 2.3.2) |
| Mapping | MapStruct 1.6.3 |
| JSON | Jackson 3 |
| Migrations | Flyway |
| Build | Maven |

---

## Architecture

```
┌──────────────┐     REST /api/v1     ┌─────────────────────────┐
│  React SPA   │ ───────────────────▶ │   Spring Boot Backend   │
│  (frontend)  │ ◀─────────────────── │  Controller → Service   │
└──────────────┘      JSON (camelCase)│        → Repository     │
                                      └───────────┬─────────────┘
                                                  │
                 ┌────────────────────────────────┼────────────────────────────┐
                 ▼                                 ▼                            ▼
        ┌─────────────────┐             ┌────────────────────┐        ┌─────────────────┐
        │   PostgreSQL    │             │      Stripe        │        │   Cloudinary    │
        │ exclusion       │             │ PaymentIntents +   │        │ image upload    │
        │ constraints     │             │ webhooks           │        │ + CDN           │
        └─────────────────┘             └────────────────────┘        └─────────────────┘
```

**Key design decisions**

- `total_price` is stored (not generated) to allow future seasonal pricing / discounts / taxes without a painful migration.
- `nights` is a generated column (pure date arithmetic).
- Reservations use **half-open intervals** `[check_in, check_out)` — one guest's check-out date may equal another's check-in date.
- Rooms are never hard-deleted; they are soft-deleted via `is_active = false`.

---

## Prerequisites

Make sure you have the following installed:

- **JDK 21**
- **Maven 3.9+** (or use the bundled `./mvnw` wrapper)
- **PostgreSQL 16+**
- A **Stripe** account (test mode is fine for development)
- A **Cloudinary** account
- **IntelliJ IDEA** (recommended) or any IDE

---

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/<your-username>/blerina-house-backend.git
cd blerina-house-backend
```

### 2. Configure environment variables

This project reads secrets from **environment variables**, not from committed config files. See [Environment Variables](#environment-variables) below.

> **Important (IntelliJ users):** Spring Boot does **not** auto-load `.env` files the way Vite does. Set the variables in **Run → Edit Configurations → Environment variables**, or export them in your shell before running.

### 3. Create the database

See [Database Setup](#database-setup).

### 4. Run

```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.

---

## Environment Variables

| Variable | Required | Example | Description |
|---|---|---|---|
| `DB_URL` | ✅ | `jdbc:postgresql://localhost:5432/blerina_house` | JDBC connection string |
| `DB_USERNAME` | ✅ | `postgres` | Database user |
| `DB_PASSWORD` | ✅ | `your_password` | Database password |
| `JWT_SECRET` | ✅ | `a-long-random-256-bit-secret` | HMAC signing key for JWT (keep secret!) |
| `CLOUDINARY_URL` | ✅ | `cloudinary://<key>:<secret>@<cloud_name>` | Full Cloudinary connection URL |
| `STRIPE_SECRET_KEY` | ✅ | `sk_test_...` | Stripe secret key (server-side only) |
| `STRIPE_WEBHOOK_SECRET` | ✅ | `whsec_...` | Stripe webhook signing secret |
| `APP_CORS_ALLOWED_ORIGINS` | ⬜ | `http://localhost:5173` | Allowed frontend origin(s); defaults to Vite dev |

> **Never commit any of these values.** All keys above are secrets except the CORS origin.

### Example `.env.example` (commit this, not your real `.env`)

```env
DB_URL=jdbc:postgresql://localhost:5432/blerina_house
DB_USERNAME=postgres
DB_PASSWORD=
JWT_SECRET=
CLOUDINARY_URL=
STRIPE_SECRET_KEY=
STRIPE_WEBHOOK_SECRET=
APP_CORS_ALLOWED_ORIGINS=http://localhost:5173
```

---

## Database Setup

The schema requires the `btree_gist` extension (used by the anti-double-booking exclusion constraint).

**Step 1 — create the database (run once):**

```bash
createdb -U postgres -E UTF8 --template=template0 blerina_house
```

**Step 2 — apply the schema:**

If Flyway is wired into the app, migrations run automatically on startup. To apply the schema manually instead:

```bash
psql -U postgres -d blerina_house -f blerina_house_schema.sql
```

The schema runs inside a single transaction — if anything fails, nothing is applied.

> ⚠️ The development seed inserts a default admin (`admin` / `admin123`). **Change this before production** and do not let the dev seed run against a production database.

---

## Running the Application

### From the command line

```bash
./mvnw spring-boot:run
```

### From IntelliJ IDEA

1. Open the project.
2. Go to **Run → Edit Configurations**.
3. Add the [environment variables](#environment-variables) under **Environment variables**.
4. Run the main application class.

### Verifying it works

```bash
curl http://localhost:8080/api/v1/rooms
```

Expected: `200 OK` with a JSON array of active rooms.

---

## API Reference

- **Base URL (dev):** `http://localhost:8080`
- **Prefix:** all routes under `/api/v1`
- **JSON naming:** `camelCase`
- **Dates:** ISO-8601 date strings (`"2026-07-15"`)
- **Timestamps:** ISO-8601 with zone (`"2026-07-15T12:30:00Z"`, UTC)
- **Money:** decimal, 2 dp, EUR (`60.00`)
- **Admin auth:** `Authorization: Bearer <accessToken>`

### Public endpoints (no auth)

| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/auth/login` | Admin login → returns access token |
| GET | `/api/v1/rooms` | List active rooms |
| GET | `/api/v1/rooms/{slug}` | Room detail by slug |
| GET | `/api/v1/availability?roomId=&checkIn=&checkOut=` | Check availability + estimated total |
| POST | `/api/v1/reservations` | Create a `PENDING` reservation |
| GET | `/api/v1/reservations/{code}` | Get reservation by code |
| POST | `/api/v1/payments/intent` | Create Stripe Payment Intent |
| POST | `/api/v1/payments/webhook` | Stripe webhook (Stripe only) |

### Admin endpoints (Bearer, `ROLE_ADMIN`)

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/admin/rooms` | List all rooms (incl. inactive) |
| POST | `/api/v1/admin/rooms` | Create room |
| PUT | `/api/v1/admin/rooms/{id}` | Update room |
| DELETE | `/api/v1/admin/rooms/{id}` | Soft-delete room |
| POST | `/api/v1/admin/rooms/{roomId}/images` | Upload room image(s) (multipart) |
| DELETE | `/api/v1/admin/images/{imageId}` | Delete image |
| PUT | `/api/v1/admin/images/{imageId}/cover` | Set cover image |
| GET | `/api/v1/admin/reservations?status=&page=&size=` | List reservations (paged) |
| GET | `/api/v1/admin/reservations/{id}` | Reservation detail |
| POST | `/api/v1/admin/reservations/{id}/cancel` | Cancel reservation |
| POST | `/api/v1/admin/reservations/{id}/refund` | Refund reservation |
| POST | `/api/v1/admin/blocked-dates` | Block dates for a room |
| DELETE | `/api/v1/admin/blocked-dates/{id}` | Unblock dates |

### Error response shape

```json
{
  "timestamp": "2026-06-17T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/reservations",
  "fieldErrors": [
    { "field": "checkOutDate", "message": "must be after check-in date" }
  ]
}
```

| Status | Meaning |
|---|---|
| 400 | Validation / malformed request |
| 401 | Missing / invalid token / bad login |
| 403 | Authenticated but insufficient role |
| 404 | Resource not found |
| 409 | Room not available (double-booking) |
| 422 | Business rule violation (capacity, invalid status transition) |
| 502 | Stripe / Cloudinary upstream error |

> Full DTO field definitions and enums are documented in [`BLERINA_HOUSE_Backend_Reference.md`](./BLERINA_HOUSE_Backend_Reference.md).

---

## Booking Flow

```
1. GET  /availability                    → show availability + estimated total
2. POST /reservations                    → create PENDING reservation (returns code + id)
3. POST /payments/intent {reservationId} → returns clientSecret (for Stripe Elements)
4. Frontend: stripe.confirmPayment(...)  → user pays
5. Stripe → webhook → backend marks reservation CONFIRMED (server-side)
6. GET  /reservations/{code}             → frontend reads real status (may need 1–2 retries)
```

The reservation holds for **~15 minutes** as `PENDING`. After that it auto-expires (`EXPIRED`) and the room is released. Confirmation is authoritative only from the **webhook**, never from the client response.

### Reservation state machine

```
PENDING ─▶ CONFIRMED ─▶ CHECKED_IN ─▶ CHECKED_OUT
PENDING / CONFIRMED ─▶ CANCELLED
PENDING ─▶ EXPIRED   (automatic, after hold timeout)
```

---

## Project Structure

```
backend/
├── src/main/java/com/blerinahouse/
│   ├── controller/      # REST endpoints (thin)
│   ├── service/         # business logic (booking engine, payments, images)
│   ├── repository/      # Spring Data JPA repositories + custom queries
│   ├── entity/          # JPA entities (match DB schema exactly)
│   ├── dto/             # request/response DTOs (never expose entities)
│   ├── mapper/          # MapStruct entity ↔ DTO mappers
│   ├── config/          # security, CORS, Cloudinary, Stripe config
│   ├── exception/       # GlobalExceptionHandler + ApiError
│   └── util/            # helpers
├── src/main/resources/
│   ├── application.yml          # non-secret config (profiles)
│   └── db/migration/            # Flyway migrations
└── pom.xml
```

---

## Security Notes

- All secrets are injected via environment variables — **nothing sensitive is committed**.
- Passwords are hashed with **BCrypt**.
- Admin routes are protected by JWT + `ROLE_ADMIN`.
- Stripe webhook signatures are verified using `STRIPE_WEBHOOK_SECRET`.
- Double-booking is prevented at the **database level**, not just in application code.
- For production: rotate the seed admin credentials, enable HTTPS, configure rate limiting, and restrict CORS to the real domain.

> If you cloned this repo and found the dev seed admin (`admin`/`admin123`), that credential is for local development only. **Never deploy it.**

---

## Roadmap

- ✅ **Phase 3** — Backend (complete)
- 🚧 **Phase 4** — Frontend (React + Vite + TypeScript + TailwindCSS)
- ⬜ **Phase 5** — Deployment (Docker Compose, Nginx, VPS, SSL, monitoring)

Planned backend follow-ups: refresh tokens, rate limiting, idempotency keys for `/reservations`, i18n validation messages.

---

## License

Proprietary — © 2026 Blerina House. All rights reserved.

---

*Built as a real production system for Blerina House, Sarti, Halkidiki, Greece.*
