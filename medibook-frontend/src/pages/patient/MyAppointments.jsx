import { useState, useEffect } from 'react';
import api from '../../services/api';
import toast from 'react-hot-toast';
import { ClipboardList, X, Video, Star, Receipt, FileText } from 'lucide-react';

const statusClasses = {
  PENDING_PAYMENT: 'bg-orange-50 text-orange-700',
  PENDING: 'bg-amber-50 text-amber-700',
  BOOKED: 'bg-amber-50 text-amber-700',
  SCHEDULED: 'bg-green-50 text-green-700',
  COMPLETED: 'bg-blue-50 text-blue-700',
  CANCELLED: 'bg-red-50 text-red-700',
  REJECTED: 'bg-red-50 text-red-700',
  NO_SHOW: 'bg-orange-50 text-orange-700',
};

const paymentStatusClasses = {
  PENDING: 'bg-yellow-50 text-yellow-700 border-yellow-200',
  PAID: 'bg-green-50 text-green-700 border-green-200',
  REFUNDED: 'bg-purple-50 text-purple-700 border-purple-200',
  FAILED: 'bg-red-50 text-red-700 border-red-200',
};

export default function MyAppointments() {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);

  // View Record Modal State
  const [isRecordModalOpen, setIsRecordModalOpen] = useState(false);
  const [selectedRecord, setSelectedRecord] = useState(null);
  const [loadingRecord, setLoadingRecord] = useState(false);

  // Invoice Modal
  const [invoiceData, setInvoiceData] = useState(null);
  const [isInvoiceOpen, setIsInvoiceOpen] = useState(false);

  // PDF Features: Review & Video states
  const [isVideoOpen, setIsVideoOpen] = useState(false);
  const [isReviewOpen, setIsReviewOpen] = useState(false);
  const [reviewForm, setReviewForm] = useState({ rating: 0, comment: '' });

  useEffect(() => {
    loadAppointments();
  }, []);

  const loadAppointments = async () => {
    try {
      const data = await api.get('/appointments/my');
      setAppointments(data);
    } catch (err) {
      console.error('Failed to load appointments:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = async (id) => {
    if (!window.confirm('Are you sure you want to cancel this appointment?')) return;
    try {
      const result = await api.put(`/appointments/${id}/cancel`);
      // Show refund info if available
      if (result?.refund?.refunded) {
        toast.success(`Cancelled! Refund of ₹${result.refund.amount} will be processed in 3-5 days.`);
      } else if (result?.refund?.message) {
        toast.success(`Cancelled. ${result.refund.message}`);
      } else {
        toast.success('Appointment cancelled');
      }
      loadAppointments();
    } catch (err) {
      toast.error(err.message || 'Cancel failed');
    }
  };

  const handleViewRecord = async (aptId) => {
    setLoadingRecord(true);
    try {
      const record = await api.get(`/records/appointment/${aptId}`);
      if (record) {
        setSelectedRecord(record);
        setIsRecordModalOpen(true);
      }
    } catch (err) {
      toast.error('Medical record not available yet');
    } finally {
      setLoadingRecord(false);
    }
  };

  const handleViewInvoice = async (paymentId) => {
    try {
      const data = await api.get(`/payments/${paymentId}/invoice`);
      setInvoiceData(data);
      setIsInvoiceOpen(true);
    } catch (err) {
      toast.error('Invoice not available');
    }
  };

  const closeRecordModal = () => { setIsRecordModalOpen(false); setSelectedRecord(null); };
  const closeInvoiceModal = () => { setIsInvoiceOpen(false); setInvoiceData(null); };

  const submitReview = (e) => {
    e.preventDefault();
    if (!reviewForm.rating || !reviewForm.comment) {
      toast.error('Please provide a rating and comment');
      return;
    }
    toast.success('Review submitted! (Dummy endpoint)');
    setIsReviewOpen(false);
    setReviewForm({ rating: 0, comment: '' });
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
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900 mb-1">My Appointments</h1>
        <p className="text-sm text-gray-500">View and manage your booked appointments</p>
      </div>

      {appointments.length === 0 ? (
        <div className="text-center py-16">
          <ClipboardList className="w-12 h-12 mx-auto text-gray-300 mb-4" />
          <h3 className="text-base font-semibold text-gray-600 mb-1">No appointments yet</h3>
          <p className="text-sm text-gray-400">Book your first appointment from the Find Providers page</p>
        </div>
      ) : (
        <div className="bg-white border border-gray-200 rounded-xl shadow-sm overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="bg-gray-50 border-b border-gray-200">
                  <th className="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-gray-500">Provider</th>
                  <th className="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-gray-500">Date</th>
                  <th className="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-gray-500">Time</th>
                  <th className="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-gray-500">Status</th>
                  <th className="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-gray-500">Payment</th>
                  <th className="text-left px-4 py-3 text-xs font-semibold uppercase tracking-wider text-gray-500">Action</th>
                </tr>
              </thead>
              <tbody>
                {appointments.map((apt) => (
                  <tr key={apt.id} className="border-b border-gray-100 hover:bg-gray-50/50 transition-colors">
                    <td className="px-4 py-3 font-medium text-gray-900">{apt.providerName}</td>
                    <td className="px-4 py-3 text-gray-600">{apt.slotDate}</td>
                    <td className="px-4 py-3 text-gray-600">{apt.slotStartTime} - {apt.slotEndTime}</td>
                    <td className="px-4 py-3">
                      <span className={`inline-flex px-2.5 py-0.5 text-xs font-semibold rounded-full uppercase tracking-wide ${statusClasses[apt.status] || 'bg-gray-100 text-gray-600'}`}>
                        {apt.status === 'PENDING_PAYMENT' ? 'AWAITING PAYMENT' : apt.status}
                      </span>
                    </td>
                    <td className="px-4 py-3">
                      {apt.paymentStatus ? (
                        <div className="flex flex-col gap-1">
                          <span className={`inline-flex px-2 py-0.5 text-[10px] font-semibold rounded-full border ${paymentStatusClasses[apt.paymentStatus] || ''}`}>
                            {apt.paymentStatus}
                          </span>
                          <span className="text-[10px] text-gray-400">
                            {apt.paymentMode} • ₹{apt.paymentAmount}
                          </span>
                        </div>
                      ) : (
                        <span className="text-xs text-gray-400">—</span>
                      )}
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex flex-wrap gap-2">
                        {(apt.status === 'SCHEDULED' || apt.status === 'PENDING' || apt.status === 'BOOKED' || apt.status === 'PENDING_PAYMENT') && (
                          <>
                            <button
                              className="px-3 py-1.5 text-xs font-medium text-white bg-red-600 rounded-lg hover:bg-red-700 transition-colors cursor-pointer"
                              onClick={() => handleCancel(apt.id)}
                            >
                              Cancel
                            </button>
                            {apt.status === 'SCHEDULED' && (
                              <button
                                className="inline-flex items-center gap-1 px-3 py-1.5 text-xs font-medium text-white bg-purple-600 rounded-lg hover:bg-purple-700 transition-colors cursor-pointer"
                                onClick={() => window.location.href = `/meet/${apt.id}`}
                              >
                                <Video className="w-3.5 h-3.5" /> Join
                              </button>
                            )}
                          </>
                        )}
                        {apt.status === 'COMPLETED' && (
                          <>
                            <button
                              className="px-3 py-1.5 text-xs font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors cursor-pointer disabled:opacity-50"
                              onClick={() => handleViewRecord(apt.id)}
                              disabled={loadingRecord}
                            >
                              Record
                            </button>
                            {apt.paymentId && (
                              <button
                                className="inline-flex items-center gap-1 px-3 py-1.5 text-xs font-medium text-blue-600 bg-blue-50 rounded-lg hover:bg-blue-100 border border-blue-200 transition-colors cursor-pointer"
                                onClick={() => handleViewInvoice(apt.paymentId)}
                              >
                                <FileText className="w-3.5 h-3.5" /> Invoice
                              </button>
                            )}
                            <button
                              className="inline-flex items-center gap-1 px-3 py-1.5 text-xs font-medium text-amber-600 bg-amber-50 rounded-lg hover:bg-amber-100 border border-amber-200 transition-colors cursor-pointer"
                              onClick={() => setIsReviewOpen(true)}
                            >
                              <Star className="w-3.5 h-3.5" /> Rate
                            </button>
                          </>
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
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* View Record Modal */}
      {isRecordModalOpen && selectedRecord && (
        <div className="fixed inset-0 bg-black/40 flex justify-center items-center z-50 px-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-lg p-6">
            <div className="flex justify-between items-center mb-5 pb-4 border-b border-gray-200">
              <h2 className="text-lg font-bold text-gray-900">Medical Record</h2>
              <button onClick={closeRecordModal} className="text-gray-400 hover:text-gray-600 cursor-pointer p-1">
                <X className="w-5 h-5" />
              </button>
            </div>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-500 mb-0.5">Provider</label>
                <p className="text-sm text-gray-900">{selectedRecord.providerName}</p>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-500 mb-0.5">Patient</label>
                <p className="text-sm text-gray-900">{selectedRecord.patientName}</p>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-500 mb-1">Diagnosis</label>
                <div className="bg-gray-50 border border-gray-200 rounded-lg p-3 text-sm text-gray-800 whitespace-pre-wrap">
                  {selectedRecord.diagnosis}
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-500 mb-1">Prescription</label>
                <div className="bg-gray-50 border border-gray-200 rounded-lg p-3 text-sm text-gray-800 whitespace-pre-wrap">
                  {selectedRecord.prescription}
                </div>
              </div>
              {selectedRecord.notes && (
                <div>
                  <label className="block text-sm font-medium text-gray-500 mb-1">Clinical Notes</label>
                  <div className="bg-gray-50 border border-gray-200 rounded-lg p-3 text-sm text-gray-800 whitespace-pre-wrap">
                    {selectedRecord.notes}
                  </div>
                </div>
              )}
              {selectedRecord.followUpDate && (
                <div>
                  <label className="block text-sm font-medium text-gray-500 mb-0.5">Follow-Up Date</label>
                  <p className="text-sm font-semibold text-blue-600">
                    {new Date(selectedRecord.followUpDate).toLocaleDateString()}
                  </p>
                </div>
              )}
              <div className="text-xs text-gray-400 pt-2">
                Record Date: {new Date(selectedRecord.createdAt).toLocaleString()}
              </div>
            </div>
            <div className="mt-6 flex justify-end">
              <button type="button" className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors cursor-pointer" onClick={closeRecordModal}>
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Invoice Modal */}
      {isInvoiceOpen && invoiceData && (
        <div className="fixed inset-0 bg-black/40 flex justify-center items-center z-50 px-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md p-6">
            <div className="flex justify-between items-center mb-5 pb-4 border-b border-gray-200">
              <div>
                <h2 className="text-lg font-bold text-gray-900">Invoice</h2>
                <p className="text-xs text-gray-400">{invoiceData.invoiceNumber}</p>
              </div>
              <button onClick={closeInvoiceModal} className="text-gray-400 hover:text-gray-600 cursor-pointer p-1">
                <X className="w-5 h-5" />
              </button>
            </div>
            <div className="space-y-3 text-sm">
              <div className="flex justify-between"><span className="text-gray-500">Patient</span><span className="font-medium">{invoiceData.patientName}</span></div>
              <div className="flex justify-between"><span className="text-gray-500">Doctor</span><span className="font-medium">{invoiceData.providerName}</span></div>
              <div className="flex justify-between"><span className="text-gray-500">Specialization</span><span className="font-medium">{invoiceData.specialization}</span></div>
              <div className="flex justify-between"><span className="text-gray-500">Clinic</span><span className="font-medium">{invoiceData.clinicName}</span></div>
              <hr className="border-gray-200" />
              <div className="flex justify-between"><span className="text-gray-500">Date</span><span className="font-medium">{invoiceData.appointmentDate}</span></div>
              <div className="flex justify-between"><span className="text-gray-500">Time</span><span className="font-medium">{invoiceData.appointmentTime}</span></div>
              <div className="flex justify-between"><span className="text-gray-500">Payment Mode</span><span className="font-medium">{invoiceData.paymentMode}</span></div>
              <div className="flex justify-between"><span className="text-gray-500">Transaction ID</span><span className="font-mono text-xs">{invoiceData.transactionId || '—'}</span></div>
              <hr className="border-gray-200" />
              <div className="flex justify-between text-base">
                <span className="font-semibold text-gray-900">Total Amount</span>
                <span className="font-bold text-green-700">₹{invoiceData.amount}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Status</span>
                <span className={`inline-flex px-2 py-0.5 text-xs font-semibold rounded-full ${paymentStatusClasses[invoiceData.paymentStatus] || ''}`}>
                  {invoiceData.paymentStatus}
                </span>
              </div>
            </div>
            <div className="mt-6 flex justify-end">
              <button onClick={closeInvoiceModal} className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors cursor-pointer">
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Video Consultation Modal (Dummy) */}
      {isVideoOpen && (
        <div className="fixed inset-0 bg-black/80 flex flex-col justify-center items-center z-50 px-4">
          <div className="w-full max-w-4xl bg-gray-900 rounded-xl shadow-2xl overflow-hidden flex flex-col h-[80vh]">
            <div className="bg-gray-800 px-4 py-3 flex justify-between items-center border-b border-gray-700">
              <div className="flex items-center gap-2 text-white font-semibold">
                <Video className="w-5 h-5 text-purple-400" />
                Teleconsultation Room
              </div>
              <button onClick={() => setIsVideoOpen(false)} className="text-gray-400 hover:text-white cursor-pointer">
                <X className="w-5 h-5" />
              </button>
            </div>
            <div className="flex-1 flex items-center justify-center relative">
              <div className="text-center text-gray-500">
                <p className="text-lg">Waiting for host to start the meeting...</p>
                <p className="text-sm mt-2">(Jitsi IFrame Placeholder)</p>
              </div>
            </div>
            <div className="bg-gray-800 p-4 flex justify-center gap-4 border-t border-gray-700">
              <button className="px-6 py-2 bg-red-600 hover:bg-red-700 text-white rounded-full font-medium transition-colors cursor-pointer" onClick={() => setIsVideoOpen(false)}>
                Leave Call
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Review Modal (Dummy) */}
      {isReviewOpen && (
        <div className="fixed inset-0 bg-black/40 flex justify-center items-center z-50 px-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md p-6">
            <div className="flex justify-between items-center mb-5 pb-4 border-b border-gray-200">
              <h2 className="text-lg font-bold text-gray-900">Rate Provider</h2>
              <button onClick={() => setIsReviewOpen(false)} className="text-gray-400 hover:text-gray-600 cursor-pointer p-1">
                <X className="w-5 h-5" />
              </button>
            </div>
            <form onSubmit={submitReview} className="space-y-5">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Your Rating</label>
                <div className="flex gap-1">
                  {[1, 2, 3, 4, 5].map((star) => (
                    <button key={star} type="button" className="p-1 cursor-pointer focus:outline-none"
                      onClick={() => setReviewForm({ ...reviewForm, rating: star })}>
                      <Star className={`w-8 h-8 ${star <= reviewForm.rating ? 'fill-yellow-400 text-yellow-400' : 'text-gray-300 hover:text-yellow-200 transition-colors'}`} />
                    </button>
                  ))}
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Review</label>
                <textarea
                  value={reviewForm.comment}
                  onChange={(e) => setReviewForm({ ...reviewForm, comment: e.target.value })}
                  placeholder="Share your experience..."
                  className="w-full p-3 border border-gray-300 rounded-lg resize-none h-28 outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/10 transition-all"
                />
              </div>
              <button type="submit"
                className="w-full flex items-center justify-center gap-2 bg-blue-600 text-white px-4 py-2.5 rounded-lg hover:bg-blue-700 font-medium transition-colors cursor-pointer">
                Submit Review
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
