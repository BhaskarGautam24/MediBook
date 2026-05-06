import { useState, useEffect } from 'react';
import api from '../../services/api';
import toast from 'react-hot-toast';
import { CheckCircle, XCircle } from 'lucide-react';

export default function ManageProviders() {
  const [providers, setProviders] = useState([]);
  const [loading, setLoading] = useState(true);

  const loadProviders = () => {
    api.get('/admin/providers/pending')
      .then((res) => setProviders(res))
      .catch(() => toast.error('Failed to load pending providers'))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadProviders();
  }, []);

  const approveProvider = (id) => {
    api.put(`/admin/providers/${id}/verify`)
      .then(() => {
        toast.success('Provider approved');
        loadProviders();
      })
      .catch(() => toast.error('Failed to approve provider'));
  };

  const rejectProvider = (id) => {
    if (confirm('Are you sure you want to reject this provider?')) {
      api.put(`/admin/providers/${id}/reject`)
        .then(() => {
          toast.success('Provider rejected');
          loadProviders();
        })
        .catch(() => toast.error('Failed to reject provider'));
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-16">
        <div className="spinner" />
      </div>
    );
  }

  return (
    <div>
      <h3 className="text-base font-semibold text-gray-800 mb-4">Pending Verifications</h3>
      {providers.length === 0 ? (
        <p className="text-sm text-gray-400">No pending providers to verify.</p>
      ) : (
        <div className="bg-white border border-gray-200 rounded-xl shadow-sm overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="bg-gray-50 border-b border-gray-200">
                  <th className="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-gray-500">Provider Name</th>
                  <th className="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-gray-500">Email</th>
                  <th className="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-gray-500">Specialization</th>
                  <th className="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-gray-500">Actions</th>
                </tr>
              </thead>
              <tbody>
                {providers.map((provider) => (
                  <tr key={provider.id} className="border-b border-gray-100 hover:bg-gray-50/50 transition-colors">
                    <td className="px-4 py-3 font-medium text-gray-900">{provider.name}</td>
                    <td className="px-4 py-3 text-gray-600">{provider.email}</td>
                    <td className="px-4 py-3 text-gray-600">{provider.specialization}</td>
                    <td className="px-4 py-3">
                      <div className="flex gap-2">
                        <button
                          className="inline-flex items-center gap-1 px-3 py-1.5 text-xs font-medium text-white bg-green-600 rounded-lg hover:bg-green-700 transition-colors cursor-pointer"
                          onClick={() => approveProvider(provider.id)}
                        >
                          <CheckCircle className="w-3.5 h-3.5" />
                          Approve
                        </button>
                        <button
                          className="inline-flex items-center gap-1 px-3 py-1.5 text-xs font-medium text-white bg-red-600 rounded-lg hover:bg-red-700 transition-colors cursor-pointer"
                          onClick={() => rejectProvider(provider.id)}
                        >
                          <XCircle className="w-3.5 h-3.5" />
                          Reject
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}
