# 🏥 MediBook Backend

**Book Smarter. Heal Faster. Care Better.**

A robust Spring Boot REST API backend for the Online Appointment Booking System. Built with enterprise-grade technologies to handle complex healthcare workflows with security, scalability, and reliability.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Features](#features)
- [Project Structure](#project-structure)
- [Installation](#installation)
- [Configuration](#configuration)
- [Database Setup](#database-setup)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Maven Build](#maven-build)
- [Testing](#testing)
- [Deployment](#deployment)

---

## 🎯 Overview

The MediBook backend is a comprehensive Spring Boot application that handles:
- **Authentication & Authorization**: JWT-based security with role-based access control
- **Appointment Management**: Complete lifecycle from booking to completion
- **User Management**: Patients, providers, and administrators
- **Payment Processing**: Integration with Razorpay and Stripe
- **Notifications**: Email and SMS notifications via Twilio
- **Medical Records**: Secure storage and retrieval of healthcare data
- **Video Consultations**: Signaling and coordination for real-time meetings

---

## 🛠 Tech Stack

| Technology | Purpose | Version |
|-----------|---------|---------|
| **Java** | Programming Language | 24 |
| **Spring Boot** | Framework | 3.4.4 |
| **Spring Security** | Authentication/Authorization | 3.4.4 |
| **Spring Data JPA** | ORM & Database Access | 3.4.4 |
| **Spring Validation** | Input Validation | 3.4.4 |
| **Spring Mail** | Email Service | 3.4.4 |
| **MySQL** | Database | Latest |
| **JWT (JJWT)** | Token Management | 0.12.5 |
| **Razorpay** | Payment Gateway | 1.4.7 |
| **Stripe** | Payment Gateway | 24.5.0 |
| **Twilio** | SMS Notifications | 10.1.0 |
| **Lombok** | Code Generation | 1.18.38 |
| **Maven** | Build Tool | 3.x |

---

## ✨ Features

### Authentication & Security
✅ JWT-based authentication  
✅ Role-based access control (RBAC)  
✅ Password encryption with BCrypt  
✅ Secure token refresh mechanism  
✅ CORS configuration for frontend integration  

### Appointment Management
✅ Book appointments with real-time availability  
✅ Cancel appointments with configurable window  
✅ Reschedule appointments  
✅ View appointment history  
✅ Appointment status tracking  

### Payment Integration
✅ Multiple payment gateway support (Razorpay/Stripe)  
✅ Secure payment processing  
✅ Refund handling  
✅ Payment history and reconciliation  
✅ Transaction logging  

### Notifications
✅ Email notifications for appointments  
✅ SMS notifications via Twilio  
✅ Appointment reminders  
✅ Payment confirmations  
✅ System alerts  

### User Management
✅ Patient registration and profile  
✅ Provider profile and qualifications  
✅ Admin user management  
✅ User activity tracking  
✅ Secure password reset  

### Medical Records
✅ Secure medical record storage  
✅ Prescription management  
✅ Medical history tracking  
✅ Healthcare provider access control  

### Scheduling
✅ Automated slot generation  
✅ Appointment reminder scheduler  
✅ Slot availability updates  
✅ Cron-based background tasks  

---

## 📁 Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/medibook/
│   │   │   ├── MediBookApplication.java     # Entry point
│   │   │   ├── admin/                       # Admin controllers
│   │   │   ├── config/                      # Spring configurations
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── CorsConfig.java
│   │   │   │   └── ...
│   │   │   ├── controller/                  # REST controllers
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── AppointmentController.java
│   │   │   │   ├── PaymentController.java
│   │   │   │   └── ...
│   │   │   ├── dto/                         # Data Transfer Objects
│   │   │   │   ├── AppointmentDTO.java
│   │   │   │   ├── UserDTO.java
│   │   │   │   └── ...
│   │   │   ├── entity/                      # JPA Entities
│   │   │   │   ├── User.java
│   │   │   │   ├── Appointment.java
│   │   │   │   ├── Payment.java
│   │   │   │   ├── MedicalRecord.java
│   │   │   │   └── ...
│   │   │   ├── enums/                       # Enumerations
│   │   │   │   ├── UserRole.java
│   │   │   │   ├── AppointmentStatus.java
│   │   │   │   ├── PaymentStatus.java
│   │   │   │   └── ...
│   │   │   ├── filter/                      # Security filters
│   │   │   │   └── JwtAuthenticationFilter.java
│   │   │   ├── repository/                  # Data access layer
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── AppointmentRepository.java
│   │   │   │   ├── PaymentRepository.java
│   │   │   │   └── ...
│   │   │   ├── scheduler/                   # Scheduled tasks
│   │   │   │   ├── AppointmentScheduler.java
│   │   │   │   └── ReminderScheduler.java
│   │   │   └── service/                     # Business logic
│   │   │       ├── UserService.java
│   │   │       ├── AppointmentService.java
│   │   │       ├── PaymentService.java
│   │   │       ├── NotificationService.java
│   │   │       ├── MedicalRecordService.java
│   │   │       └── ...
│   │   └── resources/
│   │       └── application.yml              # Configuration
│   └── test/                                 # Unit & integration tests
│       ├── test_api.py
│       ├── test_flow.py
│       ├── test_slots.py
│       ├── test_medical_records.py
│       ├── test_notification_flow.py
│       ├── e2e_test.py
│       ├── create_test_users.py
│       └── reset_db.py
├── pom.xml                                   # Maven configuration
└── .gitignore                                # Git ignore rules
```

---

## 🚀 Installation

### Prerequisites
- **Java Development Kit (JDK)** v24 or higher
- **Maven** 3.6+ or higher
- **MySQL Server** 5.7+
- **Git**

### Steps

1. **Clone the Repository**
   ```bash
   git clone https://github.com/BhaskarGautam24/Online-Appointment-Booking-System.git
   cd Online-Appointment-Booking-System
   git checkout backend
   ```

2. **Navigate to Backend Directory**
   ```bash
   cd backend
   ```

3. **Install Dependencies**
   ```bash
   mvn clean install
   ```

---

## ⚙️ Configuration

### Application Configuration (application.yml)

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/medibook?createDatabaseIfNotExist=true&useSSL=false
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect

  mail:
    host: sandbox.smtp.mailtrap.io
    port: 2525
    username: your_email
    password: your_password

app:
  jwt:
    secret: MediBookSecretKey2026ForJWTTokenSigningPurposeMustBe256BitsLongAtLeast
    expiration-ms: 86400000  # 24 hours

payment:
  gateway: razorpay  # or 'stripe' or 'mock'
  razorpay:
    key-id: your_key_id
    key-secret: your_key_secret
  stripe:
    secret-key: your_secret_key

twilio:
  account-sid: your_account_sid
  auth-token: your_auth_token
  messaging-service-sid: your_messaging_service_sid
```

### Update Configuration
1. Open `backend/src/main/resources/application.yml`
2. Update database credentials
3. Add API keys for external services
4. Configure JWT secret (use at least 256-bit key)

---

## 🗄️ Database Setup

### Create Database
```sql
CREATE DATABASE medibook CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE medibook;
```

### Tables Created Automatically
When the application starts with `ddl-auto: update`, Hibernate will automatically create:
- `users` - User accounts (patients, providers, admins)
- `appointments` - Appointment records
- `slots` - Provider availability slots
- `payments` - Payment transactions
- `medical_records` - Patient medical history
- `prescriptions` - Doctor prescriptions
- And other supporting tables

### Initialize Test Data
```bash
python backend/create_test_users.py
```

---

## 🎮 Running the Application

### Using Maven
```bash
mvn spring-boot:run
```

### Build and Run JAR
```bash
mvn clean package
java -jar target/medibook-1.0.0.jar
```

### Access the Application
- **Base URL**: `http://localhost:8080`
- **API Base**: `http://localhost:8080/api`

---

## 📚 API Documentation

### Core Endpoints

#### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | User registration |
| POST | `/api/auth/login` | User login |
| POST | `/api/auth/refresh` | Refresh JWT token |
| POST | `/api/auth/logout` | User logout |

#### Appointments
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/appointments` | Get user appointments |
| POST | `/api/appointments` | Book appointment |
| GET | `/api/appointments/{id}` | Get appointment details |
| PUT | `/api/appointments/{id}` | Update appointment |
| DELETE | `/api/appointments/{id}` | Cancel appointment |

#### Providers
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/providers` | List all providers |
| GET | `/api/providers/{id}` | Get provider details |
| GET | `/api/providers/{id}/slots` | Get provider slots |
| POST | `/api/providers/{id}/slots` | Create slots |

#### Payments
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/payments/initiate` | Initiate payment |
| POST | `/api/payments/verify` | Verify payment |
| GET | `/api/payments` | Payment history |

#### Medical Records
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/medical-records` | Get medical records |
| POST | `/api/medical-records` | Create record |
| GET | `/api/medical-records/{id}` | Record details |

---

## 🧪 Testing

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=TestClassName
```

### Test Files
- `test_api.py` - REST API endpoint tests
- `test_flow.py` - User flow tests
- `test_slots.py` - Slot management tests
- `test_medical_records.py` - Medical records tests
- `test_notification_flow.py` - Notification system tests
- `e2e_test.py` - End-to-end integration tests

### Run Python Tests
```bash
# Install dependencies
pip install requests

# Run individual test
python backend/test_api.py

# Run all tests
python backend/e2e_test.py
```

---

## 📦 Maven Build

### Build Phases

| Command | Description |
|---------|-------------|
| `mvn clean` | Remove build artifacts |
| `mvn compile` | Compile source code |
| `mvn package` | Create JAR/WAR file |
| `mvn install` | Install to local repository |
| `mvn deploy` | Deploy to remote repository |

### Generate Documentation
```bash
mvn javadoc:javadoc
```

---

## 🔐 Security Features

### JWT Token Management
- Tokens expire after 24 hours
- Refresh token mechanism for continuous sessions
- Secure token storage on client

### Password Security
- BCrypt hashing (strength 10)
- Complex password requirements
- Secure password reset flow

### CORS Configuration
- Configured for frontend domain
- Restricted HTTP methods
- Credential handling

### Role-Based Access Control
- ADMIN: Full system access
- PROVIDER: Access to own appointments and slots
- PATIENT: Access to own appointments and records

---

## 🚀 Deployment

### Prepare for Production

1. **Update Configuration**
   ```yaml
   server:
     servlet:
       context-path: /api
     error:
       include-message: never
       include-binding-errors: never
   
   spring:
     jpa:
       show-sql: false
       hibernate:
         ddl-auto: validate
   ```

2. **Build Production JAR**
   ```bash
   mvn clean package -DskipTests -Pproduction
   ```

3. **Deploy to Server**
   ```bash
   scp target/medibook-1.0.0.jar user@server:/app/
   ```

4. **Run Application**
   ```bash
   java -jar /app/medibook-1.0.0.jar
   ```

---

## 📞 Troubleshooting

### Database Connection Issues
- Verify MySQL is running
- Check credentials in `application.yml`
- Ensure database exists

### JWT Token Errors
- Clear browser cookies
- Refresh the page
- Re-login if token expired

### Payment Gateway Issues
- Verify API keys in configuration
- Check payment gateway status
- Review transaction logs

### Email/SMS Not Sending
- Verify Twilio and Mailtrap credentials
- Check network connectivity
- Review error logs

---

## 🤝 Contributing

1. Create feature branch: `git checkout -b feature/your-feature`
2. Commit changes: `git commit -m 'Add your feature'`
3. Push to branch: `git push origin feature/your-feature`
4. Open Pull Request

---

## 📄 License

This project is part of the Online Appointment Booking System by BhaskarGautam24.

---

## 📞 Support & Contact

For issues, questions, or suggestions:
- Create an issue on GitHub
- Contact the development team
- Check documentation in the repo

---

**Last Updated**: May 6, 2026  
**Version**: 1.0.0  
**Status**: Active Development  
**Java Version**: 24  
**Spring Boot Version**: 3.4.4
