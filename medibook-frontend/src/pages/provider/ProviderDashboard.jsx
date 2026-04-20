import { HiCalendar, HiClock, HiCurrencyRupee, HiStar } from 'react-icons/hi';
import { useAuth } from '../../context/AuthContext';

export default function ProviderDashboard() {
  const { user } = useAuth();

  const stats = [
    { label: "Today's Appointments", value: '5', icon: HiCalendar, color: 'from-blue-500 to-blue-600' },
    { label: 'Upcoming', value: '18', icon: HiClock, color: 'from-teal-500 to-teal-600' },
    { label: 'Monthly Revenue', value: '₹45K', icon: HiCurrencyRupee, color: 'from-green-500 to-green-600' },
    { label: 'Avg Rating', value: '4.8', icon: HiStar, color: 'from-amber-500 to-amber-600' },
  ];

  return (
    <div className="space-y-6">
      <div className="bg-gradient-to-r from-secondary-600/20 to-primary-600/20 border border-secondary-500/20 rounded-2xl p-6">
        <h2 className="text-xl font-bold text-white">Good morning, Dr. {user?.fullName?.split(' ').pop()}! 👨‍⚕️</h2>
        <p className="text-gray-400 mt-1 text-sm">You have 5 appointments scheduled today.</p>
      </div>

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {stats.map((s, i) => (
          <div key={i} className="bg-slate-800/50 border border-slate-700/50 rounded-xl p-5 hover:border-secondary-500/30 transition-all">
            <div className={`w-10 h-10 rounded-lg bg-gradient-to-br ${s.color} flex items-center justify-center text-white mb-3`}>
              <s.icon className="text-lg" />
            </div>
            <p className="text-2xl font-bold text-white">{s.value}</p>
            <p className="text-xs text-gray-400 mt-0.5">{s.label}</p>
          </div>
        ))}
      </div>

      <div className="bg-slate-800/50 border border-slate-700/50 rounded-2xl p-6">
        <h3 className="text-lg font-semibold text-white mb-4">Today's Schedule</h3>
        <p className="text-sm text-gray-400">No appointments data yet. Connect the appointment-service to see live data.</p>
      </div>
    </div>
  );
}
