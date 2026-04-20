import { Outlet } from 'react-router-dom';
import Navbar from '../components/Navbar';

/**
 * Main layout for public pages — Navbar + content.
 */
export default function MainLayout() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900">
      <Navbar />
      <main>
        <Outlet />
      </main>

      {/* Footer */}
      <footer className="border-t border-slate-800 mt-20">
        <div className="max-w-7xl mx-auto px-4 py-8">
          <div className="flex flex-col md:flex-row justify-between items-center gap-4">
            <div className="flex items-center gap-2">
              <div className="w-7 h-7 rounded-md bg-gradient-to-br from-primary-500 to-secondary-500 flex items-center justify-center text-white font-bold text-sm">M</div>
              <span className="text-sm font-semibold text-gray-400">MediBook</span>
            </div>
            <p className="text-xs text-gray-500">© 2026 MediBook Platform. All rights reserved.</p>
            <div className="flex gap-4 text-xs text-gray-500">
              <a href="#" className="hover:text-gray-300 transition-colors">Privacy</a>
              <a href="#" className="hover:text-gray-300 transition-colors">Terms</a>
              <a href="#" className="hover:text-gray-300 transition-colors">Contact</a>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
}
