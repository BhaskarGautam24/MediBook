# SPEC.md — Project Specification

> **Status**: `FINALIZED`
> **Project**: MediBook — Online Appointment Booking System
> **Client Deadline**: 2026-04-25
> **Tagline**: Book Smarter. Heal Faster. Care Better.

## Vision

MediBook is a full-stack Online Appointment Booking System that enables patients to search for healthcare providers (doctors, consultants, therapists, specialists), view their available time slots, and book appointments online — all from a single, unified platform. The system supports three primary roles — **Patient**, **Provider**, and **Admin** — each with role-specific dashboards and distinct capabilities. The platform is architected as a microservices system with independent services for authentication, provider profiles, scheduling, appointments, payments, reviews, notifications, and medical records.

## Goals

1. Build a production-ready microservices backend using Java Spring Boot with clean, maintainable code
2. Build a modern, responsive React + Tailwind CSS frontend for all three user roles
3. Deliver core booking flow: provider search → slot viewing → appointment booking → payment → review
4. Implement provider availability management with recurring slot generation
5. Support electronic medical records with Cloudinary-based document attachments
6. Implement role-based access control with JWT authentication and OAuth2 social login
7. Provide admin dashboard for user management, provider verification, analytics, and moderation
8. Ensure daily incremental progress visible as GitHub commits

## Non-Goals (Out of Scope — v1.0)

- Redis caching (will add later)
- RabbitMQ async messaging (will add later)
- Video consultation backend (Twilio/Jitsi) — frontend placeholder only
- Email/SMS notifications (will add later — in-app only for now)
- Kubernetes orchestration (Docker only)
- HIPAA-compliant encryption (AES-256 at rest)
- Mobile app

## Users

| Role | Description |
|------|-------------|
| **Guest** | Unauthenticated visitor — browse providers, view profiles, check available slots |
| **Patient** | Registered user — book appointments, make payments, view records, submit reviews |
| **Provider** | Healthcare professional — manage availability, conduct appointments, create medical records |
| **Admin** | Platform administrator — manage users, verify providers, moderate content, view analytics |

## Technology Stack

| Layer | Technology |
|-------|------------|
| Frontend | React 18 + Tailwind CSS + FullCalendar.js |
| Backend | Java Spring Boot (Spring MVC, Spring Security, Spring Data JPA) |
| Authentication | JWT (JSON Web Tokens) + Spring Security OAuth2 (Google/GitHub) |
| Database | MySQL (one schema per microservice) |
| File Storage | Cloudinary (medical record attachments, profile pictures) |
| Payment Gateway | Razorpay / Stripe |
| Containerization | Docker + Docker Compose |
| API Documentation | Swagger / OpenAPI 3.0 |
| CI/CD | GitHub Actions (optional) |

## Microservices

| # | Service | Base Package | Responsibility |
|---|---------|-------------|----------------|
| 1 | auth-service | com.medibook.auth | Registration, login, JWT, OAuth2, profile management |
| 2 | provider-service | com.medibook.provider | Provider profiles, search, verification, rating aggregation |
| 3 | schedule-service | com.medibook.schedule | Availability slots, booking state, bulk/recurring generation |
| 4 | appointment-service | com.medibook.appointment | Booking lifecycle, status transitions |
| 5 | payment-service | com.medibook.payment | Payment processing, refunds, invoices, earnings |
| 6 | review-service | com.medibook.review | Patient reviews, star ratings, average computation |
| 7 | notification-service | com.medibook.notification | In-app notifications (email/SMS later) |
| 8 | record-service | com.medibook.record | Electronic medical records, prescriptions, follow-ups |

## Constraints

- **Deadline**: April 25, 2026 (5 days from start)
- **Daily commits**: Each day must show visible progress in GitHub
- **Clean code**: Well-organized, readable, properly commented
- **Self-tested**: All services must be tested and working before delivery

## Success Criteria

- [ ] All 8 microservices running and communicating via REST
- [ ] React frontend with complete UI for Patient, Provider, and Admin roles
- [ ] Full booking flow works end-to-end (search → book → pay → review)
- [ ] Provider can manage availability and create medical records
- [ ] Admin can verify providers, manage users, and view analytics
- [ ] JWT authentication and OAuth2 social login working
- [ ] Docker Compose spins up the entire system
- [ ] Swagger docs available for all service APIs
- [ ] Cloudinary integration for file uploads
- [ ] Clean, well-documented codebase
