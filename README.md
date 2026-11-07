# Sugar Loyalty

**Sugar Loyalty** is a full-stack loyalty-program and point-of-sale (POS) platform for **Sugar Pop**. It provides a public customer registration flow, a Progressive Web App (PWA) customer portal with QR digital loyalty cards, and a protected point-of-sale interface for managing customer points, purchases, dynamic reward catalogs, and birthday campaigns.

This repository is a portfolio project. It contains application source code and setup templates only; do not add real customer records, database backups, or credentials.

## Features

- **Customer Registration & QR Digital Card:** Public registration form (`POST /api/clientes`) that generates a downloadable/scannable QR code and sends an HTML welcome email.
- **PWA Customer Portal Authentication:** Server-side credential verification (`POST /api/clientes/login-cliente` via `ClienteLoginDTO`) that validates both phone number and registered email before returning customer points, active rewards, and transaction history, with CORS configured for the mobile PWA hosted on Netlify.
- **Protected Point-of-Sale (POS):** Secured with **Spring Security** (authentication + CSRF protection + 24h session keep-alive) and camera-based QR scanning (`html5-qrcode`).
- **Points & Purchase Tracking:** Automatically awards 1 point per $10.00 MXN spent, updates customer balances transactionally, and emails a purchase summary showing unlocked rewards and progress toward upcoming rewards.
- **Dynamic Rewards Catalog:** In-POS reward manager to create and soft-delete (`activa = false`) rewards, with server-side validation of reward cost during redemption.
- **Birthday Tracker & Automated Campaigns:** Displays the current month's birthday customers in the POS header, highlights birthday customers on lookup, and runs a monthly `@Scheduled` cron job (plus manual trigger) to email birthday customers a free-dessert voucher.

## Technology

- **Java 21 LTS** and **Spring Boot 4.1.1**
- **Spring MVC**, **Thymeleaf**, **Jakarta Bean Validation**, **Spring Data JPA**
- **Spring Security** (Form Login, CSRF protection & CORS configuration for PWA)
- **Spring Scheduling & Async** (`@EnableScheduling`, `@Scheduled` cron jobs, and `@EnableAsync` / `@Async` non-blocking background email dispatch)
- **PostgreSQL** (Cloud deployment on **Neon.tech** / local PostgreSQL 18)
- **ZXing** (QR code generation) & **Dual Email Delivery** (HTTPS Webhook relay via Google Apps Script with Unicode-safe JSON + `MailConfig` SMTP fallback with 5s timeouts)
- **Docker** (Cloud deployment on **Render.com** with `UTF-8` JVM encoding & local Docker Compose)

## Architecture

The application follows a layered backend architecture:

- `config/` configures Spring Security authentication, CORS rules (`SecurityConfig`), and explicit mail bean configuration (`MailConfig`).
- `controller/` handles web views (`ViewController`) and REST API endpoints (`ClienteController`, `TransaccionController`, `RecompensaController`).
- `dto/` defines request/response contracts (`ClienteRegistroDTO`, `ClienteLoginDTO`, `ClienteResponseDTO`, `TransaccionRegistroDTO`, `CanjeRegistroDTO`, etc.) and Jakarta validation rules.
- `service/` implements business rules, server-side customer authentication, transactional point operations, QR generation, HTML email templates, and scheduled birthday notifications (`CumpleanosScheduler`).
- `repository/` accesses persisted data through Spring Data JPA derived queries and custom JPQL (`@Query`).
- `model/` contains the JPA entities (`Cliente`, `Transaccion`, `Recompensa`).
- `templates/` contains the registration (`registro.html`) and point-of-sale (`pos.html`) views.

## Environment Variables (Cloud / Render Deployment)

Configure the following environment variables in your cloud provider (never commit real secrets to Git):

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_MAIL_USERNAME`
- `SPRING_MAIL_PASSWORD`
- `SPRING_SECURITY_USER_NAME`
- `SPRING_SECURITY_USER_PASSWORD`

## Run locally with Docker

Install Docker Desktop with Docker Compose, then run these commands in PowerShell from the repository folder:

```powershell
Copy-Item .env.example .env
```

Edit `.env` locally with your database and SMTP settings. Never commit or share this file.

Start the application:

```powershell
docker compose up --build
```

Open:

- Registration page: <http://localhost:8081/registro>
- Point-of-sale page: <http://localhost:8081/pos>

## Run tests

On Windows (using JDK 21):

```powershell
.\mvnw.cmd test
```

## Portafolio (Español)

**Sugar Loyalty** es una plataforma full-stack de fidelización y punto de venta (POS) para la repostería **Sugar Pop**, desplegada en la nube con **Render** (Backend Docker), **Neon** (PostgreSQL Serverless) y **Netlify** (PWA de clientes en React). Incluye registro e inicio de sesión seguro de clientes con tarjeta QR digital, panel de caja protegido con Spring Security, catálogo administrable de recompensas, notificaciones dinámicas por correo electrónico tras cada compra y envío automatizado de regalos de cumpleaños cada inicio de mes.
