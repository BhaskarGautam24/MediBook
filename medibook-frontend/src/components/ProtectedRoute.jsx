import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function ProtectedRoute({ allowedRoles, children }) {
  const { isAuthenticated, isLoading, user } = useAuth();

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-16">
        <div className="spinner" />
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user?.role)) {
    const dashboardMap = {
      PATIENT: '/patient/providers',
      PROVIDER: '/provider/dashboard',
      ADMIN: '/admin/dashboard',
    };
    return <Navigate to={dashboardMap[user?.role] || '/'} replace />;
  }

  return children;
}
