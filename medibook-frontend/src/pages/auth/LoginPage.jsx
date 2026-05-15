import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import toast from 'react-hot-toast';
import { LogIn, Activity } from 'lucide-react';

export default function LoginPage() {
  const [form, setForm] = useState({ email: '', password: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const user = await login(form);
      toast.success(`Welcome back, ${user.name}!`);

      const dashboardMap = {
        PATIENT: '/providers',
        PROVIDER: '/provider/dashboard',
        ADMIN: '/admin/dashboard',
      };
      navigate(dashboardMap[user.role] || '/');
    } catch (err) {
      setError(err.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[calc(100vh-80px)] flex items-center justify-center px-5 py-12 relative overflow-hidden bg-slate-50">
      {/* Decorative Background Elements */}
      <div className="absolute top-0 inset-x-0 h-full bg-gradient-to-br from-blue-100/40 via-transparent to-indigo-100/40 pointer-events-none" />
      <div className="absolute -top-32 -left-32 w-96 h-96 bg-blue-300/30 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute bottom-0 -right-32 w-96 h-96 bg-indigo-300/20 rounded-full blur-3xl pointer-events-none" />

      <div className="w-full max-w-md bg-white/80 backdrop-blur-xl border border-white/50 rounded-3xl p-10 shadow-2xl shadow-slate-200/50 relative z-10">
        <div className="text-center mb-8">
          <div className="w-16 h-16 mx-auto mb-6 bg-gradient-to-br from-blue-50 to-indigo-50 rounded-2xl flex items-center justify-center shadow-inner border border-white">
            <LogIn className="w-8 h-8 text-blue-600" />
          </div>
          <h1 className="text-3xl font-extrabold text-slate-900 tracking-tight">Welcome Back</h1>
          <p className="text-sm font-medium text-slate-500 mt-2">Sign in to your MediBook account</p>
        </div>

        {error && (
          <div className="mb-6 px-4 py-3 text-sm text-red-700 bg-red-50/80 backdrop-blur-sm border border-red-200 rounded-xl flex items-center gap-2">
            <Activity className="w-4 h-4 text-red-500 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label htmlFor="email" className="block text-sm font-bold text-slate-700 mb-2">
              Email Address
            </label>
            <input
              id="email"
              type="email"
              name="email"
              className="w-full px-4 py-3 text-sm border-2 border-slate-200 rounded-xl bg-white/50 text-slate-900 outline-none focus:border-blue-500 focus:bg-white transition-all placeholder:text-slate-400 shadow-sm"
              placeholder="you@example.com"
              value={form.email}
              onChange={handleChange}
              required
            />
          </div>

          <div>
            <div className="flex items-center justify-between mb-2">
              <label htmlFor="password" className="block text-sm font-bold text-slate-700">
                Password
              </label>
              <Link to="/forgot-password" className="text-xs text-blue-600 hover:text-blue-700 font-medium hover:underline">
                Forgot password?
              </Link>
            </div>
            <input
              id="password"
              type="password"
              name="password"
              className="w-full px-4 py-3 text-sm border-2 border-slate-200 rounded-xl bg-white/50 text-slate-900 outline-none focus:border-blue-500 focus:bg-white transition-all placeholder:text-slate-400 shadow-sm"
              placeholder="Enter your password"
              value={form.password}
              onChange={handleChange}
              required
            />
          </div>

          <button
            type="submit"
            className="w-full py-3.5 text-sm font-bold text-white bg-gradient-to-r from-blue-600 to-indigo-600 rounded-xl hover:shadow-lg hover:shadow-blue-500/30 hover:-translate-y-0.5 transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer mt-2"
            disabled={loading}
          >
            {loading ? 'Signing in...' : 'Sign In'}
          </button>
        </form>

        {/* Divider */}
        <div className="flex items-center gap-3 my-6">
          <div className="flex-1 h-px bg-slate-200"></div>
          <span className="text-xs font-medium text-slate-400 uppercase">or</span>
          <div className="flex-1 h-px bg-slate-200"></div>
        </div>

        {/* Google OAuth Button */}
        <a
          href="http://localhost:8080/oauth2/authorization/google"
          className="w-full py-3 text-sm font-semibold text-slate-700 bg-white border-2 border-slate-200 rounded-xl hover:bg-slate-50 hover:border-slate-300 transition-all flex items-center justify-center gap-3 shadow-sm"
        >
          <svg className="w-5 h-5" viewBox="0 0 24 24">
            <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 0 1-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z" fill="#4285F4"/>
            <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
            <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
            <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
          </svg>
          Continue with Google
        </a>

        <div className="text-center mt-6 text-sm font-medium text-slate-500">
          Don't have an account?{' '}
          <Link to="/register" className="text-blue-600 hover:text-blue-700 font-bold hover:underline underline-offset-4 transition-all">
            Register here
          </Link>
        </div>
      </div>
    </div>
  );
}
