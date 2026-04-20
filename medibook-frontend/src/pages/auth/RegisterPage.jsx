import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import toast from 'react-hot-toast';
import { HiUser, HiMail, HiLockClosed, HiPhone, HiArrowRight } from 'react-icons/hi';

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    fullName: '', email: '', password: '', confirmPassword: '', phone: '', role: 'PATIENT',
  });
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.fullName || !form.email || !form.password) {
      toast.error('Please fill in all required fields');
      return;
    }
    if (form.password !== form.confirmPassword) {
      toast.error('Passwords do not match');
      return;
    }
    if (form.password.length < 6) {
      toast.error('Password must be at least 6 characters');
      return;
    }

    setLoading(true);
    try {
      const { confirmPassword, ...data } = form;
      const user = await register(data);
      toast.success(`Welcome, ${user.fullName}!`);
      switch (user.role) {
        case 'PATIENT': navigate('/patient/dashboard'); break;
        case 'PROVIDER': navigate('/provider/dashboard'); break;
        default: navigate('/');
      }
    } catch (err) {
      toast.error(err.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  const inputClass = "w-full pl-10 pr-4 py-2.5 bg-slate-900/50 border border-slate-600 rounded-xl text-white placeholder-gray-500 text-sm focus:outline-none focus:border-primary-500 focus:ring-1 focus:ring-primary-500/50 transition-all";

  return (
    <div className="min-h-[calc(100vh-4rem)] flex items-center justify-center px-4 py-12">
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-1/3 right-1/4 w-72 h-72 bg-secondary-500/10 rounded-full blur-3xl"></div>
        <div className="absolute bottom-1/3 left-1/4 w-72 h-72 bg-primary-500/10 rounded-full blur-3xl"></div>
      </div>

      <div className="relative w-full max-w-md animate-fade-in">
        <div className="text-center mb-8">
          <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-primary-500 to-secondary-500 flex items-center justify-center text-white font-bold text-2xl mx-auto mb-4 shadow-xl">M</div>
          <h1 className="text-2xl font-bold text-white">Create your account</h1>
          <p className="text-gray-400 mt-1 text-sm">Join MediBook for smarter healthcare</p>
        </div>

        <div className="bg-slate-800/60 border border-slate-700/50 rounded-2xl p-8 shadow-2xl backdrop-blur-sm">
          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Role Selector */}
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-2">I am a</label>
              <div className="grid grid-cols-2 gap-3">
                {['PATIENT', 'PROVIDER'].map((role) => (
                  <button
                    key={role}
                    type="button"
                    onClick={() => setForm({ ...form, role })}
                    className={`py-2.5 rounded-xl text-sm font-medium transition-all border ${
                      form.role === role
                        ? 'bg-primary-500/20 border-primary-500/50 text-primary-300'
                        : 'bg-slate-900/50 border-slate-600 text-gray-400 hover:border-slate-500'
                    }`}
                  >
                    {role === 'PATIENT' ? '🏥 Patient' : '👨‍⚕️ Provider'}
                  </button>
                ))}
              </div>
            </div>

            {/* Full Name */}
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-1.5">Full Name *</label>
              <div className="relative">
                <HiUser className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" />
                <input type="text" value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} placeholder="Dr. Jane Smith" className={inputClass} />
              </div>
            </div>

            {/* Email */}
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-1.5">Email *</label>
              <div className="relative">
                <HiMail className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" />
                <input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} placeholder="jane@example.com" className={inputClass} />
              </div>
            </div>

            {/* Phone */}
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-1.5">Phone</label>
              <div className="relative">
                <HiPhone className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" />
                <input type="tel" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} placeholder="+91 98765 43210" className={inputClass} />
              </div>
            </div>

            {/* Password */}
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-1.5">Password *</label>
                <div className="relative">
                  <HiLockClosed className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" />
                  <input type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} placeholder="••••••" className={inputClass} />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-1.5">Confirm *</label>
                <div className="relative">
                  <HiLockClosed className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" />
                  <input type="password" value={form.confirmPassword} onChange={(e) => setForm({ ...form, confirmPassword: e.target.value })} placeholder="••••••" className={inputClass} />
                </div>
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-3 bg-gradient-to-r from-primary-600 to-secondary-600 text-white font-semibold rounded-xl hover:from-primary-500 hover:to-secondary-500 transition-all shadow-lg hover:shadow-primary-500/25 disabled:opacity-50 flex items-center justify-center gap-2 text-sm"
            >
              {loading ? (
                <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
              ) : (
                <>Create Account <HiArrowRight /></>
              )}
            </button>
          </form>
        </div>

        <p className="text-center mt-6 text-sm text-gray-400">
          Already have an account?{' '}
          <Link to="/login" className="text-primary-400 hover:text-primary-300 font-medium transition-colors">Sign in</Link>
        </p>
      </div>
    </div>
  );
}
