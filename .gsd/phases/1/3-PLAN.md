---
phase: 1
plan: 3
wave: 2
---

# Plan 1.3: React Frontend Initialization & Auth Pages

## Objective
Initialize the React + Tailwind CSS frontend with routing, auth context, login/register pages, and protected route system. The frontend should connect to the auth-service and establish the foundation for all subsequent UI work.

## Context
- .gsd/SPEC.md
- d:\MediBook\auth-service\ (for API endpoints)

## Tasks

<task type="auto">
  <name>Initialize React app with Vite, Tailwind CSS, and routing</name>
  <files>
    d:\MediBook\medibook-frontend\ (entire React app)
  </files>
  <action>
    Initialize React app with Vite in d:\MediBook\medibook-frontend\:
    - Use: npx -y create-vite@latest medibook-frontend -- --template react
    - Install dependencies: react-router-dom, axios, @fullcalendar/react, @fullcalendar/daygrid, @fullcalendar/timegrid, @fullcalendar/interaction, react-icons, react-hot-toast
    - Install Tailwind CSS 3: tailwindcss, postcss, autoprefixer
    - Configure tailwind.config.js with custom MediBook color palette:
      - primary: medical blue (#2563EB gradient range)
      - secondary: teal/mint (#0D9488 range)
      - accent: warm amber (#F59E0B range)
      - neutral: slate grays
    - Add Inter font from Google Fonts
    
    Create project structure:
    - src/components/ (shared UI components)
    - src/pages/ (route pages)
    - src/pages/auth/ (login, register)
    - src/pages/patient/ (patient dashboard pages)
    - src/pages/provider/ (provider dashboard pages)
    - src/pages/admin/ (admin dashboard pages)
    - src/context/ (auth context)
    - src/services/ (API service layer)
    - src/hooks/ (custom hooks)
    - src/layouts/ (layout components)
    - src/utils/ (utility functions)
    
    Set up React Router with:
    - Public routes: /, /login, /register, /providers, /providers/:id
    - Patient routes: /patient/dashboard, /patient/appointments, /patient/records, /patient/notifications
    - Provider routes: /provider/dashboard, /provider/availability, /provider/appointments, /provider/records, /provider/earnings, /provider/reviews
    - Admin routes: /admin/dashboard, /admin/users, /admin/providers, /admin/appointments, /admin/payments, /admin/reviews, /admin/analytics
    - ProtectedRoute component that checks auth and redirects to /login
    - RoleBasedRoute component that checks user role
  </action>
  <verify>cd d:\MediBook\medibook-frontend && npm run build</verify>
  <done>React app builds successfully. Tailwind CSS configured. All route paths defined. Project structure created.</done>
</task>

<task type="auto">
  <name>Build auth context, API service, and login/register pages</name>
  <files>
    d:\MediBook\medibook-frontend\src\context\AuthContext.jsx
    d:\MediBook\medibook-frontend\src\services\api.js
    d:\MediBook\medibook-frontend\src\services\authService.js
    d:\MediBook\medibook-frontend\src\pages\auth\LoginPage.jsx
    d:\MediBook\medibook-frontend\src\pages\auth\RegisterPage.jsx
    d:\MediBook\medibook-frontend\src\pages\HomePage.jsx
    d:\MediBook\medibook-frontend\src\components\Navbar.jsx
    d:\MediBook\medibook-frontend\src\layouts\MainLayout.jsx
    d:\MediBook\medibook-frontend\src\layouts\DashboardLayout.jsx
  </files>
  <action>
    AuthContext (React Context + useReducer):
    - State: user, token, isAuthenticated, isLoading
    - Actions: login, register, logout, updateProfile
    - Persist token in localStorage
    - On app load, validate stored token via /api/v1/auth/validate
    - Provide: user, token, isAuthenticated, login(), register(), logout()
    
    API service (axios):
    - Base URL: http://localhost:8081 (auth-service, configurable via env)
    - Request interceptor: attach JWT Bearer token from localStorage
    - Response interceptor: handle 401 → redirect to login
    
    authService:
    - register(data) → POST /api/v1/auth/register
    - login(data) → POST /api/v1/auth/login
    - validateToken() → GET /api/v1/auth/validate
    - getProfile(userId) → GET /api/v1/auth/profile/{userId}
    - updateProfile(userId, data) → PUT /api/v1/auth/profile/{userId}
    
    LoginPage:
    - Clean, modern design with medical theme
    - Email + password fields with validation
    - "Login" button with loading state
    - Link to register page
    - OAuth buttons (Google/GitHub) — styled but non-functional placeholder for now
    - Error toast on failure, redirect to role-based dashboard on success
    
    RegisterPage:
    - Full name, email, password, confirm password, phone
    - Role selector: Patient (default) or Provider
    - Validation (email format, password match, required fields)
    - On success: auto-login and redirect
    
    HomePage:
    - Hero section with tagline "Book Smarter. Heal Faster. Care Better."
    - Search bar placeholder (for provider search)
    - Feature highlights cards
    - Call-to-action buttons
    
    Navbar:
    - Logo + app name
    - Nav links based on auth state and role
    - Profile dropdown when logged in
    - Login/Register buttons when logged out
    
    MainLayout: Navbar + content area (for public pages)
    DashboardLayout: Sidebar + topbar + content area (for authenticated pages)
    
    DESIGN REQUIREMENTS:
    - Use glassmorphism effects for cards
    - Smooth transitions and hover animations
    - Medical blue/teal color scheme
    - Inter font throughout
    - Fully responsive (mobile-first)
    - Dark mode support via Tailwind dark: classes
  </action>
  <verify>cd d:\MediBook\medibook-frontend && npm run build</verify>
  <done>Frontend builds. Login/Register pages render with proper styling. AuthContext provides authentication state. API service configured. Navbar and layouts work.</done>
</task>

## Success Criteria
- [ ] `npm run build` succeeds
- [ ] React Router configured with all planned routes
- [ ] AuthContext manages login/logout/token state
- [ ] Login page with email/password form and OAuth placeholders
- [ ] Register page with role selection and validation
- [ ] Home page with hero, search placeholder, and feature cards
- [ ] Navbar adapts to auth state (logged in vs logged out)
- [ ] Dashboard layout with sidebar ready for role-specific pages
- [ ] Tailwind CSS with custom MediBook color palette
- [ ] Responsive design works on mobile and desktop
