# 🏥 MediBook - Online Appointment Booking System

**Book Smarter. Heal Faster. Care Better.**

A comprehensive, full-stack healthcare appointment booking platform built with modern technologies. Connect patients with healthcare providers seamlessly, manage appointments efficiently, and streamline medical practice operations.

---

## 📋 Table of Contents

- [Project Overview](#project-overview)
- [System Architecture](#system-architecture)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Repository Structure](#repository-structure)
- [Quick Start](#quick-start)
- [Detailed Setup](#detailed-setup)
- [API Documentation](#api-documentation)
- [Security](#security)
- [Contributing](#contributing)

---

## 🎯 Project Overview

**MediBook** is an enterprise-level healthcare appointment booking system designed to bridge the gap between patients seeking medical services and healthcare providers offering their expertise. The platform provides:

- **Seamless Appointment Booking**: Real-time slot availability and instant booking confirmation
- **Multi-Role Support**: Dedicated interfaces for patients, providers, and administrators
- **Integrated Payments**: Secure payment processing with Razorpay and Stripe
- **Video Consultations**: Built-in video meeting capabilities for remote consultations
- **Medical Records Management**: Secure storage and retrieval of patient health information
- **Automated Notifications**: Email and SMS reminders and confirmations
- **Comprehensive Analytics**: System-wide insights for administrators

---

## 🏗 System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        MediBook Platform                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌──────────────────┐      ┌──────────────────┐                 │
│  │   Frontend       │      │    Backend API   │                 │
│  │  (React Vite)    │◄────►│  (Spring Boot)   │                 │
│  │  Port: 5173      │      │  Port: 8080      │                 │
│  └──────────────────┘      └──────────────────┘                 │
│         │                           │                            │
│         └───────────────┬───────────┘                            │
│                         │                                         │
│      ┌──────────────────┼──────────────────┐                    │
│      │                  │                  │                     │
│  ┌───▼────┐      ┌─────▼─────┐     ┌────▼──────┐               │
│  │ MySQL  │      │ Razorpay  │     │  Twilio   │               │
│  │Database│      │ / Stripe  │     │   SMS     │               │
│  └────────┘      └───────────┘     └───────────┘               │
│                                                                   │
│  Secure Authentication | JWT Tokens | CORS Configuration        │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

---

## ✨ Core Features

### 👥 Patient Features
- 🔐 Secure registration and authentication
- 🔍 Search and browse healthcare providers by specialty
- 📅 Real-time appointment slot booking
- ✏️ Appointment management (view, reschedule, cancel)
- 📞 Video consultations with providers
- 📋 Access medical records and health history
- 🔔 Appointment reminders via email/SMS
- 💳 Secure online payments

### 👨‍⚕️ Provider Features
- 📊 Interactive dashboard with key metrics
- 📅 Advanced slot management and scheduling
- 👁️ View all appointments and patient profiles
- 💰 Earnings tracking and revenue analytics
- 📞 Video consultation capabilities
- 📋 Patient medical records management
- ⚙️ Profile and qualification management

### 🛡️ Admin Features
- 📈 System-wide analytics and insights
- 👥 User management (patients, providers)
- 👨‍⚕️ Provider verification and management
- 📋 Appointment monitoring and reports
- 🔒 System security and compliance oversight
- 📊 Revenue and transaction tracking
- ⚙️ System configuration and settings

---

## 🛠 Tech Stack Comparison

| Layer | Technology | Version | Purpose |
|-------|-----------|---------|---------|
| **Frontend** | React | 19.2.5 | UI Framework |
| | Vite | 5.4.19 | Build Tool |
| | Tailwind CSS | 3.4.19 | Styling |
| | React Router | 7.14.1 | Routing |
| **Backend** | Spring Boot | 3.4.4 | Framework |
| | Java | 24 | Language |
| | Spring Security | 3.4.4 | Auth |
| **Database** | MySQL | 5.7+ | Data Storage |
| **APIs** | JWT | 0.12.5 | Authentication |
| | Razorpay | 1.4.7 | Payments |
| | Stripe | 24.5.0 | Payments |
| | Twilio | 10.1.0 | SMS |
| **Dev Tools** | Maven | 3.6+ | Build (Backend) |
| | npm | Latest | Package Manager |

---

## 📁 Repository Structure

```
Online-Appointment-Booking-System/
│
├── README.md                          # Main project documentation
├── FRONTEND_README.md                 # React frontend guide
├── BACKEND_README.md                  # Spring Boot backend guide
│
├── medibook-frontend/                 # React Frontend Branch
│   ├── src/
│   │   ├── components/               # Reusable components
│   │   ├── pages/                    # Page components
│   │   ├── layouts/                  # Layout wrappers
│   │   ├── context/                  # React Context (Auth)
│   │   ├── services/                 # API services
│   │   ├── App.jsx
│   │   └── main.jsx
│   ├── package.json
│   ├── vite.config.js
│   └── tailwind.config.js
│
├── backend/                           # Spring Boot Backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/medibook/
│   │   │   │   ├── config/          # Security, CORS configs
│   │   │   │   ├── controller/      # REST endpoints
│   │   │   │   ├── service/         # Business logic
│   │   │   │   ├── repository/      # Data access
│   │   │   │   ├── entity/          # JPA entities
│   │   │   │   ├── dto/             # Data transfer objects
│   │   │   │   └── filter/          # JWT filter
│   │   │   └── resources/
│   │   │       └── application.yml
│   │   └── test/                     # Test files
│   ├── pom.xml
│   └── .gitignore
│
└── Online Appointment Booking Case Study.pdf  # Project specification
```

### Branch Structure
- **`main`**: Default branch - Project documentation
- **`frontend`**: React Vite frontend application
- **`backend`**: Spring Boot REST API backend
- **`dev`**: Development branch for active work

---

## 🚀 Quick Start

### Prerequisites
- **Node.js** v16+ (for frontend)
- **Java JDK** 24+ (for backend)
- **Maven** 3.6+ (for backend build)
- **MySQL** 5.7+ (database)

### Frontend Setup (2 minutes)

```bash
# Clone and setup frontend
git clone https://github.com/BhaskarGautam24/Online-Appointment-Booking-System.git
cd Online-Appointment-Booking-System
git checkout frontend
cd medibook-frontend

# Install and run
npm install
npm run dev
# Access at http://localhost:5173
```

### Backend Setup (3 minutes)

```bash
# Clone and setup backend
git clone https://github.com/BhaskarGautam24/Online-Appointment-Booking-System.git
cd Online-Appointment-Booking-System
git checkout backend
cd backend

# Configure database
# Update: backend/src/main/resources/application.yml
# - Change database credentials
# - Add payment gateway keys

# Build and run
mvn clean install
mvn spring-boot:run
# API available at http://localhost:8080/api
```

---

## 📖 Detailed Setup

### Frontend Setup Guide

**See [FRONTEND_README.md](./FRONTEND_README.md) for detailed instructions**

```bash
cd medibook-frontend
npm install
npm run dev              # Development
npm run build            # Production build
npm run preview          # Preview build
```

**Environment Configuration** (`.env`):
```env
VITE_API_URL=http://localhost:8080/api
VITE_APP_NAME=MediBook
```

### Backend Setup Guide

**See [BACKEND_README.md](./BACKEND_README.md) for detailed instructions**

```bash
cd backend
mvn clean install
mvn spring-boot:run     # Development
mvn clean package       # Build JAR
```

**Configuration** (`application.yml`):
- Update MySQL credentials
- Configure JWT secret
- Add Razorpay/Stripe API keys
- Set Twilio SMS credentials

---

## 📡 API Documentation

### Base URL
```
http://localhost:8080/api
```

### Authentication Endpoints
```
POST   /auth/register          - User registration
POST   /auth/login             - User authentication
POST   /auth/refresh           - Refresh JWT token
POST   /auth/logout            - User logout
```

### Appointment Endpoints
```
GET    /appointments           - Get user appointments
POST   /appointments           - Book new appointment
GET    /appointments/{id}      - Get appointment details
PUT    /appointments/{id}      - Update appointment
DELETE /appointments/{id}      - Cancel appointment
```

### Provider Endpoints
```
GET    /providers              - List all providers
GET    /providers/{id}         - Get provider details
GET    /providers/{id}/slots   - Get available slots
POST   /providers/{id}/slots   - Create availability slots
```

### Payment Endpoints
```
POST   /payments/initiate      - Initiate payment
POST   /payments/verify        - Verify payment
GET    /payments               - Payment history
```

### Medical Records Endpoints
```
GET    /medical-records        - Get medical records
POST   /medical-records        - Create medical record
GET    /medical-records/{id}   - Get record details
```

**See [BACKEND_README.md](./BACKEND_README.md) for complete API documentation**

---

## 🔐 Security Features

### Authentication
- ✅ JWT-based token authentication
- ✅ Secure password hashing (BCrypt)
- ✅ Token refresh mechanism
- ✅ Logout with token invalidation

### Authorization
- ✅ Role-Based Access Control (RBAC)
- ✅ Route-level protection
- ✅ Resource-level permissions

### Data Protection
- ✅ HTTPS/SSL encryption support
- ✅ CORS configuration for frontend
- ✅ Input validation and sanitization
- ✅ SQL injection prevention (JPA)

### External Services
- ✅ Secure payment gateway integration
- ✅ Environment-based API key management
- ✅ Rate limiting support
- ✅ Audit logging

---

## 🧪 Testing

### Frontend Tests
```bash
cd medibook-frontend
# Testing setup (Vitest/Jest recommended)
npm run test
```

### Backend Tests
```bash
cd backend
# Unit tests
mvn test

# Integration tests
python test_api.py
python e2e_test.py
```

---

## 🚀 Deployment

### Frontend Deployment
```bash
cd medibook-frontend
npm run build
# Deploy dist/ folder to hosting (Vercel, Netlify, etc.)
```

### Backend Deployment
```bash
cd backend
mvn clean package -DskipTests
# Deploy JAR to server/cloud (AWS, Azure, GCP, etc.)
java -jar target/medibook-1.0.0.jar
```

---

## 📚 Documentation

| Document | Purpose |
|----------|---------|
| [FRONTEND_README.md](./FRONTEND_README.md) | React frontend setup and development guide |
| [BACKEND_README.md](./BACKEND_README.md) | Spring Boot backend setup and API docs |
| Case Study PDF | Complete project requirements and specifications |

---

## 🤝 Contributing

### How to Contribute

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Commit** your changes (`git commit -m 'Add amazing feature'`)
4. **Push** to the branch (`git push origin feature/amazing-feature`)
5. **Open** a Pull Request

### Code Standards
- Follow existing code style and conventions
- Write meaningful commit messages
- Update documentation for new features
- Test your changes thoroughly

---

## 📞 Support & Contact

### Getting Help
- 📖 Check documentation files (FRONTEND_README.md, BACKEND_README.md)
- 🐛 Create an issue on GitHub for bugs
- 💬 Start a discussion for feature requests
- 📧 Contact: BhaskarGautam24@github.com

### Reporting Issues
Include:
- Clear description of the issue
- Steps to reproduce
- Expected vs actual behavior
- Environment details (OS, versions)
- Screenshots/logs if applicable

---

## 📄 License

This project is licensed under the MIT License. See the LICENSE file for details.

---

## 🙏 Acknowledgments

- Built with React, Spring Boot, and MySQL
- Payment integration with Razorpay and Stripe
- SMS notifications powered by Twilio
- Inspired by healthcare delivery best practices

---

## 📊 Project Status

| Component | Status | Version |
|-----------|--------|---------|
| Frontend | ✅ Active | 1.0.0 |
| Backend | ✅ Active | 1.0.0 |
| Documentation | ✅ Complete | 1.0.0 |
| Testing | ✅ In Progress | - |
| Deployment | ✅ Ready | - |

---

## 🗺 Roadmap

- ✅ Core appointment booking system
- ✅ Multi-user role support
- ✅ Payment integration
- ✅ Video consultation
- ⏳ Advanced analytics dashboard
- ⏳ Mobile application
- ⏳ AI-based doctor recommendations
- ⏳ Prescription management system
- ⏳ Telemedicine enhancements

---

**Last Updated**: May 6, 2026  
**Version**: 1.0.0  
**Status**: Active Development  
**Author**: BhaskarGautam24

---

### Quick Links
- 🌐 [Frontend Repository](https://github.com/BhaskarGautam24/Online-Appointment-Booking-System/tree/frontend)
- 🔧 [Backend Repository](https://github.com/BhaskarGautam24/Online-Appointment-Booking-System/tree/backend)
- 📖 [Frontend Guide](./FRONTEND_README.md)
- 📖 [Backend Guide](./BACKEND_README.md)
