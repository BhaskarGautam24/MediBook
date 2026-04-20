import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AuthProvider } from './context/AuthContext';

// Layouts
import MainLayout from './layouts/MainLayout';
import DashboardLayout from './layouts/DashboardLayout';

// Components
import ProtectedRoute from './components/ProtectedRoute';

// Public Pages
import HomePage from './pages/HomePage';
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';
import PlaceholderPage from './pages/PlaceholderPage';

// Dashboard Pages
import PatientDashboard from './pages/patient/PatientDashboard';
import ProviderDashboard from './pages/provider/ProviderDashboard';
import AdminDashboard from './pages/admin/AdminDashboard';

export default function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          {/* ===== Public Routes ===== */}
          <Route element={<MainLayout />}>
            <Route path="/" element={<HomePage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/providers" element={<PlaceholderPage title="Find Doctors" description="Provider directory with search and filtering — coming in Phase 2." />} />
            <Route path="/providers/:id" element={<PlaceholderPage title="Provider Profile" description="Detailed provider profile with availability calendar — coming in Phase 2." />} />
          </Route>

          {/* ===== Patient Routes ===== */}
          <Route element={
            <ProtectedRoute allowedRoles={['PATIENT']}>
              <DashboardLayout />
            </ProtectedRoute>
          }>
            <Route path="/patient/dashboard" element={<PatientDashboard />} />
            <Route path="/patient/appointments" element={<PlaceholderPage title="My Appointments" description="View and manage your appointments — coming in Phase 3." />} />
            <Route path="/patient/records" element={<PlaceholderPage title="Medical Records" description="Access your electronic medical records — coming in Phase 5." />} />
            <Route path="/patient/notifications" element={<PlaceholderPage title="Notifications" description="In-app notifications center — coming in Phase 4." />} />
            <Route path="/patient/settings" element={<PlaceholderPage title="Settings" description="Profile and account settings." />} />
          </Route>

          {/* ===== Provider Routes ===== */}
          <Route element={
            <ProtectedRoute allowedRoles={['PROVIDER']}>
              <DashboardLayout />
            </ProtectedRoute>
          }>
            <Route path="/provider/dashboard" element={<ProviderDashboard />} />
            <Route path="/provider/availability" element={<PlaceholderPage title="Manage Availability" description="Configure your availability slots — coming in Phase 2." />} />
            <Route path="/provider/appointments" element={<PlaceholderPage title="Appointments" description="View and manage patient appointments — coming in Phase 3." />} />
            <Route path="/provider/records" element={<PlaceholderPage title="Medical Records" description="Create and manage patient records — coming in Phase 5." />} />
            <Route path="/provider/earnings" element={<PlaceholderPage title="Earnings" description="Revenue analytics and payment history — coming in Phase 4." />} />
            <Route path="/provider/reviews" element={<PlaceholderPage title="Reviews" description="Patient reviews and ratings — coming in Phase 4." />} />
            <Route path="/provider/settings" element={<PlaceholderPage title="Settings" description="Profile and account settings." />} />
          </Route>

          {/* ===== Admin Routes ===== */}
          <Route element={
            <ProtectedRoute allowedRoles={['ADMIN']}>
              <DashboardLayout />
            </ProtectedRoute>
          }>
            <Route path="/admin/dashboard" element={<AdminDashboard />} />
            <Route path="/admin/users" element={<PlaceholderPage title="User Management" description="Manage all platform users — coming in Phase 5." />} />
            <Route path="/admin/providers" element={<PlaceholderPage title="Provider Verification" description="Review and verify provider credentials — coming in Phase 5." />} />
            <Route path="/admin/appointments" element={<PlaceholderPage title="All Appointments" description="Platform-wide appointment monitoring — coming in Phase 5." />} />
            <Route path="/admin/payments" element={<PlaceholderPage title="Payments" description="Transaction management — coming in Phase 5." />} />
            <Route path="/admin/reviews" element={<PlaceholderPage title="Review Moderation" description="Moderate patient reviews — coming in Phase 5." />} />
            <Route path="/admin/analytics" element={<PlaceholderPage title="Platform Analytics" description="Platform-wide analytics dashboard — coming in Phase 5." />} />
            <Route path="/admin/settings" element={<PlaceholderPage title="Settings" description="Admin settings." />} />
          </Route>

          {/* 404 */}
          <Route path="*" element={
            <MainLayout>
              <PlaceholderPage title="Page Not Found" description="The page you're looking for doesn't exist." />
            </MainLayout>
          } />
        </Routes>
      </Router>

      {/* Toast Notifications */}
      <Toaster
        position="top-right"
        toastOptions={{
          duration: 3000,
          style: {
            background: '#1e293b',
            color: '#f1f5f9',
            border: '1px solid #334155',
            fontSize: '14px',
          },
          success: { iconTheme: { primary: '#14b8a6', secondary: '#f1f5f9' } },
          error: { iconTheme: { primary: '#ef4444', secondary: '#f1f5f9' } },
        }}
      />
    </AuthProvider>
  );
}
