import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../services/api';
import toast from 'react-hot-toast';
import { useAuth } from '../../context/AuthContext';
import { AlertCircle, CheckCircle2, Clock, CalendarCheck, BarChart3, IndianRupee, ArrowRight, Pencil, Save } from 'lucide-react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

export default function ProviderDashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [stats, setStats] = useState({
    totalSlots: 0,
    bookedSlots: 0,
    totalAppointments: 0,
    completedAppointments: 0,
    providerStatus: 'PENDING',
    consultationFee: 500,
  });
  const [earnings, setEarnings] = useState(null);
  const [loading, setLoading] = useState(true);

  // Fee editing state
  const [isEditingFee, setIsEditingFee] = useState(false);
  const [feeInput, setFeeInput] = useState('');
  const [savingFee, setSavingFee] = useState(false);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [statsData, earningsData] = await Promise.all([
        api.get('/providers/me/stats'),
        api.get('/payments/earnings').catch(() => null),
      ]);
      setStats(statsData);
      setEarnings(earningsData);
    } catch (err) {
      console.error('Failed to load data:', err);
    } finally {
      setLoading(false);
    }
  };

  // Save consultation fee
  const handleSaveFee = async () => {
    const fee = parseFloat(feeInput);
    if (isNaN(fee) || fee < 0) {
      toast.error('Please enter a valid fee amount');
      return;
    }
    setSavingFee(true);
    try {
      await api.put('/providers/me/fee', { consultationFee: fee });
      setStats(prev => ({ ...prev, consultationFee: fee }));
      setIsEditingFee(false);
      toast.success(`Consultation fee updated to ₹${fee}`);
    } catch (err) {
      toast.error(err.message || 'Failed to update fee');
    } finally {
      setSavingFee(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-16">
        <div className="spinner" />
      </div>
    );
  }

  const statusConfig = {
    APPROVED: { class: 'bg-green-50 text-green-700', icon: CheckCircle2 },
    PENDING: { class: 'bg-amber-50 text-amber-700', icon: Clock },
    REJECTED: { class: 'bg-red-50 text-red-700', icon: AlertCircle },
  };

  const currentStatus = statusConfig[stats.providerStatus] || statusConfig.PENDING;
  const StatusIcon = currentStatus.icon;

  const statCards = [
    { label: 'Total Slots', value: stats.totalSlots, icon: Clock },
    { label: 'Booked Slots', value: stats.bookedSlots, icon: CalendarCheck },
    { label: 'Total Appointments', value: stats.totalAppointments, icon: BarChart3 },
    { label: 'Completed', value: stats.completedAppointments, icon: CheckCircle2 },
  ];

  // Use daily breakdown from backend (properly aggregated by date, refunds subtracted)
  const earningsChartData = earnings?.dailyBreakdown
    ? earnings.dailyBreakdown.map((d) => ({
        name: new Date(d.date + 'T00:00:00').toLocaleDateString('en-IN', { day: '2-digit', month: 'short' }),
        earnings: d.amount || 0,
      }))
    : [];

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900 mb-1">Provider Dashboard</h1>
        <p className="text-sm text-gray-500">Welcome back, {user?.name}</p>
      </div>

      {stats.providerStatus !== 'APPROVED' && (
        <div className={`flex items-center gap-3 px-4 py-3 rounded-lg text-sm mb-6 ${
          stats.providerStatus === 'PENDING'
            ? 'bg-blue-50 text-blue-700 border border-blue-200'
            : 'bg-red-50 text-red-700 border border-red-200'
        }`}>
          <StatusIcon className="w-5 h-5 shrink-0" />
          {stats.providerStatus === 'PENDING'
            ? 'Your profile is pending admin verification. You cannot add slots or receive bookings until approved.'
            : 'Your profile has been rejected by the admin. Please contact support.'}
        </div>
      )}

      {/* Status + Stats Cards */}
      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-4 mb-6">
        <div className="bg-white border border-gray-200 rounded-xl p-5 shadow-sm">
          <div className="text-xs font-semibold uppercase tracking-wider text-gray-400 mb-2">Status</div>
          <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 text-xs font-semibold rounded-full uppercase ${currentStatus.class}`}>
            <StatusIcon className="w-3.5 h-3.5" />
            {stats.providerStatus}
          </span>
        </div>
        {statCards.map((card) => (
          <div key={card.label} className="bg-white border border-gray-200 rounded-xl p-5 shadow-sm">
            <div className="text-xs font-semibold uppercase tracking-wider text-gray-400 mb-2">{card.label}</div>
            <div className="text-2xl font-bold text-gray-900">{card.value}</div>
          </div>
        ))}
      </div>

      {/* Consultation Fee Card */}
      <div className="bg-white border border-gray-200 rounded-xl p-5 shadow-sm mb-6">
        <div className="flex items-center justify-between">
          <div>
            <div className="text-xs font-semibold uppercase tracking-wider text-gray-400 mb-1">Consultation Fee</div>
            {isEditingFee ? (
              <div className="flex items-center gap-2">
                <span className="text-lg font-bold text-gray-600">₹</span>
                <input
                  type="number"
                  value={feeInput}
                  onChange={(e) => setFeeInput(e.target.value)}
                  className="w-32 px-3 py-1.5 text-lg font-bold border border-blue-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500"
                  min="0"
                  autoFocus
                  onKeyDown={(e) => e.key === 'Enter' && handleSaveFee()}
                />
                <button
                  onClick={handleSaveFee}
                  disabled={savingFee}
                  className="inline-flex items-center gap-1 px-3 py-1.5 text-sm font-medium text-white bg-green-600 rounded-lg hover:bg-green-700 transition-colors cursor-pointer disabled:opacity-50"
                >
                  <Save className="w-3.5 h-3.5" /> Save
                </button>
                <button
                  onClick={() => setIsEditingFee(false)}
                  className="px-3 py-1.5 text-sm text-gray-500 hover:text-gray-700 transition-colors cursor-pointer"
                >
                  Cancel
                </button>
              </div>
            ) : (
              <div className="flex items-center gap-3">
                <span className="text-2xl font-bold text-green-700">₹{stats.consultationFee}</span>
                <button
                  onClick={() => { setFeeInput(String(stats.consultationFee)); setIsEditingFee(true); }}
                  className="inline-flex items-center gap-1 px-2.5 py-1 text-xs font-medium text-blue-600 bg-blue-50 rounded-lg hover:bg-blue-100 border border-blue-200 transition-colors cursor-pointer"
                >
                  <Pencil className="w-3 h-3" /> Edit
                </button>
              </div>
            )}
          </div>
          <div className="p-3 bg-green-50 rounded-lg">
            <IndianRupee className="w-6 h-6 text-green-600" />
          </div>
        </div>
        <p className="text-xs text-gray-400 mt-2">This amount will be charged to patients booking your appointments</p>
      </div>

      {/* Earnings Summary Cards */}
      {earnings && (
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
          <div className="bg-gradient-to-br from-green-50 to-emerald-50 border border-green-200 rounded-xl p-5">
            <div className="text-xs font-semibold uppercase tracking-wider text-green-600 mb-1">Total Earnings</div>
            <div className="text-2xl font-bold text-green-700">₹{(earnings.totalEarnings || 0).toLocaleString()}</div>
          </div>
          <div className="bg-gradient-to-br from-blue-50 to-indigo-50 border border-blue-200 rounded-xl p-5">
            <div className="text-xs font-semibold uppercase tracking-wider text-blue-600 mb-1">This Month</div>
            <div className="text-2xl font-bold text-blue-700">₹{(earnings.monthlyEarnings || 0).toLocaleString()}</div>
          </div>
          <div className="bg-gradient-to-br from-amber-50 to-yellow-50 border border-amber-200 rounded-xl p-5">
            <div className="text-xs font-semibold uppercase tracking-wider text-amber-600 mb-1">Pending</div>
            <div className="text-2xl font-bold text-amber-700">{earnings.pendingPayments || 0}</div>
          </div>
          <div className="bg-gradient-to-br from-purple-50 to-violet-50 border border-purple-200 rounded-xl p-5">
            <div className="text-xs font-semibold uppercase tracking-wider text-purple-600 mb-1">Completed</div>
            <div className="text-2xl font-bold text-purple-700">{earnings.completedPayments || 0}</div>
          </div>
        </div>
      )}

      {/* Earnings Chart */}
      <div className="bg-white border border-gray-200 rounded-xl p-6 shadow-sm">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h3 className="text-lg font-bold text-gray-900">Earnings Overview</h3>
            <p className="text-sm text-gray-500">Revenue from completed appointments</p>
          </div>
          <button
            onClick={() => navigate('/provider/earnings')}
            className="flex items-center gap-1 text-sm text-blue-600 hover:text-blue-700 font-medium cursor-pointer"
          >
            View Details <ArrowRight className="w-4 h-4" />
          </button>
        </div>
        <div className="h-72 w-full">
          {earningsChartData.length > 0 ? (
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={earningsChartData}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e5e7eb" />
                <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fill: '#6b7280', fontSize: 12 }} dy={10} />
                <YAxis axisLine={false} tickLine={false} tick={{ fill: '#6b7280', fontSize: 12 }} tickFormatter={(value) => `₹${value}`} dx={-10} />
                <Tooltip
                  cursor={{ fill: '#f3f4f6' }}
                  contentStyle={{ borderRadius: '8px', border: '1px solid #e5e7eb', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }}
                  formatter={(value) => [`₹${value}`, 'Earnings']}
                />
                <Bar dataKey="earnings" fill="#3b82f6" radius={[4, 4, 0, 0]} barSize={40} />
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <div className="h-full flex items-center justify-center text-gray-400 text-sm">
              <div className="text-center">
                <IndianRupee className="w-10 h-10 mx-auto mb-2 text-gray-300" />
                <p>No earnings data yet. Complete appointments to see your earnings here.</p>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
