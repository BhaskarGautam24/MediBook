import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useState } from 'react';
import { HiMenu, HiX, HiUserCircle, HiLogout, HiCog } from 'react-icons/hi';

export default function Navbar() {
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [profileOpen, setProfileOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const getDashboardPath = () => {
    if (!user) return '/';
    switch (user.role) {
      case 'PATIENT': return '/patient/dashboard';
      case 'PROVIDER': return '/provider/dashboard';
      case 'ADMIN': return '/admin/dashboard';
      default: return '/';
    }
  };

  return (
    <nav className="sticky top-0 z-50 glass-dark shadow-lg">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16 items-center">
          {/* Logo */}
          <Link to="/" className="flex items-center gap-2 group">
            <div className="w-9 h-9 rounded-lg bg-gradient-to-br from-primary-500 to-secondary-500 flex items-center justify-center text-white font-bold text-lg shadow-lg group-hover:shadow-primary-500/50 transition-shadow">
              M
            </div>
            <span className="text-xl font-bold text-white tracking-tight">
              Medi<span className="text-secondary-400">Book</span>
            </span>
          </Link>

          {/* Desktop Nav */}
          <div className="hidden md:flex items-center gap-6">
            <Link to="/providers" className="text-gray-300 hover:text-white transition-colors text-sm font-medium">
              Find Doctors
            </Link>

            {isAuthenticated ? (
              <div className="relative">
                <button
                  onClick={() => setProfileOpen(!profileOpen)}
                  className="flex items-center gap-2 text-gray-300 hover:text-white transition-colors"
                >
                  <div className="w-8 h-8 rounded-full bg-gradient-to-br from-primary-400 to-secondary-400 flex items-center justify-center text-white text-sm font-semibold">
                    {user?.fullName?.charAt(0) || 'U'}
                  </div>
                  <span className="text-sm font-medium">{user?.fullName?.split(' ')[0]}</span>
                </button>

                {profileOpen && (
                  <div className="absolute right-0 mt-2 w-56 bg-slate-800 rounded-xl shadow-2xl border border-slate-700 py-2 animate-fade-in">
                    <div className="px-4 py-2 border-b border-slate-700">
                      <p className="text-sm font-semibold text-white">{user?.fullName}</p>
                      <p className="text-xs text-gray-400">{user?.email}</p>
                      <span className="inline-block mt-1 px-2 py-0.5 bg-primary-500/20 text-primary-300 text-xs rounded-full">
                        {user?.role}
                      </span>
                    </div>
                    <Link
                      to={getDashboardPath()}
                      onClick={() => setProfileOpen(false)}
                      className="flex items-center gap-2 px-4 py-2 text-sm text-gray-300 hover:bg-slate-700 hover:text-white transition-colors"
                    >
                      <HiUserCircle className="text-lg" /> Dashboard
                    </Link>
                    <Link
                      to={`/${user?.role?.toLowerCase()}/settings`}
                      onClick={() => setProfileOpen(false)}
                      className="flex items-center gap-2 px-4 py-2 text-sm text-gray-300 hover:bg-slate-700 hover:text-white transition-colors"
                    >
                      <HiCog className="text-lg" /> Settings
                    </Link>
                    <button
                      onClick={handleLogout}
                      className="flex items-center gap-2 w-full px-4 py-2 text-sm text-red-400 hover:bg-slate-700 hover:text-red-300 transition-colors"
                    >
                      <HiLogout className="text-lg" /> Sign Out
                    </button>
                  </div>
                )}
              </div>
            ) : (
              <div className="flex items-center gap-3">
                <Link
                  to="/login"
                  className="text-gray-300 hover:text-white transition-colors text-sm font-medium"
                >
                  Sign In
                </Link>
                <Link
                  to="/register"
                  className="px-4 py-2 bg-gradient-to-r from-primary-600 to-secondary-600 text-white text-sm font-medium rounded-lg hover:from-primary-500 hover:to-secondary-500 transition-all shadow-lg hover:shadow-primary-500/25"
                >
                  Get Started
                </Link>
              </div>
            )}
          </div>

          {/* Mobile Toggle */}
          <button
            onClick={() => setMobileOpen(!mobileOpen)}
            className="md:hidden text-gray-300 hover:text-white"
          >
            {mobileOpen ? <HiX className="text-2xl" /> : <HiMenu className="text-2xl" />}
          </button>
        </div>
      </div>

      {/* Mobile Menu */}
      {mobileOpen && (
        <div className="md:hidden bg-slate-800 border-t border-slate-700 animate-fade-in">
          <div className="px-4 py-3 space-y-2">
            <Link to="/providers" onClick={() => setMobileOpen(false)} className="block py-2 text-gray-300 hover:text-white text-sm">
              Find Doctors
            </Link>
            {isAuthenticated ? (
              <>
                <Link to={getDashboardPath()} onClick={() => setMobileOpen(false)} className="block py-2 text-gray-300 hover:text-white text-sm">
                  Dashboard
                </Link>
                <button onClick={handleLogout} className="block py-2 text-red-400 hover:text-red-300 text-sm">
                  Sign Out
                </button>
              </>
            ) : (
              <>
                <Link to="/login" onClick={() => setMobileOpen(false)} className="block py-2 text-gray-300 hover:text-white text-sm">
                  Sign In
                </Link>
                <Link to="/register" onClick={() => setMobileOpen(false)} className="block py-2 text-primary-400 hover:text-primary-300 text-sm font-medium">
                  Get Started
                </Link>
              </>
            )}
          </div>
        </div>
      )}
    </nav>
  );
}
