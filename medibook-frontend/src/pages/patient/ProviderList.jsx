import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../../services/api';
import { Building2, MapPin, Clock, ArrowRight } from 'lucide-react';

export default function ProviderList() {
  const [providers, setProviders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  useEffect(() => {
    loadProviders();
  }, []);

  const loadProviders = async () => {
    try {
      const data = await api.get('/providers');
      setProviders(data);
    } catch (err) {
      console.error('Failed to load providers:', err);
    } finally {
      setLoading(false);
    }
  };

  const filtered = providers.filter((p) => {
    const term = search.toLowerCase();
    return (
      p.userName?.toLowerCase().includes(term) ||
      p.specialization?.toLowerCase().includes(term) ||
      p.clinicName?.toLowerCase().includes(term)
    );
  });

  if (loading) {
    return (
      <div className="flex items-center justify-center py-16">
        <div className="spinner" />
      </div>
    );
  }

  return (
    <div className="max-w-5xl mx-auto px-6 py-8">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900 mb-1">Find a Provider</h1>
        <p className="text-sm text-gray-500">Browse verified healthcare providers and book an appointment</p>
      </div>

      <div className="mb-6">
        <input
          type="text"
          className="w-full max-w-sm px-3.5 py-2.5 text-sm border border-gray-300 rounded-lg bg-white text-gray-900 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/10 transition-all placeholder:text-gray-400"
          placeholder="Search by name, specialization, or clinic..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>

      {filtered.length === 0 ? (
        <div className="text-center py-16">
          <Building2 className="w-12 h-12 mx-auto text-gray-300 mb-4" />
          <h3 className="text-base font-semibold text-gray-600 mb-1">No providers found</h3>
          <p className="text-sm text-gray-400">Try adjusting your search terms</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
          {filtered.map((provider) => (
            <div
              key={provider.id}
              className="bg-white border border-gray-200 rounded-xl p-6 shadow-sm hover:shadow-md transition-shadow flex flex-col gap-3"
            >
              <div>
                <div className="text-base font-semibold text-gray-900">{provider.userName}</div>
                <div className="text-sm font-medium text-blue-600">{provider.specialization}</div>
              </div>
              <div className="flex items-center gap-2 text-sm text-gray-500">
                <Building2 className="w-4 h-4 text-gray-400" />
                {provider.clinicName}
              </div>
              <div className="flex items-center gap-2 text-sm text-gray-500">
                <MapPin className="w-4 h-4 text-gray-400" />
                {provider.clinicAddress}
              </div>
              <div className="flex items-center gap-2 text-sm text-gray-500">
                <Clock className="w-4 h-4 text-gray-400" />
                {provider.experienceYears} years experience
              </div>
              <Link
                to={`/providers/${provider.id}/slots`}
                className="mt-auto inline-flex items-center justify-center gap-2 px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors"
              >
                View Available Slots
                <ArrowRight className="w-3.5 h-3.5" />
              </Link>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
