import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/**
 * Protects routes that require authentication.
 * Redirects to /login if not authenticated.
 * Optionally restricts by role.
 */
export default function ProtectedRoute({ children, allowedRoles }) {
  const { isAuthenticated, isLoading, user } = useAuth();

  if (isLoading) {
    return (
      <div className="min-h-screen bg-slate-900 flex items-center justify-center">
        <div className="w-10 h-10 border-3 border-primary-500/30 border-t-primary-500 rounded-full animate-spin"></div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user?.role)) {
    // Redirect to their own dashboard
    const dashMap = { PATIENT: '/patient/dashboard', PROVIDER: '/provider/dashboard', ADMIN: '/admin/dashboard' };
    return <Navigate to={dashMap[user?.role] || '/'} replace />;
  }

  return children;
}
