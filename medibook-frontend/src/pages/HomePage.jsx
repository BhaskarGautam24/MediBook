import { Link } from 'react-router-dom';
import { HiSearch, HiCalendar, HiShieldCheck, HiChatAlt2, HiCreditCard, HiDocumentText } from 'react-icons/hi';

const features = [
  { icon: HiSearch, title: 'Find Specialists', desc: 'Search doctors by specialization, location, or rating', color: 'from-blue-500 to-blue-600' },
  { icon: HiCalendar, title: 'Book Instantly', desc: 'View real-time availability and book in seconds', color: 'from-teal-500 to-teal-600' },
  { icon: HiShieldCheck, title: 'Verified Doctors', desc: 'All providers are credential-verified by our team', color: 'from-purple-500 to-purple-600' },
  { icon: HiCreditCard, title: 'Secure Payments', desc: 'Pay online via card, UPI, or wallet — or at clinic', color: 'from-amber-500 to-amber-600' },
  { icon: HiDocumentText, title: 'Medical Records', desc: 'Access prescriptions and records after every visit', color: 'from-rose-500 to-rose-600' },
  { icon: HiChatAlt2, title: 'Reviews & Ratings', desc: 'Read verified patient reviews before booking', color: 'from-indigo-500 to-indigo-600' },
];

export default function HomePage() {
  return (
    <div>
      {/* Hero Section */}
      <section className="relative overflow-hidden">
        {/* Background Glow */}
        <div className="absolute inset-0 overflow-hidden">
          <div className="absolute -top-40 -right-40 w-96 h-96 bg-primary-500/20 rounded-full blur-3xl"></div>
          <div className="absolute -bottom-40 -left-40 w-96 h-96 bg-secondary-500/20 rounded-full blur-3xl"></div>
        </div>

        <div className="relative max-w-7xl mx-auto px-4 py-24 sm:py-32">
          <div className="text-center max-w-3xl mx-auto">
            <div className="inline-block mb-6 px-4 py-1.5 bg-primary-500/10 border border-primary-500/20 rounded-full">
              <span className="text-sm text-primary-300 font-medium">🏥 Trusted by 10,000+ patients</span>
            </div>

            <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold text-white leading-tight mb-6">
              Book Smarter.<br />
              <span className="gradient-text">Heal Faster.</span><br />
              Care Better.
            </h1>

            <p className="text-lg text-gray-400 mb-10 max-w-2xl mx-auto leading-relaxed">
              Find top healthcare providers, view their real-time availability, and book appointments instantly — all from one unified platform.
            </p>

            {/* Search Bar */}
            <div className="max-w-xl mx-auto mb-8">
              <div className="flex items-center bg-slate-800/80 border border-slate-700 rounded-xl p-2 shadow-2xl hover:border-primary-500/50 transition-colors">
                <HiSearch className="text-gray-400 text-xl ml-3" />
                <input
                  type="text"
                  placeholder="Search by doctor name, specialization, or location..."
                  className="flex-1 bg-transparent text-white placeholder-gray-500 px-3 py-2 text-sm focus:outline-none"
                />
                <Link
                  to="/providers"
                  className="px-5 py-2.5 bg-gradient-to-r from-primary-600 to-secondary-600 text-white text-sm font-medium rounded-lg hover:from-primary-500 hover:to-secondary-500 transition-all shadow-lg"
                >
                  Search
                </Link>
              </div>
            </div>

            {/* CTAs */}
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <Link
                to="/register"
                className="px-8 py-3.5 bg-gradient-to-r from-primary-600 to-primary-700 text-white font-semibold rounded-xl hover:from-primary-500 hover:to-primary-600 transition-all shadow-xl hover:shadow-primary-500/25 text-sm"
              >
                Get Started — It's Free
              </Link>
              <Link
                to="/providers"
                className="px-8 py-3.5 bg-slate-800 text-gray-300 font-semibold rounded-xl border border-slate-700 hover:bg-slate-700 hover:text-white transition-all text-sm"
              >
                Browse Doctors
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* Features */}
      <section className="max-w-7xl mx-auto px-4 py-20">
        <div className="text-center mb-16">
          <h2 className="text-3xl font-bold text-white mb-4">Everything You Need</h2>
          <p className="text-gray-400 max-w-xl mx-auto">A complete healthcare booking platform designed for patients, providers, and administrators.</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {features.map((f, i) => (
            <div
              key={i}
              className="group p-6 bg-slate-800/50 border border-slate-700/50 rounded-2xl hover:border-primary-500/30 hover:bg-slate-800 transition-all duration-300 hover:-translate-y-1"
            >
              <div className={`w-12 h-12 rounded-xl bg-gradient-to-br ${f.color} flex items-center justify-center text-white text-xl mb-4 shadow-lg group-hover:scale-110 transition-transform`}>
                <f.icon />
              </div>
              <h3 className="text-lg font-semibold text-white mb-2">{f.title}</h3>
              <p className="text-sm text-gray-400 leading-relaxed">{f.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Stats */}
      <section className="border-y border-slate-800">
        <div className="max-w-7xl mx-auto px-4 py-16">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-8">
            {[
              { val: '10K+', label: 'Patients' },
              { val: '500+', label: 'Verified Doctors' },
              { val: '50K+', label: 'Appointments Booked' },
              { val: '4.8★', label: 'Average Rating' },
            ].map((s, i) => (
              <div key={i} className="text-center">
                <p className="text-3xl font-bold gradient-text mb-1">{s.val}</p>
                <p className="text-sm text-gray-500">{s.label}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="max-w-7xl mx-auto px-4 py-20">
        <div className="relative overflow-hidden rounded-3xl bg-gradient-to-r from-primary-600 to-secondary-600 p-12 text-center">
          <div className="absolute -top-20 -right-20 w-60 h-60 bg-white/10 rounded-full blur-2xl"></div>
          <h2 className="text-3xl font-bold text-white mb-4">Ready to Book Your Appointment?</h2>
          <p className="text-primary-100 mb-8 max-w-lg mx-auto">Join thousands of patients who trust MediBook for their healthcare needs.</p>
          <Link
            to="/register"
            className="inline-block px-8 py-3.5 bg-white text-primary-700 font-semibold rounded-xl hover:bg-gray-100 transition-all shadow-xl text-sm"
          >
            Create Free Account
          </Link>
        </div>
      </section>
    </div>
  );
}
