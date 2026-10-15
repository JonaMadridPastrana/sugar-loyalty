# Sugar Loyalty

**Sugar Loyalty** is a loyalty-program web application for Sugar Pop. It provides a customer registration flow and a point-of-sale interface for managing customer points, purchases, and reward redemptions.

This repository is a portfolio project. It contains application source code and setup templates only; do not add real customer records, database backups, credentials, or Google Wallet service-account files.

## Features

- Customer registration and lookup.
- Point-of-sale workflows for recording purchases, adjusting points, and redeeming rewards.
- Transaction history backed by PostgreSQL.
- QR code generation, email notifications, and optional Google Wallet integration.
- Server-rendered pages with Thymeleaf and browser-side JavaScript.

## Technology

- Java 21 and Spring Boot
- Spring MVC, Thymeleaf, Bean Validation, Spring Data JPA
- PostgreSQL
- Docker and Docker Compose
- Google Wallet and SMTP integrations (credentials required)

## Architecture

The application is organized into familiar backend layers:

- `controller/` handles web pages and HTTP API requests.
- `dto/` defines request and response data and validation.
- `service/` implements loyalty workflows and external integrations.
- `repository/` accesses persisted data through Spring Data JPA.
- `model/` contains the customer and transaction entities.
- `templates/` contains the registration and POS pages.

## Run locally with Docker

Install Docker Desktop with Docker Compose, then run these commands in PowerShell from the repository folder:

```powershell
Copy-Item .env.example .env
```

Edit `.env` locally with the database, email, and Google Wallet settings. Never commit or share this file. Put the Google Wallet service-account JSON at `secrets/google-wallet-credentials.json` (or set `GOOGLE_WALLET_CREDENTIALS_PATH` in `.env` to its local path).

Start the application:

```powershell
docker compose up --build
```

Open:

- Registration page: <http://localhost:8081/registro>
- Point-of-sale page: <http://localhost:8081/pos>

Stop the containers while keeping the database volume:

```powershell
docker compose down
```

The database is persisted in a Docker volume. `docker compose down -v` also deletes that volume and its data; do not use it unless you intentionally want to erase the local database.

## Run tests

On Windows:

```powershell
.\mvnw.cmd test
```

The current integration test calls the Google Wallet API, so it requires working local Wallet credentials and network access. Do not configure production credentials in a public CI workflow.

## Security and deployment note

The POS and API are intended for a controlled environment. Do not expose them to the public internet until authentication, authorization, and production deployment safeguards have been implemented and reviewed. Use fictitious data for demonstrations.

## Portafolio

Sugar Loyalty es una aplicación web de fidelización para Sugar Pop. Incluye registro de clientes y flujos de punto de venta para administrar puntos, compras y canjes.

El proyecto demuestra una estructura backend por capas, persistencia con PostgreSQL, validación de solicitudes, integración con servicios externos y ejecución local con Docker Compose. Para una demostración, usa datos ficticios y credenciales de prueba almacenadas solo en archivos locales ignorados por Git.
