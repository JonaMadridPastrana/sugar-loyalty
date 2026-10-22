# Sugar Loyalty

**Sugar Loyalty** is a loyalty-program and point-of-sale (POS) web application for **Sugar Pop**. It provides a public customer registration flow with QR digital loyalty cards and a protected point-of-sale interface for managing customer points, purchases, dynamic reward catalogs, and birthday campaigns.

This repository is a portfolio project. It contains application source code and setup templates only; do not add real customer records, database backups, or credentials.

## Features

- **Customer Registration & QR Digital Card:** Public registration form that generates a downloadable/scannable QR code and sends an HTML welcome email.
- **Protected Point-of-Sale (POS):** Secured with **Spring Security** (authentication + CSRF protection) and camera-based QR scanning (`html5-qrcode`).
- **Points & Purchase Tracking:** Automatically awards 1 point per $10.00 MXN spent, updates customer balances transactionally, and emails a purchase summary showing unlocked rewards and progress toward upcoming rewards.
- **Dynamic Rewards Catalog:** In-POS reward manager to create and soft-delete (`activa = false`) rewards, with server-side validation of reward cost during redemption.
- **Birthday Tracker & Automated Campaigns:** Displays the current month's birthday customers in the POS header, highlights birthday customers on lookup, and runs a monthly `@Scheduled` cron job (plus manual trigger) to email birthday customers a free-dessert voucher.

## Technology

- **Java 21 LTS** and **Spring Boot 4.1.1**
- **Spring MVC**, **Thymeleaf**, **Jakarta Bean Validation**, **Spring Data JPA**
- **Spring Security** (Form Login & CSRF protection)
- **Spring Scheduling** (`@EnableScheduling` / `@Scheduled` cron jobs)
- **PostgreSQL 18**
- **ZXing** (QR code generation) & **Spring Mail** (HTML multipart emails)
- **Docker** and **Docker Compose**

## Architecture

The application follows a layered backend architecture:

- `config/` configures Spring Security authentication and endpoint authorization rules.
- `controller/` handles web views (`ViewController`) and REST API endpoints (`ClienteController`, `TransaccionController`, `RecompensaController`).
- `dto/` defines request/response contracts and Jakarta validation rules.
- `service/` implements business rules, transactional point operations, QR generation, HTML email templates, and scheduled birthday notifications (`CumpleanosScheduler`).
- `repository/` accesses persisted data through Spring Data JPA derived queries and custom JPQL (`@Query`).
- `model/` contains the JPA entities (`Cliente`, `Transaccion`, `Recompensa`).
- `templates/` contains the registration (`registro.html`) and point-of-sale (`pos.html`) views.

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

Stop the containers while keeping the database volume:

```powershell
docker compose down
```

## Run tests

On Windows (using JDK 21):

```powershell
.\mvnw.cmd test
```

## Portafolio (Español)

**Sugar Loyalty** es una aplicación web de fidelización y punto de venta (POS) para la repostería **Sugar Pop**. Incluye registro público de clientes con tarjeta QR digital, panel de caja protegido con Spring Security, catálogo administrable de recompensas, notificaciones dinámicas por correo electrónico tras cada compra y envío automatizado de regalos de cumpleaños cada inicio de mes.
