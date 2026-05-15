import { useEffect, useState } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { CheckCircle, XCircle, Loader } from 'lucide-react';
import api from '../../services/api';

/**
 * VerifyEmailPage — Handles email verification when user clicks the link.
 * URL: /verify-email?token=xxxxx
 */
export default function VerifyEmailPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');

  const [status, setStatus] = useState('loading'); // loading | success | error
  const [message, setMessage] = useState('');

  useEffect(() => {
    if (!token) {
      setStatus('error');
      setMessage('Invalid verification link.');
      return;
    }

    const verify = async () => {
      try {
        const res = await api.get(`/auth/verify-email?token=${token}`);
        setStatus('success');
        setMessage(res.message || 'Email verified successfully!');
      } catch (err) {
        setStatus('error');
        setMessage(err.message || 'Verification failed.');
      }
    };

    verify();
  }, [token]);

  return (
    <div className="min-h-[calc(100vh-64px)] flex items-center justify-center px-5">
      <div className="bg-white border border-gray-200 rounded-xl p-10 max-w-md w-full shadow-sm text-center">
        {status === 'loading' && (
          <>
            <Loader className="w-16 h-16 text-blue-500 mx-auto mb-4 animate-spin" />
            <h2 className="text-xl font-bold text-gray-900">Verifying your email...</h2>
          </>
        )}

        {status === 'success' && (
          <>
            <CheckCircle className="w-16 h-16 text-green-500 mx-auto mb-4" />
            <h2 className="text-2xl font-bold text-gray-900 mb-2">Email Verified!</h2>
            <p className="text-gray-600 mb-6">{message}</p>
            <Link
              to="/login"
              className="inline-block bg-blue-600 text-white px-6 py-2.5 rounded-lg hover:bg-blue-700 text-sm font-medium transition-colors"
            >
              Continue to Login
            </Link>
          </>
        )}

        {status === 'error' && (
          <>
            <XCircle className="w-16 h-16 text-red-500 mx-auto mb-4" />
            <h2 className="text-2xl font-bold text-gray-900 mb-2">Verification Failed</h2>
            <p className="text-gray-600 mb-6">{message}</p>
            <Link to="/login" className="text-blue-600 hover:underline text-sm font-medium">
              Go to Login
            </Link>
          </>
        )}
      </div>
    </div>
  );
}
