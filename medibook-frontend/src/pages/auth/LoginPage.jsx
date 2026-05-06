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
            <label htmlFor="password" className="block text-sm font-bold text-slate-700 mb-2">
              Password
            </label>
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

        <div className="text-center mt-8 text-sm font-medium text-slate-500">
          Don't have an account?{' '}
          <Link to="/register" className="text-blue-600 hover:text-blue-700 font-bold hover:underline underline-offset-4 transition-all">
            Register here
          </Link>
        </div>
      </div>
    </div>
  );
}
