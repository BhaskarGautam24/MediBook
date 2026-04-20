---
phase: 1
plan: 2
wave: 1
---

# Plan 1.2: Auth-Service — JWT Authentication & OAuth2

## Objective
Implement the complete auth-service with user registration, login, JWT token generation/validation, OAuth2 social login (Google/GitHub), profile management, and role-based access control. This is the security gateway for the entire platform.

## Context
- .gsd/SPEC.md (Auth/User-Service class diagram — Section 4.1)
- d:\MediBook\auth-service\

## Tasks

<task type="auto">
  <name>Implement User entity, DTOs, repository, and JWT utility</name>
  <files>
    d:\MediBook\auth-service\src\main\java\com\medibook\auth\entity\User.java
    d:\MediBook\auth-service\src\main\java\com\medibook\auth\repository\UserRepository.java
    d:\MediBook\auth-service\src\main\java\com\medibook\auth\dto\RegisterRequest.java
    d:\MediBook\auth-service\src\main\java\com\medibook\auth\dto\LoginRequest.java
    d:\MediBook\auth-service\src\main\java\com\medibook\auth\dto\AuthResponse.java
    d:\MediBook\auth-service\src\main\java\com\medibook\auth\dto\UserProfileDto.java
    d:\MediBook\auth-service\src\main\java\com\medibook\auth\dto\ChangePasswordRequest.java
    d:\MediBook\auth-service\src\main\java\com\medibook\auth\config\JwtUtil.java
    d:\MediBook\auth-service\src\main\java\com\medibook\auth\exception\GlobalExceptionHandler.java
    d:\MediBook\auth-service\src\main\java\com\medibook\auth\exception\UserAlreadyExistsException.java
    d:\MediBook\auth-service\src\main\java\com\medibook\auth\exception\InvalidCredentialsException.java
  </files>
  <action>
    User entity with fields from case study Section 4.1:
    - userId (Long, auto-generated), fullName, email (unique), passwordHash, phone, role (enum: PATIENT/PROVIDER/ADMIN), provider (OAuth provider name or null), isActive, createdAt, profilePicUrl
    - Use @Entity, @Table, Lombok @Data/@Builder/@NoArgsConstructor/@AllArgsConstructor
    
    UserRepository extends JpaRepository with methods:
    - findByEmail(), existsByEmail(), findAllByRole(), findByPhone(), findByFullNameContaining()
    - Use Optional<User> for single-result queries
    
    DTOs:
    - RegisterRequest: fullName, email, password, phone, role (default PATIENT)
    - LoginRequest: email, password
    - AuthResponse: token, userId, fullName, email, role
    - UserProfileDto: userId, fullName, email, phone, role, profilePicUrl, isActive, createdAt
    - ChangePasswordRequest: currentPassword, newPassword
    
    JwtUtil:
    - generateToken(User) → String (24h expiry, includes userId, email, role in claims)
    - validateToken(String token) → boolean
    - extractUserId(String token) → Long
    - extractEmail(String token) → String
    - extractRole(String token) → String
    - Use io.jsonwebtoken (jjwt) library, secret from application.yml
    
    GlobalExceptionHandler with @ControllerAdvice for consistent error responses.
  </action>
  <verify>cd d:\MediBook && mvn compile -pl auth-service -q</verify>
  <done>auth-service compiles. User entity, repository, all DTOs, JWT utility, and exception handlers exist.</done>
</task>

<task type="auto">
  <name>Implement AuthService, Security Config, and REST Controller</name>
  <files>
    d:\MediBook\auth-service\src\main\java\com\medibook\auth\service\AuthService.java
    d:\MediBook\auth-service\src\main\java\com\medibook\auth\service\AuthServiceImpl.java
    d:\MediBook\auth-service\src\main\java\com\medibook\auth\controller\AuthController.java
    d:\MediBook\auth-service\src\main\java\com\medibook\auth\config\SecurityConfig.java
    d:\MediBook\auth-service\src\main\java\com\medibook\auth\config\JwtAuthFilter.java
    d:\MediBook\auth-service\src\main\java\com\medibook\auth\config\SwaggerConfig.java
    d:\MediBook\auth-service\src\main\java\com\medibook\auth\config\CorsConfig.java
  </files>
  <action>
    AuthService interface with methods:
    - register(RegisterRequest) → AuthResponse
    - login(LoginRequest) → AuthResponse
    - validateToken(String token) → UserProfileDto
    - getUserById(Long userId) → UserProfileDto
    - getUserByEmail(String email) → UserProfileDto
    - updateProfile(Long userId, UserProfileDto) → UserProfileDto
    - changePassword(Long userId, ChangePasswordRequest) → void
    - deactivateAccount(Long userId) → void
    - getAllUsers() → List<UserProfileDto>
    - getUsersByRole(String role) → List<UserProfileDto>
    
    AuthServiceImpl:
    - register: validate unique email, hash password with BCryptPasswordEncoder, save user, return JWT
    - login: find by email, verify password, return JWT
    - validateToken: parse JWT, return user profile
    - Profile CRUD operations
    - Password change with old password verification
    
    AuthController (REST):
    - POST /api/v1/auth/register → register
    - POST /api/v1/auth/login → login
    - GET /api/v1/auth/validate → validate token (header: Authorization Bearer)
    - GET /api/v1/auth/profile/{userId} → get profile
    - PUT /api/v1/auth/profile/{userId} → update profile
    - PUT /api/v1/auth/password/{userId} → change password
    - PUT /api/v1/auth/deactivate/{userId} → deactivate
    - GET /api/v1/auth/users → all users (admin only)
    - GET /api/v1/auth/users/role/{role} → users by role
    
    SecurityConfig:
    - Permit: /api/v1/auth/register, /api/v1/auth/login, /api/v1/auth/validate, /swagger-ui/**, /v3/api-docs/**
    - All other endpoints require authentication
    - Add JwtAuthFilter before UsernamePasswordAuthenticationFilter
    - CORS enabled for React frontend (localhost:5173)
    - CSRF disabled (stateless JWT)
    
    JwtAuthFilter (OncePerRequestFilter):
    - Extract Bearer token from Authorization header
    - Validate via JwtUtil
    - Set SecurityContext with user details
    
    SwaggerConfig: OpenAPI 3.0 config with JWT bearer auth scheme
    CorsConfig: Allow localhost:5173 and localhost:3000
  </action>
  <verify>cd d:\MediBook && mvn compile -pl auth-service -q</verify>
  <done>auth-service compiles. All auth endpoints defined. Security config permits public and protects private routes. Swagger configured.</done>
</task>

## Success Criteria
- [ ] auth-service compiles with `mvn compile`
- [ ] User entity maps to MySQL table with all fields from case study
- [ ] JWT generation and validation works with 24h expiry
- [ ] Register, login, profile CRUD, password change endpoints defined
- [ ] Spring Security configured with JWT filter
- [ ] CORS allows React frontend origin
- [ ] Swagger UI accessible at /swagger-ui.html
