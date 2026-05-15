import { Outlet, NavLink } from 'react-router-dom';
import Navbar from '../components/Navbar';
import { useAuth } from '../context/AuthContext';
import {
  Search,
  ClipboardList,
  LayoutDashboard,
  Clock,
  Calendar,
  Users,
  Building2,
  IndianRupee,
  Star,
  Settings,
  UserCog,
} from 'lucide-react';

const iconMap = {
  '🏥 Find Providers': Search,
  '📋 My Appointments': ClipboardList,
  '📊 Dashboard': LayoutDashboard,
  '🕐 Manage Slots': Clock,
  '📋 Appointments': Calendar,
  '🏥 Providers': Building2,
  '👥 Users': Users,
  '💰 Earnings': IndianRupee,
  '⭐ Reviews': Star,
  '⚙️ Settings': Settings,
  '👤 Edit Profile': UserCog,
};

const sidebarConfig = {
  PATIENT: {
    label: 'Patient Menu',
    links: [
      { to: '/providers', label: '🏥 Find Providers', text: 'Find Providers' },
      { to: '/patient/appointments', label: '📋 My Appointments', text: 'My Appointments' },
      { to: '/patient/settings', label: '⚙️ Settings', text: 'Settings' },
    ],
  },
  PROVIDER: {
    label: 'Provider Menu',
    links: [
      { to: '/provider/dashboard', label: '📊 Dashboard', text: 'Dashboard' },
      { to: '/provider/slots', label: '🕐 Manage Slots', text: 'Manage Slots' },
      { to: '/provider/appointments', label: '📋 Appointments', text: 'Appointments' },
      { to: '/provider/earnings', label: '💰 Earnings', text: 'Earnings' },
      { to: '/provider/reviews', label: '⭐ Reviews', text: 'Reviews' },
      { to: '/provider/profile', label: '👤 Edit Profile', text: 'Edit Profile' },
      { to: '/provider/settings', label: '⚙️ Settings', text: 'Settings' },
    ],
  },
  ADMIN: {
    label: 'Admin Menu',
    links: [
      { to: '/admin/dashboard', label: '📊 Dashboard', text: 'Dashboard' },
      { to: '/admin/providers', label: '🏥 Providers', text: 'Providers' },
      { to: '/admin/users', label: '👥 Users', text: 'Users' },
      { to: '/admin/appointments', label: '📋 Appointments', text: 'Appointments' },
    ],
  },
};

export default function DashboardLayout() {
  const { user } = useAuth();
  const config = sidebarConfig[user?.role] || { label: 'Menu', links: [] };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col font-sans">
      <Navbar />
      <div className="flex flex-1 overflow-hidden relative">
        {/* Subtle Dashboard Background Effect */}
        <div className="absolute top-0 inset-x-0 h-64 bg-gradient-to-b from-blue-100/30 to-transparent pointer-events-none" />
        
        <aside className="w-64 bg-white/80 backdrop-blur-xl border-r border-slate-200/60 py-6 shrink-0 h-[calc(100vh-80px)] overflow-y-auto hidden md:block shadow-[4px_0_24px_-12px_rgba(0,0,0,0.1)] relative z-10">
          <div className="mb-6 px-4">
            <div className="px-4 py-2 text-[10px] font-bold uppercase tracking-widest text-slate-400 mb-2">
              {config.label}
            </div>
            <div className="space-y-1">
              {config.links.map((link) => {
                const Icon = iconMap[link.label] || ClipboardList;
                return (
                  <NavLink
                    key={link.to}
                    to={link.to}
                    className={({ isActive }) =>
                      `group flex items-center gap-3 px-4 py-3 text-sm font-semibold rounded-xl transition-all duration-200 ${
                        isActive
                          ? 'text-blue-700 bg-blue-50 shadow-sm shadow-blue-100'
                          : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900'
                      }`
                    }
                  >
                    {({ isActive }) => (
                      <>
                        <div className={`p-1.5 rounded-lg transition-colors ${isActive ? 'bg-white shadow-sm text-blue-600' : 'bg-transparent text-slate-400 group-hover:text-slate-600 group-hover:bg-slate-100'}`}>
                          <Icon className="w-4 h-4" />
                        </div>
                        {link.text}
                      </>
                    )}
                  </NavLink>
                );
              })}
            </div>
          </div>
        </aside>
        
        <main className="flex-1 overflow-y-auto h-[calc(100vh-80px)] relative z-0">
          <div className="p-6 md:p-10 max-w-7xl mx-auto min-h-full">
            <div className="animate-in fade-in slide-in-from-bottom-4 duration-500">
              <Outlet />
            </div>
          </div>
        </main>
      </div>
    </div>
  );
}
