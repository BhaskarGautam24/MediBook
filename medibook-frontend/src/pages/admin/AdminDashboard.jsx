import { HiUsers, HiShieldCheck, HiCalendar, HiCurrencyRupee } from 'react-icons/hi';

export default function AdminDashboard() {
  const stats = [
    { label: 'Total Users', value: '1,240', icon: HiUsers, color: 'from-blue-500 to-blue-600' },
    { label: 'Verified Providers', value: '86', icon: HiShieldCheck, color: 'from-green-500 to-green-600' },
    { label: 'Total Bookings', value: '4,320', icon: HiCalendar, color: 'from-purple-500 to-purple-600' },
    { label: 'Platform Revenue', value: '₹12.5L', icon: HiCurrencyRupee, color: 'from-amber-500 to-amber-600' },
  ];

  return (
    <div className="space-y-6">
      <div className="bg-gradient-to-r from-purple-600/20 to-blue-600/20 border border-purple-500/20 rounded-2xl p-6">
        <h2 className="text-xl font-bold text-white">Admin Dashboard 🛡️</h2>
        <p className="text-gray-400 mt-1 text-sm">Platform overview and management controls.</p>
      </div>

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {stats.map((s, i) => (
          <div key={i} className="bg-slate-800/50 border border-slate-700/50 rounded-xl p-5 hover:border-purple-500/30 transition-all">
            <div className={`w-10 h-10 rounded-lg bg-gradient-to-br ${s.color} flex items-center justify-center text-white mb-3`}>
              <s.icon className="text-lg" />
            </div>
            <p className="text-2xl font-bold text-white">{s.value}</p>
            <p className="text-xs text-gray-400 mt-0.5">{s.label}</p>
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <div className="bg-slate-800/50 border border-slate-700/50 rounded-2xl p-6">
          <h3 className="text-lg font-semibold text-white mb-4">Pending Verifications</h3>
          <p className="text-sm text-gray-400">Provider verification data will appear here.</p>
        </div>
        <div className="bg-slate-800/50 border border-slate-700/50 rounded-2xl p-6">
          <h3 className="text-lg font-semibold text-white mb-4">Recent Activity</h3>
          <p className="text-sm text-gray-400">Platform activity feed will appear here.</p>
        </div>
      </div>
    </div>
  );
}
