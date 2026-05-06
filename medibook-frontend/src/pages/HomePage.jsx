import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Search, CalendarCheck, ShieldCheck, ArrowRight, Activity, HeartPulse } from 'lucide-react';

const features = [
  {
    icon: Search,
    title: 'Find Providers',
    description: 'Browse verified healthcare providers by specialization and book the right doctor for your needs.',
  },
  {
    icon: CalendarCheck,
    title: 'Easy Booking',
    description: 'View real-time availability and book appointments instantly with just a few clicks.',
  },
  {
    icon: ShieldCheck,
    title: 'Verified Doctors',
    description: 'All healthcare providers are verified by our admin team to ensure quality and trust.',
  },
];

export default function HomePage() {
  const { isAuthenticated, user } = useAuth();

  const getDashboardLink = () => {
    if (!user) return '/login';
    const map = {
      PATIENT: '/providers',
      PROVIDER: '/provider/dashboard',
      ADMIN: '/admin/dashboard',
    };
    return map[user.role] || '/';
  };

  return (
    <div className="min-h-screen bg-slate-50 overflow-hidden">
      {/* Decorative Background Elements */}
      <div className="absolute top-0 inset-x-0 h-96 bg-gradient-to-b from-blue-100/50 to-transparent pointer-events-none" />
      <div className="absolute -top-40 -right-40 w-96 h-96 bg-blue-400/20 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute top-40 -left-40 w-96 h-96 bg-indigo-400/20 rounded-full blur-3xl pointer-events-none" />

      {/* Hero */}
      <section className="relative pt-32 pb-20 px-5 max-w-4xl mx-auto text-center z-10">
        <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-blue-50 border border-blue-100 text-blue-700 text-sm font-medium mb-8 shadow-sm">
          <Activity className="w-4 h-4" />
          <span>Modern Healthcare Access</span>
        </div>
        <h1 className="text-5xl md:text-7xl font-extrabold text-slate-900 mb-6 leading-tight tracking-tight">
          Book Smarter. <br />
          <span className="text-transparent bg-clip-text bg-gradient-to-r from-blue-600 to-indigo-600">
            Heal Faster. Care Better.
          </span>
        </h1>
        <p className="text-lg md:text-xl text-slate-600 mb-10 leading-relaxed max-w-2xl mx-auto font-medium">
          MediBook connects patients with verified healthcare providers.
          Search doctors, view available slots, and book appointments —
          all in one seamless, secure platform.
        </p>
        <div className="flex gap-4 justify-center flex-col sm:flex-row items-center">
          {isAuthenticated ? (
            <Link
              to={getDashboardLink()}
              className="group inline-flex items-center gap-2 px-8 py-4 text-lg font-semibold text-white bg-gradient-to-r from-blue-600 to-indigo-600 rounded-xl hover:shadow-lg hover:shadow-blue-500/30 hover:-translate-y-0.5 transition-all duration-300"
            >
              Go to Dashboard
              <ArrowRight className="w-5 h-5 group-hover:translate-x-1 transition-transform" />
            </Link>
          ) : (
            <>
              <Link
                to="/register"
                className="group inline-flex items-center gap-2 px-8 py-4 text-lg font-semibold text-white bg-gradient-to-r from-blue-600 to-indigo-600 rounded-xl hover:shadow-lg hover:shadow-blue-500/30 hover:-translate-y-0.5 transition-all duration-300 w-full sm:w-auto justify-center"
              >
                Get Started
                <ArrowRight className="w-5 h-5 group-hover:translate-x-1 transition-transform" />
              </Link>
              <Link
                to="/login"
                className="inline-flex items-center justify-center px-8 py-4 text-lg font-semibold text-slate-700 bg-white border-2 border-slate-200 rounded-xl hover:border-slate-300 hover:bg-slate-50 hover:-translate-y-0.5 transition-all duration-300 w-full sm:w-auto"
              >
                Login
              </Link>
            </>
          )}
        </div>
      </section>

      {/* Stats Section (Visual placeholder) */}
      <section className="relative z-10 max-w-5xl mx-auto px-5 mb-24">
        <div className="bg-white rounded-3xl p-8 md:p-12 shadow-xl shadow-slate-200/50 border border-slate-100 flex flex-col md:flex-row justify-around items-center gap-8 divide-y md:divide-y-0 md:divide-x divide-slate-100">
          <div className="text-center px-8 w-full">
            <div className="text-4xl font-extrabold text-blue-600 mb-2">500+</div>
            <div className="text-slate-500 font-medium">Verified Specialists</div>
          </div>
          <div className="text-center px-8 w-full pt-8 md:pt-0">
            <div className="text-4xl font-extrabold text-indigo-600 mb-2">24/7</div>
            <div className="text-slate-500 font-medium">Instant Booking</div>
          </div>
          <div className="text-center px-8 w-full pt-8 md:pt-0">
            <div className="text-4xl font-extrabold text-blue-600 mb-2">99%</div>
            <div className="text-slate-500 font-medium">Patient Satisfaction</div>
          </div>
        </div>
      </section>

      {/* Features */}
      <section className="relative z-10 grid grid-cols-1 md:grid-cols-3 gap-8 px-5 pb-32 max-w-6xl mx-auto">
        <div className="md:col-span-3 text-center mb-12">
          <HeartPulse className="w-12 h-12 text-blue-500 mx-auto mb-4" />
          <h2 className="text-3xl md:text-4xl font-bold text-slate-900 mb-4">Why Choose MediBook?</h2>
          <p className="text-slate-500 max-w-2xl mx-auto">Experience a new standard of healthcare management tailored for your convenience and peace of mind.</p>
        </div>
        {features.map((feature, idx) => (
          <div
            key={feature.title}
            className="group bg-white/80 backdrop-blur-md border border-white/40 rounded-2xl p-8 text-center shadow-lg shadow-slate-200/40 hover:shadow-xl hover:shadow-blue-500/10 hover:-translate-y-1 transition-all duration-300 relative overflow-hidden"
          >
            <div className="absolute top-0 right-0 w-32 h-32 bg-blue-50 rounded-bl-full -mr-16 -mt-16 transition-transform group-hover:scale-110 z-0" />
            <div className="relative z-10">
              <div className="w-16 h-16 mx-auto mb-6 bg-gradient-to-br from-blue-50 to-indigo-50 rounded-2xl flex items-center justify-center shadow-inner">
                <feature.icon className="w-8 h-8 text-blue-600" />
              </div>
              <h3 className="text-xl font-bold text-slate-900 mb-3">{feature.title}</h3>
              <p className="text-slate-600 leading-relaxed font-medium">{feature.description}</p>
            </div>
          </div>
        ))}
      </section>
    </div>
  );
}
