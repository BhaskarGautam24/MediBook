---
phase: 1
plan: 1
wave: 1
---

# Plan 1.1: Backend Project Scaffolding & Infrastructure

## Objective
Create the Spring Boot multi-module Maven project structure with all 8 microservice modules, MySQL database setup via Docker Compose, and shared configuration. This is the foundation everything else builds on.

## Context
- .gsd/SPEC.md
- .gsd/ROADMAP.md

## Tasks

<task type="auto">
  <name>Create multi-module Maven project with all microservice modules</name>
  <files>
    d:\MediBook\pom.xml (parent POM)
    d:\MediBook\auth-service\pom.xml
    d:\MediBook\provider-service\pom.xml
    d:\MediBook\schedule-service\pom.xml
    d:\MediBook\appointment-service\pom.xml
    d:\MediBook\payment-service\pom.xml
    d:\MediBook\review-service\pom.xml
    d:\MediBook\notification-service\pom.xml
    d:\MediBook\record-service\pom.xml
  </files>
  <action>
    Create parent POM (com.medibook:medibook-platform) with:
    - Java 17, Spring Boot 3.2.x parent
    - Module declarations for all 8 services
    - Shared dependency management: Spring Web, Spring Data JPA, Spring Security, MySQL Connector, Lombok, Swagger (springdoc-openapi), Cloudinary, JWT (jjwt), Validation
    
    Create each service module with its own pom.xml inheriting from parent:
    - Each service is a standalone Spring Boot application
    - Each has its own application.yml with unique server port (8081-8088)
    - Each has its own database schema name
    - Standard package structure: entity, repository, service, controller, config, dto, exception
    
    Port assignments:
    - auth-service: 8081
    - provider-service: 8082
    - schedule-service: 8083
    - appointment-service: 8084
    - payment-service: 8085
    - review-service: 8086
    - notification-service: 8087
    - record-service: 8088
    
    DO NOT add Eureka/service discovery — services communicate via direct REST calls with RestTemplate/WebClient.
  </action>
  <verify>cd d:\MediBook && mvn validate -q</verify>
  <done>Parent POM validates successfully. All 8 module POMs exist with correct dependencies. Package structure directories created.</done>
</task>

<task type="auto">
  <name>Create Docker Compose with MySQL and service configuration</name>
  <files>
    d:\MediBook\docker-compose.yml
    d:\MediBook\docker\mysql\init.sql
    d:\MediBook\.gitignore
  </files>
  <action>
    Create docker-compose.yml with:
    - MySQL 8.0 container (port 3306) with root password and volume mount
    - init.sql that creates 8 databases: medibook_auth, medibook_provider, medibook_schedule, medibook_appointment, medibook_payment, medibook_review, medibook_notification, medibook_record
    - Named volume for MySQL data persistence
    
    Create .gitignore for Java/Maven/IDE files, node_modules, .env, target/, etc.
    
    Each service application.yml should connect to its own database schema with:
    - spring.datasource.url pointing to localhost:3306/{schema_name}
    - spring.jpa.hibernate.ddl-auto=update (for development)
    - spring.jpa.show-sql=true
  </action>
  <verify>cd d:\MediBook && docker-compose config</verify>
  <done>docker-compose.yml validates. MySQL init.sql creates all 8 databases. .gitignore covers all common patterns.</done>
</task>

## Success Criteria
- [ ] `mvn validate` passes on parent project
- [ ] All 8 service module directories exist with proper Maven/Spring Boot structure
- [ ] docker-compose.yml is valid and defines MySQL service
- [ ] Each service has application.yml with unique port and database
- [ ] .gitignore covers Java, Maven, Node, IDE, and env files
