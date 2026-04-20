import { Outlet, Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  HiHome, HiCalendar, HiClipboardList, HiCreditCard,
  HiStar, HiBell, HiDocumentText, HiUsers,
  HiChartBar, HiCog, HiShieldCheck, HiClock
} from 'react-icons/hi';

/** Sidebar nav items per role */
const navItems = {
  PATIENT: [
    { path: '/patient/dashboard', label: 'Dashboard', icon: HiHome },
    { path: '/patient/appointments', label: 'My Appointments', icon: HiCalendar },
    { path: '/patient/records', label: 'Medical Records', icon: HiDocumentText },
    { path: '/patient/notifications', label: 'Notifications', icon: HiBell },
  ],
  PROVIDER: [
    { path: '/provider/dashboard', label: 'Dashboard', icon: HiHome },
    { path: '/provider/availability', label: 'Availability', icon: HiClock },
    { path: '/provider/appointments', label: 'Appointments', icon: HiCalendar },
    { path: '/provider/records', label: 'Medical Records', icon: HiDocumentText },
    { path: '/provider/earnings', label: 'Earnings', icon: HiCreditCard },
    { path: '/provider/reviews', label: 'Reviews', icon: HiStar },
  ],
  ADMIN: [
    { path: '/admin/dashboard', label: 'Dashboard', icon: HiHome },
    { path: '/admin/users', label: 'Users', icon: HiUsers },
    { path: '/admin/providers', label: 'Providers', icon: HiShieldCheck },
    { path: '/admin/appointments', label: 'Appointments', icon: HiCalendar },
    { path: '/admin/payments', label: 'Payments', icon: HiCreditCard },
    { path: '/admin/reviews', label: 'Reviews', icon: HiStar },
    { path: '/admin/analytics', label: 'Analytics', icon: HiChartBar },
  ],
};

export default function DashboardLayout() {
  const { user } = useAuth();
  const location = useLocation();
  const items = navItems[user?.role] || [];

  return (
    <div className="min-h-screen bg-slate-900 flex">
      {/* Sidebar */}
      <aside className="hidden lg:flex flex-col w-64 bg-slate-800/50 border-r border-slate-700/50">
        {/* Logo */}
        <div className="h-16 flex items-center px-6 border-b border-slate-700/50">
          <Link to="/" className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-primary-500 to-secondary-500 flex items-center justify-center text-white font-bold text-sm shadow">M</div>
            <span className="text-lg font-bold text-white">Medi<span className="text-secondary-400">Book</span></span>
          </Link>
        </div>

        {/* Role Badge */}
        <div className="px-6 py-4">
          <div className="px-3 py-1.5 bg-primary-500/10 rounded-lg border border-primary-500/20">
            <p className="text-xs text-primary-300 font-medium">{user?.role} Dashboard</p>
            <p className="text-sm text-white font-semibold truncate">{user?.fullName}</p>
          </div>
        </div>

        {/* Nav Items */}
        <nav className="flex-1 px-3 space-y-1">
          {items.map((item) => {
            const isActive = location.pathname === item.path;
            return (
              <Link
                key={item.path}
                to={item.path}
                className={`flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-200 ${
                  isActive
                    ? 'bg-primary-500/20 text-primary-300 shadow-sm'
                    : 'text-gray-400 hover:bg-slate-700/50 hover:text-white'
                }`}
              >
                <item.icon className={`text-lg ${isActive ? 'text-primary-400' : ''}`} />
                {item.label}
              </Link>
            );
          })}
        </nav>

        {/* Settings */}
        <div className="px-3 pb-4">
          <Link
            to={`/${user?.role?.toLowerCase()}/settings`}
            className="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm text-gray-400 hover:bg-slate-700/50 hover:text-white transition-all"
          >
            <HiCog className="text-lg" /> Settings
          </Link>
        </div>
      </aside>

      {/* Main Content */}
      <div className="flex-1 flex flex-col">
        {/* Top Bar */}
        <header className="h-16 border-b border-slate-700/50 flex items-center justify-between px-6">
          <h1 className="text-lg font-semibold text-white">
            {items.find(i => i.path === location.pathname)?.label || 'Dashboard'}
          </h1>
          <div className="flex items-center gap-4">
            <Link to={`/${user?.role?.toLowerCase()}/notifications`} className="relative text-gray-400 hover:text-white transition-colors">
              <HiBell className="text-xl" />
              <span className="absolute -top-1 -right-1 w-4 h-4 bg-red-500 rounded-full text-[10px] text-white flex items-center justify-center">3</span>
            </Link>
            <div className="w-8 h-8 rounded-full bg-gradient-to-br from-primary-400 to-secondary-400 flex items-center justify-center text-white text-sm font-semibold">
              {user?.fullName?.charAt(0) || 'U'}
            </div>
          </div>
        </header>

        {/* Page Content */}
        <main className="flex-1 p-6 overflow-auto">
          <div className="animate-fade-in">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
}
