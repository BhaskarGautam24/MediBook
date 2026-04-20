import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import toast from 'react-hot-toast';
import { HiMail, HiLockClosed, HiArrowRight } from 'react-icons/hi';
import { FcGoogle } from 'react-icons/fc';
import { FaGithub } from 'react-icons/fa';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: '', password: '' });
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.email || !form.password) {
      toast.error('Please fill in all fields');
      return;
    }
    setLoading(true);
    try {
      const user = await login(form);
      toast.success(`Welcome back, ${user.fullName}!`);
      // Redirect based on role
      switch (user.role) {
        case 'PATIENT': navigate('/patient/dashboard'); break;
        case 'PROVIDER': navigate('/provider/dashboard'); break;
        case 'ADMIN': navigate('/admin/dashboard'); break;
        default: navigate('/');
      }
    } catch (err) {
      toast.error(err.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[calc(100vh-4rem)] flex items-center justify-center px-4 py-12">
      {/* Background Effects */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-1/4 left-1/4 w-72 h-72 bg-primary-500/10 rounded-full blur-3xl"></div>
        <div className="absolute bottom-1/4 right-1/4 w-72 h-72 bg-secondary-500/10 rounded-full blur-3xl"></div>
      </div>

      <div className="relative w-full max-w-md animate-fade-in">
        {/* Header */}
        <div className="text-center mb-8">
          <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-primary-500 to-secondary-500 flex items-center justify-center text-white font-bold text-2xl mx-auto mb-4 shadow-xl">
            M
          </div>
          <h1 className="text-2xl font-bold text-white">Welcome back</h1>
          <p className="text-gray-400 mt-1 text-sm">Sign in to your MediBook account</p>
        </div>

        {/* Form Card */}
        <div className="bg-slate-800/60 border border-slate-700/50 rounded-2xl p-8 shadow-2xl backdrop-blur-sm">
          <form onSubmit={handleSubmit} className="space-y-5">
            {/* Email */}
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-1.5">Email</label>
              <div className="relative">
                <HiMail className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" />
                <input
                  type="email"
                  value={form.email}
                  onChange={(e) => setForm({ ...form, email: e.target.value })}
                  placeholder="you@example.com"
                  className="w-full pl-10 pr-4 py-2.5 bg-slate-900/50 border border-slate-600 rounded-xl text-white placeholder-gray-500 text-sm focus:outline-none focus:border-primary-500 focus:ring-1 focus:ring-primary-500/50 transition-all"
                />
              </div>
            </div>

            {/* Password */}
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-1.5">Password</label>
              <div className="relative">
                <HiLockClosed className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" />
                <input
                  type="password"
                  value={form.password}
                  onChange={(e) => setForm({ ...form, password: e.target.value })}
                  placeholder="••••••••"
                  className="w-full pl-10 pr-4 py-2.5 bg-slate-900/50 border border-slate-600 rounded-xl text-white placeholder-gray-500 text-sm focus:outline-none focus:border-primary-500 focus:ring-1 focus:ring-primary-500/50 transition-all"
                />
              </div>
            </div>

            {/* Submit */}
            <button
              type="submit"
              disabled={loading}
              className="w-full py-3 bg-gradient-to-r from-primary-600 to-secondary-600 text-white font-semibold rounded-xl hover:from-primary-500 hover:to-secondary-500 transition-all shadow-lg hover:shadow-primary-500/25 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 text-sm"
            >
              {loading ? (
                <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
              ) : (
                <>Sign In <HiArrowRight /></>
              )}
            </button>
          </form>

          {/* Divider */}
          <div className="flex items-center gap-3 my-6">
            <div className="flex-1 h-px bg-slate-700"></div>
            <span className="text-xs text-gray-500">or continue with</span>
            <div className="flex-1 h-px bg-slate-700"></div>
          </div>

          {/* OAuth */}
          <div className="grid grid-cols-2 gap-3">
            <button className="flex items-center justify-center gap-2 py-2.5 bg-slate-900/50 border border-slate-600 rounded-xl text-gray-300 text-sm hover:bg-slate-700 transition-all">
              <FcGoogle className="text-lg" /> Google
            </button>
            <button className="flex items-center justify-center gap-2 py-2.5 bg-slate-900/50 border border-slate-600 rounded-xl text-gray-300 text-sm hover:bg-slate-700 transition-all">
              <FaGithub className="text-lg" /> GitHub
            </button>
          </div>
        </div>

        {/* Register Link */}
        <p className="text-center mt-6 text-sm text-gray-400">
          Don't have an account?{' '}
          <Link to="/register" className="text-primary-400 hover:text-primary-300 font-medium transition-colors">
            Create one
          </Link>
        </p>
      </div>
    </div>
  );
}
