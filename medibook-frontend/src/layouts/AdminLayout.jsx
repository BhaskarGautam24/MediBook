import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  LayoutDashboard,
  Users,
  Building2,
  Calendar,
  FileText,
  LogOut,
  Stethoscope,
} from 'lucide-react';

const navItems = [
  { to: '/admin/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/admin/users', icon: Users, label: 'Users' },
  { to: '/admin/providers', icon: Building2, label: 'Providers' },
  { to: '/admin/appointments', icon: Calendar, label: 'Appointments' },
  { to: '/admin/records', icon: FileText, label: 'Records' },
];

export default function AdminLayout() {
  const { logout, user } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="flex min-h-screen bg-slate-50 font-sans">
      {/* Premium Sidebar */}
      <aside className="w-64 bg-slate-900 text-slate-200 flex flex-col fixed top-0 left-0 bottom-0 z-50 shadow-2xl">
        {/* Brand Area */}
        <div className="h-20 flex items-center px-6 border-b border-slate-800 bg-slate-900/50 backdrop-blur-md">
          <div className="flex items-center gap-3 text-xl font-black tracking-tight text-white">
            <div className="bg-gradient-to-br from-sky-400 to-blue-600 p-1.5 rounded-xl shadow-lg shadow-sky-500/20">
              <Stethoscope className="w-6 h-6 text-white" />
            </div>
            <span>MediBook <span className="text-sky-400 font-medium text-xs align-top ml-1 tracking-widest uppercase">Admin</span></span>
          </div>
        </div>

        {/* User Info Snippet */}
        <div className="px-6 py-5 border-b border-slate-800/60 bg-slate-800/20">
          <div className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1">Logged in as</div>
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-full bg-gradient-to-tr from-sky-400 to-blue-500 flex items-center justify-center shadow-md border border-slate-700">
              <span className="text-white font-bold text-sm">
                {user?.name?.charAt(0).toUpperCase() || 'A'}
              </span>
            </div>
            <div className="text-sm font-bold text-white truncate">{user?.name || 'Admin User'}</div>
          </div>
        </div>

        <nav className="flex flex-col py-6 px-4 flex-1 gap-1">
          <div className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2 px-2">Management</div>
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                `group flex items-center gap-3 px-4 py-3 text-sm font-semibold rounded-xl transition-all duration-200 ${
                  isActive
                    ? 'bg-sky-500/10 text-sky-400 shadow-sm shadow-sky-900/20'
                    : 'text-slate-400 hover:bg-slate-800 hover:text-slate-100'
                }`
              }
            >
              {({ isActive }) => (
                <>
                  <div className={`p-1.5 rounded-lg transition-colors ${isActive ? 'bg-sky-500/20 text-sky-400' : 'bg-transparent text-slate-500 group-hover:text-slate-300 group-hover:bg-slate-700'}`}>
                    <item.icon className="w-4 h-4" />
                  </div>
                  {item.label}
                </>
              )}
            </NavLink>
          ))}

          <div className="mt-auto pt-4">
            <button
              onClick={handleLogout}
              className="flex items-center gap-3 px-4 py-3 text-sm font-semibold text-slate-400 rounded-xl hover:bg-red-500/10 hover:text-red-400 transition-all w-full text-left"
            >
              <div className="p-1.5 rounded-lg bg-transparent text-slate-500 group-hover:text-red-400 transition-colors">
                <LogOut className="w-4 h-4" />
              </div>
              Sign Out
            </button>
          </div>
        </nav>
      </aside>

      {/* Main Content */}
      <div className="flex-1 ml-64 flex flex-col relative min-h-screen">
        {/* Subtle Background Effect */}
        <div className="absolute top-0 inset-x-0 h-64 bg-gradient-to-b from-sky-50/50 to-transparent pointer-events-none" />
        
        <header className="h-20 bg-white/70 backdrop-blur-xl shadow-[0_1px_2px_0_rgba(0,0,0,0.03)] flex items-center px-8 sticky top-0 z-40 border-b border-slate-200/60">
          <h2 className="text-xl font-bold text-slate-800 tracking-tight">Admin Control Panel</h2>
        </header>
        <main className="p-8 flex-1 relative z-10 max-w-7xl mx-auto w-full">
          <div className="animate-in fade-in slide-in-from-bottom-4 duration-500">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
}
