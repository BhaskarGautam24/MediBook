# DECISIONS.md — Architecture Decision Records

## ADR-001: React + Tailwind CSS over Thymeleaf + Bootstrap 5
- **Date**: 2026-04-20
- **Status**: Accepted
- **Context**: Case study specified Thymeleaf + Bootstrap 5 for server-side rendering
- **Decision**: Use React 18 + Tailwind CSS for a modern SPA experience
- **Rationale**: Better UX, modern tooling, reusable components, client preference

## ADR-002: Cloudinary over AWS S3
- **Date**: 2026-04-20
- **Status**: Accepted
- **Context**: Case study specified AWS S3 for medical record attachments
- **Decision**: Use Cloudinary for file storage
- **Rationale**: Simpler setup, built-in image transformations, generous free tier, no AWS account needed

## ADR-003: Defer RabbitMQ, Redis, Video Consultation, Email/SMS
- **Date**: 2026-04-20
- **Status**: Accepted
- **Context**: Case study includes RabbitMQ, Redis caching, video consultation, email/SMS
- **Decision**: Skip in v1.0, add incrementally post-delivery
- **Rationale**: 5-day deadline; focus on core booking flow first; daily progress visibility more important than breadth
