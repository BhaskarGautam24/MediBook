import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import NotificationBell from './NotificationBell';
import { Stethoscope, LogOut, ChevronRight } from 'lucide-react';

export default function Navbar() {
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  const getDashboardLink = () => {
    if (!user) return '/';
    const map = {
      PATIENT: '/patient/appointments',
      PROVIDER: '/provider/dashboard',
      ADMIN: '/admin/dashboard',
    };
    return map[user.role] || '/';
  };

  return (
    <nav className="flex items-center justify-between px-6 md:px-12 h-20 bg-white/70 backdrop-blur-xl border-b border-white/20 shadow-sm sticky top-0 z-50">
      <Link to="/" className="flex items-center gap-2 text-2xl font-black tracking-tight text-slate-900 hover:opacity-80 transition-opacity">
        <div className="bg-gradient-to-br from-blue-600 to-indigo-600 p-1.5 rounded-xl shadow-md shadow-blue-500/20">
          <Stethoscope className="w-6 h-6 text-white" />
        </div>
        <span>Medi<span className="text-transparent bg-clip-text bg-gradient-to-r from-blue-600 to-indigo-600">Book</span></span>
      </Link>

      <div className="flex items-center gap-2 md:gap-4">
        {!isAuthenticated ? (
          <>
            <Link
              to="/providers"
              className="hidden md:block px-4 py-2 text-sm font-semibold text-slate-600 rounded-xl hover:bg-slate-100 hover:text-slate-900 transition-all"
            >
              Find Providers
            </Link>
            <div className="w-px h-6 bg-slate-200 hidden md:block mx-2"></div>
            <Link
              to="/login"
              className="px-5 py-2.5 text-sm font-semibold text-slate-700 rounded-xl hover:bg-slate-100 transition-all"
            >
              Login
            </Link>
            <Link
              to="/register"
              className="group flex items-center gap-1 px-5 py-2.5 text-sm font-semibold text-white bg-slate-900 rounded-xl hover:bg-slate-800 shadow-md shadow-slate-900/10 hover:shadow-lg transition-all"
            >
              Register
              <ChevronRight className="w-4 h-4 group-hover:translate-x-0.5 transition-transform" />
            </Link>
          </>
        ) : (
          <>
            <Link
              to="/providers"
              className="hidden md:block px-4 py-2 text-sm font-semibold text-slate-600 rounded-xl hover:bg-slate-100 hover:text-slate-900 transition-all"
            >
              Find Providers
            </Link>
            <Link
              to={getDashboardLink()}
              className="px-4 py-2 text-sm font-semibold text-slate-600 rounded-xl hover:bg-slate-100 hover:text-slate-900 transition-all"
            >
              Dashboard
            </Link>
            <div className="w-px h-6 bg-slate-200 mx-1"></div>
            <NotificationBell />
            <div className="flex items-center gap-3 ml-2 pl-2 border-l border-slate-200">
              <div className="hidden sm:flex flex-col items-end">
                <span className="text-sm font-bold text-slate-900 leading-none">{user?.name}</span>
                <span className="text-[10px] font-bold uppercase tracking-widest text-blue-600 mt-1">
                  {user?.role}
                </span>
              </div>
              <div className="w-10 h-10 rounded-full bg-gradient-to-tr from-blue-100 to-indigo-100 flex items-center justify-center border-2 border-white shadow-sm">
                <span className="text-blue-700 font-bold text-sm">
                  {user?.name?.charAt(0).toUpperCase()}
                </span>
              </div>
            </div>
            <button
              onClick={handleLogout}
              className="ml-2 flex items-center justify-center p-2.5 text-slate-400 rounded-xl hover:bg-red-50 hover:text-red-600 transition-all"
              title="Logout"
            >
              <LogOut className="w-5 h-5" />
            </button>
          </>
        )}
      </div>
    </nav>
  );
}
