import { useState, useEffect } from 'react';
import api from '../../services/api';
import { DollarSign, TrendingUp, Clock, CheckCircle2, RefreshCcw, ArrowDown, IndianRupee } from 'lucide-react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts';

const COLORS = ['#22c55e', '#eab308', '#8b5cf6', '#ef4444'];

export default function ProviderEarnings() {
  const [earnings, setEarnings] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadEarnings();
  }, []);

  const loadEarnings = async () => {
    try {
      const data = await api.get('/payments/earnings');
      setEarnings(data);
    } catch (err) {
      console.error('Failed to load earnings:', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-16">
        <div className="spinner" />
      </div>
    );
  }

  if (!earnings) {
    return (
      <div className="text-center py-16">
        <IndianRupee className="w-12 h-12 mx-auto text-gray-300 mb-4" />
        <h3 className="text-base font-semibold text-gray-600 mb-1">No earnings data</h3>
        <p className="text-sm text-gray-400">Complete appointments to start earning</p>
      </div>
    );
  }

  // Stats cards data
  const statCards = [
    {
      label: 'Total Earnings',
      value: `₹${(earnings.totalEarnings || 0).toLocaleString()}`,
      icon: IndianRupee,
      color: 'green',
      desc: 'PAID minus REFUNDED',
    },
    {
      label: 'Monthly Earnings',
      value: `₹${(earnings.monthlyEarnings || 0).toLocaleString()}`,
      icon: TrendingUp,
      color: 'blue',
      desc: 'Current month',
    },
    {
      label: 'Today\'s Earnings',
      value: `₹${(earnings.dailyEarnings || 0).toLocaleString()}`,
      icon: DollarSign,
      color: 'indigo',
      desc: 'Today',
    },
    {
      label: 'Pending Payments',
      value: earnings.pendingPayments || 0,
      icon: Clock,
      color: 'amber',
      desc: `₹${(earnings.pendingAmount || 0).toLocaleString()} pending`,
    },
    {
      label: 'Completed Payments',
      value: earnings.completedPayments || 0,
      icon: CheckCircle2,
      color: 'emerald',
      desc: 'Successfully paid',
    },
    {
      label: 'Refunded',
      value: earnings.refundedPayments || 0,
      icon: RefreshCcw,
      color: 'purple',
      desc: `₹${(earnings.refundedAmount || 0).toLocaleString()} refunded`,
    },
  ];

  const colorMap = {
    green: { bg: 'bg-green-50', icon: 'text-green-600', border: 'border-green-100' },
    blue: { bg: 'bg-blue-50', icon: 'text-blue-600', border: 'border-blue-100' },
    indigo: { bg: 'bg-indigo-50', icon: 'text-indigo-600', border: 'border-indigo-100' },
    amber: { bg: 'bg-amber-50', icon: 'text-amber-600', border: 'border-amber-100' },
    emerald: { bg: 'bg-emerald-50', icon: 'text-emerald-600', border: 'border-emerald-100' },
    purple: { bg: 'bg-purple-50', icon: 'text-purple-600', border: 'border-purple-100' },
  };

  // Pie chart data
  const pieData = [
    { name: 'Paid', value: earnings.completedPayments || 0 },
    { name: 'Pending', value: earnings.pendingPayments || 0 },
    { name: 'Refunded', value: earnings.refundedPayments || 0 },
  ].filter(d => d.value > 0);

  // Use daily breakdown from backend (properly aggregated by date, refunds subtracted)
  const recentForChart = (earnings.dailyBreakdown || []).map((d) => ({
    name: new Date(d.date + 'T00:00:00').toLocaleDateString('en-IN', { day: '2-digit', month: 'short' }),
    amount: d.amount || 0,
  }));

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900 mb-1">Earnings Dashboard</h1>
        <p className="text-sm text-gray-500">Track your consultation income and payment history</p>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4 mb-8">
        {statCards.map((card) => {
          const Icon = card.icon;
          const colors = colorMap[card.color];
          return (
            <div key={card.label} className={`bg-white border ${colors.border} rounded-xl p-4 shadow-sm`}>
              <div className="flex items-center justify-between mb-3">
                <div className={`p-2 ${colors.bg} rounded-lg`}>
                  <Icon className={`w-4 h-4 ${colors.icon}`} />
                </div>
              </div>
              <div className="text-xl font-bold text-gray-900 mb-0.5">{card.value}</div>
              <div className="text-[10px] font-semibold uppercase tracking-wider text-gray-400">{card.label}</div>
              <div className="text-[10px] text-gray-400 mt-1">{card.desc}</div>
            </div>
          );
        })}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
        {/* Recent Earnings Bar Chart */}
        <div className="lg:col-span-2 bg-white border border-gray-200 rounded-xl p-6 shadow-sm">
          <h3 className="text-lg font-bold text-gray-900 mb-1">Recent Earnings</h3>
          <p className="text-sm text-gray-500 mb-4">Based on your last completed payments</p>
          {recentForChart.length > 0 ? (
            <div className="h-64 w-full">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={recentForChart}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e5e7eb" />
                  <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fill: '#6b7280', fontSize: 11 }} dy={10} />
                  <YAxis axisLine={false} tickLine={false} tick={{ fill: '#6b7280', fontSize: 11 }} tickFormatter={(v) => `₹${v}`} dx={-10} />
                  <Tooltip
                    cursor={{ fill: '#f3f4f6' }}
                    contentStyle={{ borderRadius: '8px', border: '1px solid #e5e7eb', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }}
                    formatter={(value) => [`₹${value}`, 'Earnings']}
                  />
                  <Bar dataKey="amount" fill="#3b82f6" radius={[4, 4, 0, 0]} barSize={36} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          ) : (
            <div className="h-64 flex items-center justify-center text-gray-400 text-sm">
              No payment data to display yet
            </div>
          )}
        </div>

        {/* Payment Distribution Pie */}
        <div className="bg-white border border-gray-200 rounded-xl p-6 shadow-sm">
          <h3 className="text-lg font-bold text-gray-900 mb-1">Payment Distribution</h3>
          <p className="text-sm text-gray-500 mb-4">By status</p>
          {pieData.length > 0 ? (
            <div className="h-52 w-full">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie data={pieData} cx="50%" cy="50%" innerRadius={50} outerRadius={80} paddingAngle={4} dataKey="value">
                    {pieData.map((_, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip formatter={(value, name) => [value, name]} />
                </PieChart>
              </ResponsiveContainer>
            </div>
          ) : (
            <div className="h-52 flex items-center justify-center text-gray-400 text-sm">No data</div>
          )}
          <div className="flex flex-wrap gap-3 mt-2 justify-center">
            {pieData.map((d, i) => (
              <div key={d.name} className="flex items-center gap-1.5 text-xs text-gray-600">
                <span className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: COLORS[i] }} />
                {d.name} ({d.value})
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Recent Payments Table */}
      <div className="bg-white border border-gray-200 rounded-xl shadow-sm overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-100">
          <h3 className="text-lg font-bold text-gray-900">Recent Transactions</h3>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-gray-50 border-b border-gray-200">
                <th className="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-gray-500">#</th>
                <th className="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-gray-500">Amount</th>
                <th className="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-gray-500">Mode</th>
                <th className="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-gray-500">Status</th>
                <th className="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-gray-500">Transaction ID</th>
                <th className="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-gray-500">Date</th>
              </tr>
            </thead>
            <tbody>
              {(earnings.recentPayments || []).length === 0 ? (
                <tr><td colSpan="6" className="text-center py-8 text-gray-400">No transactions yet</td></tr>
              ) : (
                earnings.recentPayments.map((p, i) => (
                  <tr key={p.id || i} className="border-b border-gray-100 hover:bg-gray-50/50 transition-colors">
                    <td className="px-4 py-3 text-gray-500">{p.id}</td>
                    <td className="px-4 py-3 font-semibold text-gray-900">₹{p.amount}</td>
                    <td className="px-4 py-3 text-gray-600">{p.mode}</td>
                    <td className="px-4 py-3">
                      <span className={`inline-flex px-2 py-0.5 text-[10px] font-semibold rounded-full ${
                        p.status === 'PAID' ? 'bg-green-50 text-green-700' :
                        p.status === 'PENDING' ? 'bg-yellow-50 text-yellow-700' :
                        p.status === 'REFUNDED' ? 'bg-purple-50 text-purple-700' :
                        'bg-red-50 text-red-700'
                      }`}>
                        {p.status}
                      </span>
                    </td>
                    <td className="px-4 py-3 font-mono text-xs text-gray-400">{p.transactionId || '—'}</td>
                    <td className="px-4 py-3 text-gray-500 text-xs">
                      {p.paidAt ? new Date(p.paidAt).toLocaleString() : p.createdAt ? new Date(p.createdAt).toLocaleString() : '—'}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
