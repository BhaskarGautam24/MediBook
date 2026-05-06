import { useState, useEffect } from 'react';
import api from '../../services/api';
import toast from 'react-hot-toast';
import { ClipboardList, X, Check, XCircle, CheckCircle, FileText, Video } from 'lucide-react';

const statusClasses = {
  PENDING: 'bg-amber-50 text-amber-700',
  BOOKED: 'bg-amber-50 text-amber-700',
  SCHEDULED: 'bg-green-50 text-green-700',
  COMPLETED: 'bg-blue-50 text-blue-700',
  CANCELLED: 'bg-red-50 text-red-700',
  REJECTED: 'bg-red-50 text-red-700',
  NO_SHOW: 'bg-orange-50 text-orange-700',
};

export default function ProviderAppointments() {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);

  // Medical Record Modal State
  const [isRecordModalOpen, setIsRecordModalOpen] = useState(false);
  const [selectedAppointment, setSelectedAppointment] = useState(null);
  const [recordData, setRecordData] = useState({
    diagnosis: '',
    prescription: '',
    notes: '',
    followUpDate: '',
  });
  const [isRecordExisting, setIsRecordExisting] = useState(false);

  useEffect(() => {
    loadAppointments();
  }, []);

  const loadAppointments = async () => {
    try {
      const data = await api.get('/appointments/provider');
      setAppointments(data);
    } catch (err) {
      console.error('Failed to load appointments:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleComplete = async (id) => {
    try {
      await api.put(`/appointments/${id}/complete`);
      toast.success('Appointment marked as completed');
      loadAppointments();
    } catch (err) {
      toast.error(err.message || 'Failed to complete');
    }
  };

  const handleAccept = async (id) => {
    try {
      await api.put(`/appointments/${id}/accept`);
      toast.success('Appointment accepted');
      loadAppointments();
    } catch (err) {
      toast.error(err.message || 'Failed to accept');
    }
  };

  const handleReject = async (id) => {
    if (!window.confirm('Are you sure you want to reject this appointment?')) return;
    try {
      await api.put(`/appointments/${id}/reject`);
      toast.success('Appointment rejected');
      loadAppointments();
    } catch (err) {
      toast.error(err.message || 'Failed to reject');
    }
  };

  // --- Medical Record Logic ---
  const openRecordModal = async (apt) => {
    setSelectedAppointment(apt);
    setIsRecordModalOpen(true);
    setRecordData({ diagnosis: '', prescription: '', notes: '', followUpDate: '' });
    setIsRecordExisting(false);

    try {
      const existingRecord = await api.get(`/records/appointment/${apt.id}`);
      if (existingRecord) {
        setRecordData({
          diagnosis: existingRecord.diagnosis || '',
          prescription: existingRecord.prescription || '',
          notes: existingRecord.notes || '',
          followUpDate: existingRecord.followUpDate || '',
          recordId: existingRecord.recordId,
        });
        setIsRecordExisting(true);
      }
    } catch (err) {
      console.log('No existing record found');
    }
  };

  const closeRecordModal = () => {
    setIsRecordModalOpen(false);
    setSelectedAppointment(null);
  };

  const handleRecordSubmit = async (e) => {
    e.preventDefault();
    try {
      if (isRecordExisting) {
        await api.put(`/records/${recordData.recordId}`, {
          appointmentId: selectedAppointment.id,
          ...recordData,
        });
        toast.success('Medical Record updated successfully');
      } else {
        await api.post('/records', {
          appointmentId: selectedAppointment.id,
          ...recordData,
        });
        toast.success('Medical Record saved successfully');
      }
      closeRecordModal();
    } catch (err) {
      toast.error(err.message || 'Failed to save record');
    }
  };

  const handleRecordChange = (e) => {
    const { name, value } = e.target;
    setRecordData((prev) => ({ ...prev, [name]: value }));
  };

  const inputClass =
    'w-full px-3.5 py-2.5 text-sm border border-gray-300 rounded-lg bg-white text-gray-900 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/10 transition-all placeholder:text-gray-400';

  if (loading) {
    return (
      <div className="flex items-center justify-center py-16">
        <div className="spinner" />
      </div>
    );
  }

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900 mb-1">My Appointments</h1>
        <p className="text-sm text-gray-500">View and manage patient appointments</p>
      </div>

      {appointments.length === 0 ? (
        <div className="text-center py-16">
          <ClipboardList className="w-12 h-12 mx-auto text-gray-300 mb-4" />
          <h3 className="text-base font-semibold text-gray-600 mb-1">No appointments yet</h3>
          <p className="text-sm text-gray-400">Appointments will appear here when patients book your slots</p>
        </div>
      ) : (
        <div className="bg-white border border-gray-200 rounded-xl shadow-sm overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="bg-gray-50 border-b border-gray-200">
                  <th className="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-gray-500">Patient</th>
                  <th className="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-gray-500">Date</th>
                  <th className="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-gray-500">Time</th>
                  <th className="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-gray-500">Status</th>
                  <th className="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-gray-500">Booked On</th>
                  <th className="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-gray-500">Action</th>
                </tr>
              </thead>
              <tbody>
                {appointments.map((apt) => (
                  <tr key={apt.id} className="border-b border-gray-100 hover:bg-gray-50/50 transition-colors">
                    <td className="px-4 py-3 font-medium text-gray-900">{apt.patientName}</td>
                    <td className="px-4 py-3 text-gray-600">{apt.slotDate}</td>
                    <td className="px-4 py-3 text-gray-600">{apt.slotStartTime} - {apt.slotEndTime}</td>
                    <td className="px-4 py-3">
                      <span className={`inline-flex px-2.5 py-0.5 text-xs font-semibold rounded-full uppercase tracking-wide ${statusClasses[apt.status] || 'bg-gray-100 text-gray-600'}`}>
                        {apt.status}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-gray-500">{apt.createdAt ? new Date(apt.createdAt).toLocaleDateString() : '—'}</td>
                    <td className="px-4 py-3">
                      {(apt.status === 'PENDING' || apt.status === 'BOOKED') && (
                        <div className="flex gap-2">
                          <button
                            className="inline-flex items-center gap-1 px-3 py-1.5 text-xs font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors cursor-pointer"
                            onClick={() => handleAccept(apt.id)}
                          >
                            <Check className="w-3.5 h-3.5" />
                            Accept
                          </button>
                          <button
                            className="inline-flex items-center gap-1 px-3 py-1.5 text-xs font-medium text-white bg-red-600 rounded-lg hover:bg-red-700 transition-colors cursor-pointer"
                            onClick={() => handleReject(apt.id)}
                          >
                            <XCircle className="w-3.5 h-3.5" />
                            Reject
                          </button>
                        </div>
                      )}
                      {apt.status === 'SCHEDULED' && (
                        <div className="flex gap-2">
                          <button
                            className="inline-flex items-center gap-1 px-3 py-1.5 text-xs font-medium text-white bg-purple-600 rounded-lg hover:bg-purple-700 transition-colors cursor-pointer"
                            onClick={() => window.location.href = `/meet/${apt.id}`}
                          >
                            <Video className="w-3.5 h-3.5" /> Join Video
                          </button>
                          <button
                            className="inline-flex items-center gap-1 px-3 py-1.5 text-xs font-medium text-white bg-green-600 rounded-lg hover:bg-green-700 transition-colors cursor-pointer"
                            onClick={() => handleComplete(apt.id)}
                          >
                            <CheckCircle className="w-3.5 h-3.5" />
                            Complete
                          </button>
                        </div>
                      )}
                      {apt.status === 'COMPLETED' && (
                        <button
                          className="inline-flex items-center gap-1 px-3 py-1.5 text-xs font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors cursor-pointer"
                          onClick={() => openRecordModal(apt)}
                        >
                          <FileText className="w-3.5 h-3.5" />
                          Record
                        </button>
                      )}
                      {apt.status === 'CANCELLED' && (
                        <span className="text-xs italic text-gray-400">Cancelled</span>
                      )}
                      {apt.status === 'REJECTED' && (
                        <span className="text-xs italic text-red-400">Rejected</span>
                      )}
                      {apt.status === 'NO_SHOW' && (
                        <span className="text-xs italic text-orange-400">No Show</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Medical Record Modal */}
      {isRecordModalOpen && (
        <div className="fixed inset-0 bg-black/40 flex justify-center items-center z-50 px-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-lg p-6">
            <div className="flex justify-between items-center mb-5 pb-4 border-b border-gray-200">
              <h2 className="text-lg font-bold text-gray-900">
                {isRecordExisting ? 'Edit' : 'Add'} Medical Record
              </h2>
              <button onClick={closeRecordModal} className="text-gray-400 hover:text-gray-600 cursor-pointer p-1">
                <X className="w-5 h-5" />
              </button>
            </div>
            <form onSubmit={handleRecordSubmit}>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-500">
                  Patient: <span className="text-gray-900">{selectedAppointment?.patientName}</span>
                </label>
              </div>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Diagnosis *</label>
                <textarea
                  name="diagnosis"
                  value={recordData.diagnosis}
                  onChange={handleRecordChange}
                  required
                  rows="3"
                  className={inputClass}
                  placeholder="Enter medical diagnosis"
                />
              </div>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Prescription *</label>
                <textarea
                  name="prescription"
                  value={recordData.prescription}
                  onChange={handleRecordChange}
                  required
                  rows="3"
                  className={inputClass}
                  placeholder="Enter prescribed medications"
                />
              </div>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Clinical Notes</label>
                <textarea
                  name="notes"
                  value={recordData.notes}
                  onChange={handleRecordChange}
                  rows="3"
                  className={inputClass}
                  placeholder="Enter additional clinical notes"
                />
              </div>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1.5">Follow-Up Date</label>
                <input
                  type="date"
                  name="followUpDate"
                  value={recordData.followUpDate}
                  onChange={handleRecordChange}
                  className={inputClass}
                />
              </div>
              <div className="mt-6 flex gap-3 justify-end">
                <button
                  type="button"
                  className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors cursor-pointer"
                  onClick={closeRecordModal}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors cursor-pointer"
                >
                  Save Record
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
