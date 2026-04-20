# STATE.md — Project Memory

> **Last Updated**: 2026-04-20
> **Current Phase**: Phase 1 — Foundation & Auth
> **Blockers**: None

## Active Context
- Starting greenfield MediBook project
- 5-day delivery timeline (April 20–25, 2026)
- Core-first approach: auth → providers → scheduling → appointments → payments → reviews → notifications → records

## Key Decisions
- React + Tailwind CSS over Thymeleaf + Bootstrap 5
- Cloudinary over AWS S3
- No RabbitMQ, Redis, video consultation, email/SMS in v1.0
- Daily GitHub commits required for client visibility

## Tech Stack Confirmed
- Frontend: React 18 + Tailwind CSS + FullCalendar.js
- Backend: Java Spring Boot microservices
- Auth: JWT + OAuth2 (Google/GitHub)
- DB: MySQL
- Files: Cloudinary
- Payments: Razorpay/Stripe
- Infra: Docker + Docker Compose
- Docs: Swagger/OpenAPI 3.0
