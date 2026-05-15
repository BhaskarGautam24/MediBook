import { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import toast from 'react-hot-toast';

/**
 * OAuthCallback — Landing page after Google OAuth redirect.
 * Reads token params from URL, stores them, and redirects to dashboard.
 */
export default function OAuthCallback() {
  const [searchParams] = useSearchParams();
  const { loginWithOAuthData } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    const token = searchParams.get('token');
    const refreshToken = searchParams.get('refreshToken');
    const userId = searchParams.get('userId');
    const name = searchParams.get('name');
    const email = searchParams.get('email');
    const role = searchParams.get('role');
    const profilePicture = searchParams.get('profilePicture');
    const error = searchParams.get('error');

    if (error) {
      toast.error(decodeURIComponent(error));
      navigate('/login');
      return;
    }

    if (!token || !userId) {
      toast.error('OAuth login failed');
      navigate('/login');
      return;
    }

    // Store auth data and redirect
    const user = loginWithOAuthData({
      token,
      refreshToken,
      userId,
      name: decodeURIComponent(name || ''),
      email: decodeURIComponent(email || ''),
      role: decodeURIComponent(role || 'PATIENT'),
      profilePicture: decodeURIComponent(profilePicture || ''),
    });

    toast.success(`Welcome, ${user.name}!`);

    // Redirect based on role
    switch (user.role) {
      case 'PROVIDER':
        navigate('/provider/dashboard');
        break;
      case 'ADMIN':
        navigate('/admin/dashboard');
        break;
      default:
        navigate('/providers');
    }
  }, [searchParams, loginWithOAuthData, navigate]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="text-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
        <p className="text-gray-600">Completing login...</p>
      </div>
    </div>
  );
}
