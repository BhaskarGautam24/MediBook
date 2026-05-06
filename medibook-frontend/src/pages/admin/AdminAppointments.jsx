import { useState, useEffect } from 'react';
import api from '../../services/api';

const statusClasses = {
  PENDING: 'bg-amber-50 text-amber-700',
  BOOKED: 'bg-amber-50 text-amber-700',
  SCHEDULED: 'bg-green-50 text-green-700',
  COMPLETED: 'bg-blue-50 text-blue-700',
  CANCELLED: 'bg-red-50 text-red-700',
  REJECTED: 'bg-red-50 text-red-700',
  NO_SHOW: 'bg-gray-100 text-gray-600',
};

export default function AdminAppointments() {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get('/admin/appointments')
      .then((res) => {
        setAppointments(Array.isArray(res) ? res : []);
      })
      .catch((err) => console.error("Failed to load admin appointments:", err))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="flex items-center justify-center py-16">
        <div className="spinner" />
      </div>
    );
  }

  return (
    <div className="bg-white border border-gray-200 rounded-xl shadow-sm overflow-hidden">
      <div className="overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="bg-gray-50 border-b border-gray-200">
              <th className="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-gray-500">Patient</th>
              <th className="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-gray-500">Provider</th>
              <th className="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-gray-500">Date & Time</th>
              <th className="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-gray-500">Status</th>
            </tr>
          </thead>
          <tbody>
            {appointments.length === 0 ? (
              <tr>
                <td colSpan="4" className="text-center py-12 text-sm text-gray-400">
                  No appointments found
                </td>
              </tr>
            ) : (
              appointments.map((apt) => {
                const key = apt.status ? apt.status.toUpperCase() : '';
                return (
                  <tr key={apt.id} className="border-b border-gray-100 hover:bg-gray-50/50 transition-colors">
                    <td className="px-4 py-3 font-medium text-gray-900">{apt.patientName}</td>
                    <td className="px-4 py-3 text-gray-600">{apt.providerName}</td>
                    <td className="px-4 py-3 text-gray-600">{apt.appointmentDate}</td>
                    <td className="px-4 py-3">
                      <span className={`inline-flex px-2.5 py-0.5 text-xs font-semibold rounded-full uppercase tracking-wide ${statusClasses[key] || 'bg-gray-100 text-gray-600'}`}>
                        {apt.status || 'Unknown'}
                      </span>
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
