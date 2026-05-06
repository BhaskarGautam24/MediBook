# 🎨 MediBook Frontend

**Book Smarter. Heal Faster. Care Better.**

A modern, responsive React-based frontend for the Online Appointment Booking System. Built with cutting-edge technologies to provide seamless healthcare appointment management.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Features](#features)
- [Project Structure](#project-structure)
- [Installation](#installation)
- [Running the Application](#running-the-application)
- [Available Scripts](#available-scripts)
- [Dependencies](#dependencies)
- [Key Components](#key-components)
- [Pages & Routes](#pages--routes)
- [Configuration](#configuration)
- [Best Practices](#best-practices)

---

## 🎯 Overview

The MediBook frontend is a comprehensive React application designed to handle three distinct user roles:
- **Patients**: Book appointments, view medical history, video consultations
- **Healthcare Providers**: Manage schedules, view earnings, handle appointments
- **Administrators**: System oversight, user management, analytics

The application features a clean, intuitive UI with real-time updates and smooth user experience.

---

## 🛠 Tech Stack

| Technology | Purpose | Version |
|-----------|---------|---------|
| **React** | UI Library | 19.2.5 |
| **Vite** | Build Tool & Dev Server | 5.4.19 |
| **React Router DOM** | Client-side Routing | 7.14.1 |
| **Tailwind CSS** | Utility-first CSS | 3.4.19 |
| **FullCalendar React** | Calendar Component | 6.1.20 |
| **Zustand** | State Management | 5.0.12 |
| **Recharts** | Data Visualization | 3.8.1 |
| **React Hot Toast** | Notifications | 2.6.0 |
| **Lucide React** | Icon Library | 1.12.0 |
| **PostCSS** | CSS Processing | 8.5.12 |

---

## ✨ Features

### Patient Features
✅ Browse available healthcare providers  
✅ View provider profiles and availability  
✅ Book appointments with real-time slot availability  
✅ Manage and track appointments  
✅ Video consultations with providers  
✅ Access medical records and history  
✅ Secure authentication & profile management  

### Provider Features
✅ Comprehensive dashboard with key metrics  
✅ Manage appointment slots and availability  
✅ View all appointments and patient details  
✅ Track earnings and revenue  
✅ Video consultation capabilities  
✅ Patient management system  

### Admin Features
✅ System-wide dashboard and analytics  
✅ Manage providers and user accounts  
✅ Monitor all appointments  
✅ Access medical records  
✅ User activity tracking  
✅ System configuration management  

---

## 📁 Project Structure

```
medibook-frontend/
├── public/                 # Static assets
├── src/
│   ├── components/        # Reusable React components
│   │   ├── ProtectedRoute.jsx
│   │   └── ...           # Other components
│   ├── context/          # React Context (Auth, etc.)
│   │   └── AuthContext.jsx
│   ├── layouts/          # Layout components
│   │   ├── MainLayout.jsx
│   │   ├── DashboardLayout.jsx
│   │   └── AdminLayout.jsx
│   ├── pages/            # Page components
│   │   ├── HomePage.jsx
│   │   ├── auth/         # Authentication pages
│   │   ├── patient/      # Patient-specific pages
│   │   ├── provider/     # Provider-specific pages
│   │   ├── admin/        # Admin-specific pages
│   │   └── Meet.jsx      # Video consultation
│   ├── services/         # API services & utilities
│   ├── App.jsx          # Main App component
│   ├── main.jsx         # Entry point
│   └── index.css        # Global styles
├── package.json         # Dependencies & scripts
├── vite.config.js       # Vite configuration
├── tailwind.config.js   # Tailwind CSS config
├── postcss.config.js    # PostCSS config
└── index.html           # HTML template
```

---

## 🚀 Installation

### Prerequisites
- **Node.js** (v16 or higher)
- **npm** or **yarn** package manager

### Steps

1. **Clone the Repository**
   ```bash
   git clone https://github.com/BhaskarGautam24/Online-Appointment-Booking-System.git
   cd Online-Appointment-Booking-System
   git checkout frontend
   ```

2. **Navigate to Frontend Directory**
   ```bash
   cd medibook-frontend
   ```

3. **Install Dependencies**
   ```bash
   npm install
   ```

4. **Configure Environment Variables**
   Create a `.env` file in the `medibook-frontend` directory:
   ```env
   VITE_API_URL=http://localhost:8080/api
   VITE_APP_NAME=MediBook
   ```

---

## 🎮 Running the Application

### Development Mode
```bash
npm run dev
```
The application will start at `http://localhost:5173`

### Production Build
```bash
npm run build
```

### Preview Production Build
```bash
npm run preview
```

---

## 📜 Available Scripts

| Command | Description |
|---------|-------------|
| `npm run dev` | Start development server with HMR |
| `npm run build` | Create optimized production build |
| `npm run preview` | Preview production build locally |

---

## 📦 Dependencies

### Core Dependencies
```json
{
  "react": "^19.2.5",
  "react-dom": "^19.2.5",
  "react-router-dom": "^7.14.1",
  "zustand": "^5.0.12"
}
```

### UI & Styling
```json
{
  "tailwindcss": "^3.4.19",
  "postcss": "^8.5.12",
  "autoprefixer": "^10.5.0",
  "lucide-react": "^1.12.0"
}
```

### Features
```json
{
  "@fullcalendar/react": "^6.1.20",
  "@fullcalendar/daygrid": "^6.1.20",
  "@fullcalendar/timegrid": "^6.1.20",
  "@fullcalendar/interaction": "^6.1.20",
  "recharts": "^3.8.1",
  "react-hot-toast": "^2.6.0"
}
```

---

## 🧩 Key Components

### ProtectedRoute
Wrapper component for route-based access control. Restricts pages based on user roles (PATIENT, PROVIDER, ADMIN).

```jsx
<ProtectedRoute allowedRoles={['PATIENT']}>
  <DashboardLayout />
</ProtectedRoute>
```

### AuthContext
Global authentication state management using React Context API. Handles:
- User login/logout
- Token management
- User role verification
- Protected resources

### Layouts
- **MainLayout**: For public pages (home, login, register)
- **DashboardLayout**: For patient and provider dashboards
- **AdminLayout**: For admin panel

---

## 🗺 Pages & Routes

### Public Routes
| Route | Component | Description |
|-------|-----------|-------------|
| `/` | HomePage | Landing page |
| `/login` | LoginPage | User authentication |
| `/register` | RegisterPage | New user registration |
| `/providers` | ProviderList | Browse all providers |
| `/providers/:providerId/slots` | ProviderSlots | View provider availability |

### Patient Routes (Protected)
| Route | Component | Description |
|-------|-----------|-------------|
| `/patient/appointments` | MyAppointments | Manage patient appointments |

### Provider Routes (Protected)
| Route | Component | Description |
|-------|-----------|-------------|
| `/provider/dashboard` | ProviderDashboard | Provider metrics & overview |
| `/provider/slots` | ManageSlots | Manage appointment slots |
| `/provider/appointments` | ProviderAppointments | View all appointments |
| `/provider/earnings` | ProviderEarnings | Revenue and earnings tracking |

### Admin Routes (Protected)
| Route | Component | Description |
|-------|-----------|-------------|
| `/admin/dashboard` | AdminDashboard | System analytics & overview |
| `/admin/providers` | ManageProviders | Provider management |
| `/admin/users` | ManageUsers | User account management |
| `/admin/appointments` | AdminAppointments | All appointments monitoring |
| `/admin/records` | AdminRecords | Medical records access |

### Shared Routes
| Route | Component | Description |
|-------|-----------|-------------|
| `/meet/:id` | Meet | Video consultation (patient/provider) |

---

## ⚙️ Configuration

### Tailwind CSS
Configured in `tailwind.config.js` for custom theming and utility classes.

### Vite Configuration
- HMR enabled for fast refresh during development
- Optimized builds for production
- React plugin integration

### PostCSS
Autoprefixer and Tailwind CSS processing for cross-browser compatibility.

---

## 💡 Best Practices

### Component Structure
- Keep components small and focused
- Use functional components with hooks
- Implement proper error boundaries
- Leverage Tailwind for consistent styling

### State Management
- Use Zustand for global state
- Use Context API for authentication
- Keep local component state minimal

### Performance
- Code splitting with React Router
- Lazy loading for routes and components
- Optimize re-renders using React.memo
- Use production build for deployment

### Security
- Validate all user inputs
- Store tokens securely
- Implement proper CORS handling
- Use environment variables for sensitive data

---

## 🤝 Contributing

1. Create a feature branch: `git checkout -b feature/your-feature`
2. Commit changes: `git commit -m 'Add your feature'`
3. Push to branch: `git push origin feature/your-feature`
4. Open a Pull Request

---

## 📞 Support & Contact

For issues, questions, or suggestions:
- Create an issue on GitHub
- Contact the development team
- Check documentation in the repo

---

## 📄 License

This project is part of the Online Appointment Booking System by BhaskarGautam24.

---

**Last Updated**: May 6, 2026  
**Version**: 1.0.0  
**Status**: Active Development
