import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import toast from 'react-hot-toast';
import { UserPlus } from 'lucide-react';

export default function RegisterPage() {
  const [form, setForm] = useState({
    name: '',
    email: '',
    phone: '',
    password: '',
    role: 'PATIENT',
    // Provider-specific fields
    specialization: '',
    experienceYears: '',
    clinicName: '',
    clinicAddress: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const { register } = useAuth();
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
      const payload = {
        name: form.name,
        email: form.email,
        phone: form.phone,
        password: form.password,
        role: form.role,
      };

      if (form.role === 'PROVIDER') {
        payload.specialization = form.specialization;
        payload.experienceYears = parseInt(form.experienceYears) || 0;
        payload.clinicName = form.clinicName;
        payload.clinicAddress = form.clinicAddress;
      }

      const user = await register(payload);

      if (user.role === 'PROVIDER') {
        toast.success('Registered! Your profile is pending admin verification.');
        navigate('/provider/dashboard');
      } else {
        toast.success('Registration successful!');
        navigate('/providers');
      }
    } catch (err) {
      setError(err.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  const inputClass =
    'w-full px-3.5 py-2.5 text-sm border border-gray-300 rounded-lg bg-white text-gray-900 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/10 transition-all placeholder:text-gray-400';

  return (
    <div className="min-h-[calc(100vh-64px)] flex items-center justify-center px-5 py-10">
      <div
        className={`w-full bg-white border border-gray-200 rounded-xl p-8 shadow-sm ${
          form.role === 'PROVIDER' ? 'max-w-lg' : 'max-w-md'
        }`}
      >
        <div className="text-center mb-8">
          <div className="w-12 h-12 mx-auto mb-4 bg-blue-50 rounded-xl flex items-center justify-center">
            <UserPlus className="w-6 h-6 text-blue-600" />
          </div>
          <h1 className="text-2xl font-bold text-gray-900">Create Account</h1>
          <p className="text-sm text-gray-500 mt-1">Join MediBook as a patient or provider</p>
        </div>

        {error && (
          <div className="mb-5 px-4 py-3 text-sm text-red-700 bg-red-50 border border-red-200 rounded-lg">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="mb-5">
            <label htmlFor="role" className="block text-sm font-medium text-gray-700 mb-1.5">
              I am a
            </label>
            <select
              id="role"
              name="role"
              className={`${inputClass} appearance-none bg-[url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%23495057' d='M6 8L1 3h10z'/%3E%3C/svg%3E")] bg-no-repeat bg-[right_12px_center] pr-9`}
              value={form.role}
              onChange={handleChange}
            >
              <option value="PATIENT">Patient</option>
              <option value="PROVIDER">Healthcare Provider (Doctor)</option>
            </select>
          </div>

          <div className="mb-5">
            <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-1.5">
              Full Name
            </label>
            <input
              id="name"
              type="text"
              name="name"
              className={inputClass}
              placeholder="Dr. John Smith"
              value={form.name}
              onChange={handleChange}
              required
            />
          </div>

          <div className="mb-5">
            <label htmlFor="reg-email" className="block text-sm font-medium text-gray-700 mb-1.5">
              Email
            </label>
            <input
              id="reg-email"
              type="email"
              name="email"
              className={inputClass}
              placeholder="you@example.com"
              value={form.email}
              onChange={handleChange}
              required
            />
          </div>

          <div className="mb-5">
            <label htmlFor="reg-phone" className="block text-sm font-medium text-gray-700 mb-1.5">
              Phone Number
            </label>
            <input
              id="reg-phone"
              type="tel"
              name="phone"
              className={inputClass}
              placeholder="+919876543210"
              value={form.phone}
              onChange={handleChange}
              required
            />
          </div>

          <div className="mb-5">
            <label htmlFor="reg-password" className="block text-sm font-medium text-gray-700 mb-1.5">
              Password
            </label>
            <input
              id="reg-password"
              type="password"
              name="password"
              className={inputClass}
              placeholder="Min 6 characters"
              value={form.password}
              onChange={handleChange}
              required
              minLength={6}
            />
          </div>

          {form.role === 'PROVIDER' && (
            <>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-5">
                <div>
                  <label htmlFor="specialization" className="block text-sm font-medium text-gray-700 mb-1.5">
                    Specialization
                  </label>
                  <input
                    id="specialization"
                    type="text"
                    name="specialization"
                    className={inputClass}
                    placeholder="e.g. Cardiology"
                    value={form.specialization}
                    onChange={handleChange}
                    required
                  />
                </div>
                <div>
                  <label htmlFor="experienceYears" className="block text-sm font-medium text-gray-700 mb-1.5">
                    Experience (years)
                  </label>
                  <input
                    id="experienceYears"
                    type="number"
                    name="experienceYears"
                    className={inputClass}
                    placeholder="e.g. 5"
                    value={form.experienceYears}
                    onChange={handleChange}
                    required
                    min={0}
                  />
                </div>
              </div>

              <div className="mb-5">
                <label htmlFor="clinicName" className="block text-sm font-medium text-gray-700 mb-1.5">
                  Clinic Name
                </label>
                <input
                  id="clinicName"
                  type="text"
                  name="clinicName"
                  className={inputClass}
                  placeholder="e.g. City Heart Clinic"
                  value={form.clinicName}
                  onChange={handleChange}
                  required
                />
              </div>

              <div className="mb-5">
                <label htmlFor="clinicAddress" className="block text-sm font-medium text-gray-700 mb-1.5">
                  Clinic Address
                </label>
                <input
                  id="clinicAddress"
                  type="text"
                  name="clinicAddress"
                  className={inputClass}
                  placeholder="e.g. 123 Medical Lane, Mumbai"
                  value={form.clinicAddress}
                  onChange={handleChange}
                  required
                />
              </div>
            </>
          )}

          <button
            type="submit"
            className="w-full py-2.5 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer mt-2"
            disabled={loading}
          >
            {loading ? 'Creating account...' : 'Create Account'}
          </button>
        </form>

        <div className="text-center mt-6 text-sm text-gray-500">
          Already have an account?{' '}
          <Link to="/login" className="text-blue-600 hover:text-blue-700 font-medium">
            Sign in
          </Link>
        </div>
      </div>
    </div>
  );
}
