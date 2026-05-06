import { useState, useEffect } from 'react';
import api from '../../services/api';
import toast from 'react-hot-toast';
import { Ban, CheckCircle, Trash2 } from 'lucide-react';

const roleClasses = {
  ADMIN: 'bg-sky-50 text-sky-700',
  PROVIDER: 'bg-amber-50 text-amber-700',
  PATIENT: 'bg-gray-100 text-gray-600',
};

export default function ManageUsers() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);

  const loadUsers = () => {
    api.get('/admin/users')
      .then((res) => setUsers(res))
      .catch(() => toast.error('Failed to load users'))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadUsers();
  }, []);

  const suspendUser = (id) => {
    api.put(`/admin/users/${id}/suspend`)
      .then(() => { toast.success('User suspended'); loadUsers(); })
      .catch(() => toast.error('Failed to suspend user'));
  };

  const activateUser = (id) => {
    api.put(`/admin/users/${id}/activate`)
      .then(() => { toast.success('User activated'); loadUsers(); })
      .catch(() => toast.error('Failed to activate user'));
  };

  const deleteUser = (id) => {
    if (confirm('Are you sure you want to delete this user?')) {
      api.delete(`/admin/users/${id}`)
        .then(() => { toast.success('User deleted'); loadUsers(); })
        .catch(() => toast.error('Failed to delete user'));
    }
  };

  const isActive = (user) => user.isActive ?? user.active ?? true;

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
              <th className="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-gray-500">Name</th>
              <th className="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-gray-500">Email</th>
              <th className="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-gray-500">Role</th>
              <th className="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-gray-500">Status</th>
              <th className="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-gray-500">Actions</th>
            </tr>
          </thead>
          <tbody>
            {users.map((user) => (
              <tr key={user.id} className="border-b border-gray-100 hover:bg-gray-50/50 transition-colors">
                <td className="px-4 py-3 font-medium text-gray-900">{user.name}</td>
                <td className="px-4 py-3 text-gray-600">{user.email}</td>
                <td className="px-4 py-3">
                  <span className={`inline-flex px-2.5 py-0.5 text-xs font-semibold rounded-full uppercase tracking-wide ${roleClasses[user.role] || roleClasses.PATIENT}`}>
                    {user.role || 'Unknown'}
                  </span>
                </td>
                <td className="px-4 py-3">
                  <span className={`inline-flex px-2.5 py-0.5 text-xs font-semibold rounded-full uppercase ${
                    isActive(user) ? 'bg-green-50 text-green-700' : 'bg-red-50 text-red-700'
                  }`}>
                    {isActive(user) ? 'Active' : 'Suspended'}
                  </span>
                </td>
                <td className="px-4 py-3">
                  <div className="flex gap-2">
                    {isActive(user) ? (
                      <button
                        className="inline-flex items-center gap-1 px-3 py-1.5 text-xs font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors cursor-pointer"
                        onClick={() => suspendUser(user.id)}
                      >
                        <Ban className="w-3.5 h-3.5" />
                        Suspend
                      </button>
                    ) : (
                      <button
                        className="inline-flex items-center gap-1 px-3 py-1.5 text-xs font-medium text-white bg-green-600 rounded-lg hover:bg-green-700 transition-colors cursor-pointer"
                        onClick={() => activateUser(user.id)}
                      >
                        <CheckCircle className="w-3.5 h-3.5" />
                        Activate
                      </button>
                    )}
                    <button
                      className="inline-flex items-center gap-1 px-3 py-1.5 text-xs font-medium text-white bg-red-600 rounded-lg hover:bg-red-700 transition-colors cursor-pointer"
                      onClick={() => deleteUser(user.id)}
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                      Delete
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
