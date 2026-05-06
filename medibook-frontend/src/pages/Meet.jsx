import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { HeartPulse, ArrowLeft, Video, ShieldCheck } from 'lucide-react';

export default function Meet() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);

  // We simulate a loading state to make the UI feel smooth before showing the iframe
  useEffect(() => {
    const timer = setTimeout(() => setLoading(false), 1500);
    return () => clearTimeout(timer);
  }, []);

  const roomName = `MediBook-Consultation-${id}`;

  return (
    <div className="min-h-screen bg-gradient-to-br from-rose-50 via-white to-teal-50 flex flex-col">
      {/* Warm Header */}
      <header className="bg-white/80 backdrop-blur-md border-b border-rose-100 px-6 py-4 flex items-center justify-between sticky top-0 z-10 shadow-sm">
        <div className="flex items-center gap-4">
          <button 
            onClick={() => navigate(-1)} 
            className="p-2 text-gray-500 hover:text-rose-600 hover:bg-rose-50 rounded-full transition-colors"
            title="Go back"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          <div className="flex items-center gap-2">
            <HeartPulse className="w-7 h-7 text-rose-500" />
            <span className="text-xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-rose-600 to-teal-600">
              MediBook Care
            </span>
          </div>
        </div>
        <div className="flex items-center gap-2 text-sm font-medium text-teal-700 bg-teal-50 px-3 py-1.5 rounded-full border border-teal-100">
          <ShieldCheck className="w-4 h-4" />
          Secure & Private Session
        </div>
      </header>

      {/* Main Content */}
      <main className="flex-1 max-w-6xl w-full mx-auto p-4 sm:p-6 lg:p-8 flex flex-col">
        <div className="text-center mb-6">
          <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight">
            Virtual Consultation Room
          </h1>
          <p className="text-gray-500 mt-2 max-w-2xl mx-auto">
            Welcome, {user?.name}. Your doctor will join you shortly. We care about your health and privacy.
          </p>
        </div>

        <div className="flex-1 bg-white rounded-3xl shadow-xl shadow-rose-100/50 border border-rose-100 overflow-hidden flex flex-col relative group">
          {loading ? (
            <div className="absolute inset-0 flex flex-col items-center justify-center bg-white z-20">
              <div className="w-16 h-16 relative mb-4">
                <div className="absolute inset-0 rounded-full border-4 border-rose-100"></div>
                <div className="absolute inset-0 rounded-full border-4 border-rose-500 border-t-transparent animate-spin"></div>
                <HeartPulse className="w-6 h-6 text-rose-500 absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 animate-pulse" />
              </div>
              <h3 className="text-lg font-medium text-gray-900">Preparing your private room...</h3>
              <p className="text-sm text-gray-500 mt-1">Connecting securely to MediBook servers</p>
            </div>
          ) : null}

          {/* Jitsi Iframe */}
          <div className="flex-1 w-full bg-gray-900 relative">
            <iframe
              src={`https://meet.jit.si/${roomName}?config.startWithAudioMuted=true&config.startWithVideoMuted=true&userInfo.displayName=${encodeURIComponent(user?.name || 'Guest')}`}
              allow="camera; microphone; fullscreen; display-capture; autoplay"
              className="w-full h-full border-0 absolute inset-0"
              title="MediBook Teleconsultation"
            />
          </div>
          
          {/* Bottom Bar */}
          <div className="bg-rose-50 px-6 py-4 flex items-center justify-between border-t border-rose-100">
            <div className="flex items-center gap-2 text-rose-700 font-medium">
              <Video className="w-5 h-5 animate-pulse" />
              Live Session Active
            </div>
            <button 
              onClick={() => navigate(-1)}
              className="px-6 py-2 bg-red-600 hover:bg-red-700 text-white rounded-full font-semibold shadow-md shadow-red-200 transition-all active:scale-95"
            >
              End Call & Leave
            </button>
          </div>
        </div>
      </main>
    </div>
  );
}
