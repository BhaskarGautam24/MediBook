# ROADMAP.md

> **Current Phase**: Not started
> **Milestone**: v1.0 — MVP Delivery (April 25, 2026)

## Must-Haves (from SPEC)

- [ ] Auth-service with JWT + OAuth2
- [ ] Provider-service with search and verification
- [ ] Schedule-service with slot management
- [ ] Appointment-service with full booking lifecycle
- [ ] Payment-service with Razorpay/Stripe
- [ ] Review-service with ratings
- [ ] Notification-service (in-app)
- [ ] Record-service with Cloudinary
- [ ] React + Tailwind CSS frontend for all roles
- [ ] Docker Compose deployment
- [ ] Swagger API documentation

## Phases

### Phase 1: Foundation & Auth (Day 1 — April 20)
**Status**: ⬜ Not Started
**Objective**: Project scaffolding, database setup, auth-service with JWT + OAuth2, React app initialization with routing and auth pages
**Deliverables**:
- Spring Boot multi-module Maven project structure
- MySQL database schemas
- auth-service: register, login, JWT generation/validation, OAuth2 (Google/GitHub), profile CRUD
- React app with Tailwind CSS, routing, login/register pages
- Docker Compose for MySQL + services
- Swagger config

### Phase 2: Provider & Scheduling (Day 2 — April 21)
**Status**: ⬜ Not Started
**Objective**: Provider profiles, search/filter, admin verification, availability slot management with recurring generation
**Deliverables**:
- provider-service: CRUD, search by specialization/name/location, verification, rating aggregation
- schedule-service: slot CRUD, bulk creation, recurring generation, block/unblock
- React: provider directory, provider profile page, availability calendar (FullCalendar.js), provider dashboard
**Requirements**: Phase 1 (auth-service must be running)

### Phase 3: Appointments & Booking Flow (Day 3 — April 22)
**Status**: ⬜ Not Started
**Objective**: Complete appointment booking lifecycle and patient booking UI
**Deliverables**:
- appointment-service: book, cancel, reschedule, complete, status transitions, no-show detection
- React: booking flow (select slot → confirm → payment), appointment history, patient dashboard
- Inter-service communication: appointment → schedule (mark slot booked/released)
**Requirements**: Phase 2 (provider + schedule services)

### Phase 4: Payments, Reviews & Notifications (Day 4 — April 23)
**Status**: ⬜ Not Started
**Objective**: Payment processing, review system, and in-app notifications
**Deliverables**:
- payment-service: process payment, refund, invoice generation, earnings dashboard
- review-service: star ratings, written reviews, average computation, moderation
- notification-service: in-app notifications for booking events, reminders
- React: payment flow, review submission, notification center, provider earnings page
**Requirements**: Phase 3 (appointment-service)

### Phase 5: Medical Records, Admin Panel & Polish (Day 5 — April 24–25)
**Status**: ⬜ Not Started
**Objective**: Medical records with Cloudinary, admin dashboard, Docker deployment, final testing
**Deliverables**:
- record-service: CRUD, Cloudinary file upload, follow-up tracking
- React: medical records view, admin dashboard (user management, provider verification, analytics, review moderation, revenue reports)
- Video consultation UI placeholder
- Docker Compose for all services
- Swagger documentation for all APIs
- End-to-end testing and bug fixes
- Final polish and cleanup
**Requirements**: Phase 4 (all services)
