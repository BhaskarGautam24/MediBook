import { useState, useEffect } from 'react';
import api from '../../services/api';
import { Users, Building2, Calendar, CheckCircle2 } from 'lucide-react';

export default function AdminDashboard() {
  const [stats, setStats] = useState({
    totalUsers: 0,
    totalProviders: 0,
    totalAppointments: 0,
    completedAppointments: 0,
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      api.get('/admin/users'),
      api.get('/admin/providers'),
      api.get('/admin/appointments')
    ])
      .then(([users, providers, appointments]) => {
        setStats({
          totalUsers: users.length,
          totalProviders: providers.length,
          totalAppointments: appointments.length,
          completedAppointments: appointments.filter(a => a.status === 'COMPLETED').length,
        });
      })
      .catch((err) => console.error(err))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="flex items-center justify-center py-16">
        <div className="spinner" />
      </div>
    );
  }

  const cards = [
    { label: 'Total Users', value: stats.totalUsers, icon: Users, color: 'text-blue-600 bg-blue-50' },
    { label: 'Total Providers', value: stats.totalProviders, icon: Building2, color: 'text-amber-600 bg-amber-50' },
    { label: 'Total Appointments', value: stats.totalAppointments, icon: Calendar, color: 'text-purple-600 bg-purple-50' },
    { label: 'Completed', value: stats.completedAppointments, icon: CheckCircle2, color: 'text-green-600 bg-green-50' },
  ];

  return (
    <div>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        {cards.map((card) => (
          <div key={card.label} className="bg-white border border-gray-200 rounded-xl p-5 shadow-sm">
            <div className="flex items-center justify-between mb-3">
              <span className="text-xs font-semibold uppercase tracking-wider text-gray-400">{card.label}</span>
              <div className={`w-9 h-9 rounded-lg flex items-center justify-center ${card.color}`}>
                <card.icon className="w-4.5 h-4.5" />
              </div>
            </div>
            <div className="text-2xl font-bold text-gray-900">{card.value}</div>
          </div>
        ))}
      </div>
    </div>
  );
}
