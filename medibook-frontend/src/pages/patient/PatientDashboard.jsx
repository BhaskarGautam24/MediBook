import { HiCalendar, HiClock, HiDocumentText, HiStar } from 'react-icons/hi';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export default function PatientDashboard() {
  const { user } = useAuth();

  const stats = [
    { label: 'Upcoming', value: '3', icon: HiCalendar, color: 'from-blue-500 to-blue-600' },
    { label: 'Completed', value: '12', icon: HiClock, color: 'from-green-500 to-green-600' },
    { label: 'Records', value: '8', icon: HiDocumentText, color: 'from-purple-500 to-purple-600' },
    { label: 'Reviews', value: '5', icon: HiStar, color: 'from-amber-500 to-amber-600' },
  ];

  return (
    <div className="space-y-6">
      {/* Welcome */}
      <div className="bg-gradient-to-r from-primary-600/20 to-secondary-600/20 border border-primary-500/20 rounded-2xl p-6">
        <h2 className="text-xl font-bold text-white">Welcome back, {user?.fullName?.split(' ')[0]}! 👋</h2>
        <p className="text-gray-400 mt-1 text-sm">Here's your health dashboard overview.</p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {stats.map((s, i) => (
          <div key={i} className="bg-slate-800/50 border border-slate-700/50 rounded-xl p-5 hover:border-primary-500/30 transition-all">
            <div className={`w-10 h-10 rounded-lg bg-gradient-to-br ${s.color} flex items-center justify-center text-white mb-3`}>
              <s.icon className="text-lg" />
            </div>
            <p className="text-2xl font-bold text-white">{s.value}</p>
            <p className="text-xs text-gray-400 mt-0.5">{s.label}</p>
          </div>
        ))}
      </div>

      {/* Quick Actions */}
      <div className="bg-slate-800/50 border border-slate-700/50 rounded-2xl p-6">
        <h3 className="text-lg font-semibold text-white mb-4">Quick Actions</h3>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
          <Link to="/providers" className="flex items-center gap-3 p-4 bg-slate-900/50 rounded-xl border border-slate-700 hover:border-primary-500/30 transition-all group">
            <div className="w-10 h-10 rounded-lg bg-primary-500/20 flex items-center justify-center text-primary-400 group-hover:bg-primary-500/30 transition-colors">🔍</div>
            <div>
              <p className="text-sm font-medium text-white">Find a Doctor</p>
              <p className="text-xs text-gray-500">Search providers</p>
            </div>
          </Link>
          <Link to="/patient/appointments" className="flex items-center gap-3 p-4 bg-slate-900/50 rounded-xl border border-slate-700 hover:border-primary-500/30 transition-all group">
            <div className="w-10 h-10 rounded-lg bg-secondary-500/20 flex items-center justify-center text-secondary-400 group-hover:bg-secondary-500/30 transition-colors">📅</div>
            <div>
              <p className="text-sm font-medium text-white">My Appointments</p>
              <p className="text-xs text-gray-500">View schedule</p>
            </div>
          </Link>
          <Link to="/patient/records" className="flex items-center gap-3 p-4 bg-slate-900/50 rounded-xl border border-slate-700 hover:border-primary-500/30 transition-all group">
            <div className="w-10 h-10 rounded-lg bg-purple-500/20 flex items-center justify-center text-purple-400 group-hover:bg-purple-500/30 transition-colors">📋</div>
            <div>
              <p className="text-sm font-medium text-white">Medical Records</p>
              <p className="text-xs text-gray-500">View history</p>
            </div>
          </Link>
        </div>
      </div>
    </div>
  );
}
