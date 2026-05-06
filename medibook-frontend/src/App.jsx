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

// Patient Pages
import ProviderList from './pages/patient/ProviderList';
import ProviderSlots from './pages/patient/ProviderSlots';
import MyAppointments from './pages/patient/MyAppointments';

// Provider Pages
import ProviderDashboard from './pages/provider/ProviderDashboard';
import ManageSlots from './pages/provider/ManageSlots';
import ProviderAppointments from './pages/provider/ProviderAppointments';
import ProviderEarnings from './pages/provider/ProviderEarnings';

// Shared
import Meet from './pages/Meet';

// Admin Pages
import AdminLayout from './layouts/AdminLayout';
import AdminDashboard from './pages/admin/AdminDashboard';
import ManageProviders from './pages/admin/ManageProviders';
import ManageUsers from './pages/admin/ManageUsers';
import AdminAppointments from './pages/admin/AdminAppointments';
import AdminRecords from './pages/admin/AdminRecords';

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
            <Route path="/providers" element={<ProviderList />} />
            <Route path="/providers/:providerId/slots" element={<ProviderSlots />} />
          </Route>

          {/* ===== Patient Routes ===== */}
          <Route element={
            <ProtectedRoute allowedRoles={['PATIENT']}>
              <DashboardLayout />
            </ProtectedRoute>
          }>
            <Route path="/patient/appointments" element={<MyAppointments />} />
          </Route>

          {/* ===== Provider Routes ===== */}
          <Route element={
            <ProtectedRoute allowedRoles={['PROVIDER']}>
              <DashboardLayout />
            </ProtectedRoute>
          }>
            <Route path="/provider/dashboard" element={<ProviderDashboard />} />
            <Route path="/provider/slots" element={<ManageSlots />} />
            <Route path="/provider/appointments" element={<ProviderAppointments />} />
            <Route path="/provider/earnings" element={<ProviderEarnings />} />
          </Route>

          {/* ===== Admin Routes ===== */}
          <Route element={
            <ProtectedRoute allowedRoles={['ADMIN']}>
              <AdminLayout />
            </ProtectedRoute>
          }>
            <Route path="/admin/dashboard" element={<AdminDashboard />} />
            <Route path="/admin/providers" element={<ManageProviders />} />
            <Route path="/admin/users" element={<ManageUsers />} />
            <Route path="/admin/appointments" element={<AdminAppointments />} />
            <Route path="/admin/records" element={<AdminRecords />} />
          </Route>

          {/* ===== Video Consultation Route ===== */}
          <Route path="/meet/:id" element={
            <ProtectedRoute allowedRoles={['PATIENT', 'PROVIDER']}>
              <Meet />
            </ProtectedRoute>
          } />

          {/* 404 */}
          <Route path="*" element={
            <MainLayout>
              <div className="min-h-[calc(100vh-64px)] flex items-center justify-center px-5">
                <div className="bg-white border border-gray-200 rounded-xl p-16 text-center shadow-sm">
                  <h1 className="text-5xl font-bold text-gray-900 mb-2">404</h1>
                  <p className="text-gray-500">Page not found</p>
                </div>
              </div>
            </MainLayout>
          } />
        </Routes>
      </Router>

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
          success: { iconTheme: { primary: '#16a34a', secondary: '#f1f5f9' } },
          error: { iconTheme: { primary: '#dc2626', secondary: '#f1f5f9' } },
        }}
      />
    </AuthProvider>
  );
}
