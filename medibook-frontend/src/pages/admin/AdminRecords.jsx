import { useState, useEffect } from 'react';
import api from '../../services/api';

export default function AdminRecords() {
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get('/admin/records')
      .then((res) => setRecords(res))
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

  return (
    <div>
      <h3 className="text-base font-semibold text-gray-800 mb-4">Medical Records Audit (Read-Only)</h3>
      <div className="bg-white border border-gray-200 rounded-xl shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="bg-gray-50 border-b border-gray-200">
                <th className="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-gray-500">Patient</th>
                <th className="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-gray-500">Provider</th>
                <th className="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-gray-500">Diagnosis</th>
                <th className="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-gray-500">Date</th>
              </tr>
            </thead>
            <tbody>
              {records.map((record) => (
                <tr key={record.id} className="border-b border-gray-100 hover:bg-gray-50/50 transition-colors">
                  <td className="px-4 py-3 font-medium text-gray-900">{record.patientName}</td>
                  <td className="px-4 py-3 text-gray-600">{record.providerName}</td>
                  <td className="px-4 py-3 text-gray-600 max-w-xs truncate">{record.diagnosis}</td>
                  <td className="px-4 py-3 text-gray-500">{record.createdAt ? new Date(record.createdAt).toLocaleString() : 'N/A'}</td>
                </tr>
              ))}
              {records.length === 0 && (
                <tr>
                  <td colSpan="4" className="text-center py-12 text-sm text-gray-400">
                    No medical records found
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
