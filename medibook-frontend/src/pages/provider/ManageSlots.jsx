import { useState, useEffect, useRef } from 'react';
import api from '../../services/api';
import toast from 'react-hot-toast';
import {
  Plus, Trash2, Lock, Unlock, Calendar, Clock, Layers,
  RefreshCw, ChevronDown, X, Copy, Repeat
} from 'lucide-react';
import FullCalendar from '@fullcalendar/react';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';

const TABS = [
  { id: 'single', label: 'Single Slot', icon: Plus },
  { id: 'bulk', label: 'Bulk Generate', icon: Copy },
  { id: 'recurring', label: 'Recurring', icon: Repeat },
];

const DURATIONS = [15, 20, 30, 45, 60, 90];

export default function ManageSlots() {
  const [slots, setSlots] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('single');
  const [showModal, setShowModal] = useState(false);
  const [selectedSlot, setSelectedSlot] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const calendarRef = useRef(null);

  // ── Single Slot Form ──
  const [singleForm, setSingleForm] = useState({
    date: '', startTime: '', endTime: '',
  });

  // ── Bulk Form ──
  const [bulkForm, setBulkForm] = useState({
    startDate: '', endDate: '', windowStart: '09:00',
    windowEnd: '17:00', durationMinutes: 30, bufferMinutes: 0,
  });

  // ── Recurring Form ──
  const [recurringForm, setRecurringForm] = useState({
    recurrenceType: 'DAILY', startDate: '', endDate: '',
    startTime: '09:00', endTime: '09:30',
  });

  // ── Bulk Preview ──
  const [bulkPreview, setBulkPreview] = useState(null);

  useEffect(() => { loadSlots(); }, []);

  const loadSlots = async () => {
    try {
      const data = await api.get('/slots/my');
      setSlots(data);
    } catch (err) {
      console.error('Failed to load slots:', err);
    } finally {
      setLoading(false);
    }
  };

  // ────────────────────────────────────────────────────────
  // Slot Creation Handlers
  // ────────────────────────────────────────────────────────

  const handleSingleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await api.post('/slots', singleForm);
      toast.success('Slot created successfully');
      setSingleForm({ date: '', startTime: '', endTime: '' });
      setShowModal(false);
      loadSlots();
    } catch (err) {
      toast.error(err.message || 'Failed to create slot');
    } finally {
      setSubmitting(false);
    }
  };

  const handleBulkSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      const result = await api.post('/slots/bulk', bulkForm);
      toast.success(`${result.created} slots created (${result.skipped} skipped)`);
      setBulkPreview(null);
      setShowModal(false);
      loadSlots();
    } catch (err) {
      toast.error(err.message || 'Failed to create bulk slots');
    } finally {
      setSubmitting(false);
    }
  };

  const handleRecurringSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      const result = await api.post('/slots/recurring', recurringForm);
      toast.success(`${result.created} recurring slots created`);
      setShowModal(false);
      loadSlots();
    } catch (err) {
      toast.error(err.message || 'Failed to create recurring slots');
    } finally {
      setSubmitting(false);
    }
  };

  // ── Generate preview for bulk ──
  const generatePreview = () => {
    const { startDate, endDate, windowStart, windowEnd, durationMinutes, bufferMinutes } = bulkForm;
    if (!startDate || !endDate || !windowStart || !windowEnd || !durationMinutes) return;

    const sd = new Date(startDate);
    const ed = new Date(endDate);
    const days = Math.max(1, Math.ceil((ed - sd) / (1000 * 60 * 60 * 24)) + 1);

    const ws = windowStart.split(':').map(Number);
    const we = windowEnd.split(':').map(Number);
    const totalMinutes = (we[0] * 60 + we[1]) - (ws[0] * 60 + ws[1]);
    const slotsPerDay = Math.floor(totalMinutes / (durationMinutes + (bufferMinutes || 0)));

    setBulkPreview({ days, slotsPerDay, total: days * slotsPerDay });
  };

  useEffect(() => {
    if (activeTab === 'bulk') generatePreview();
  }, [bulkForm]);

  // ────────────────────────────────────────────────────────
  // Slot Actions
  // ────────────────────────────────────────────────────────

  const handleBlockToggle = async (slot) => {
    const action = slot.blocked ? 'unblock' : 'block';
    try {
      await api.put(`/slots/${slot.id}/${action}`);
      toast.success(`Slot ${action}ed`);
      loadSlots();
      setSelectedSlot(null);
    } catch (err) {
      toast.error(err.message || `Failed to ${action} slot`);
    }
  };

  const handleDelete = async (slot) => {
    const msg = slot.booked
      ? 'This slot is BOOKED. Deleting will cancel the appointment and notify the patient. Proceed?'
      : 'Delete this slot?';
    if (!window.confirm(msg)) return;
    try {
      await api.delete(`/slots/${slot.id}`);
      toast.success(slot.booked ? 'Slot deleted & patient notified' : 'Slot deleted');
      loadSlots();
      setSelectedSlot(null);
    } catch (err) {
      toast.error(err.message || 'Failed to delete slot');
    }
  };

  const handleEventClick = (info) => {
    const props = info.event.extendedProps;
    setSelectedSlot({
      id: props.slotId,
      booked: props.isBooked,
      blocked: props.isBlocked,
      date: props.slotDate,
      startTime: props.slotStartTime,
      endTime: props.slotEndTime,
    });
  };

  const handleDateClick = (info) => {
    setSingleForm({
      date: info.dateStr.split('T')[0],
      startTime: info.dateStr.includes('T')
        ? info.dateStr.split('T')[1].substring(0, 5) : '09:00',
      endTime: '',
    });
    setActiveTab('single');
    setShowModal(true);
  };

  // ────────────────────────────────────────────────────────
  // Calendar Events Mapping
  // ────────────────────────────────────────────────────────

  const events = slots.map((slot) => {
    const isBooked = slot.booked || slot.isBooked;
    const isBlocked = slot.blocked || slot.isBlocked;

    let color = '#10b981'; // green — available
    let title = '✓ Available';
    if (isBooked) {
      color = '#ef4444'; // red — booked
      title = '● Booked';
    } else if (isBlocked) {
      color = '#6b7280'; // gray — blocked
      title = '⊘ Blocked';
    }

    return {
      id: slot.id,
      title,
      start: `${slot.date}T${slot.startTime}`,
      end: `${slot.date}T${slot.endTime}`,
      backgroundColor: color,
      borderColor: color,
      textColor: '#fff',
      extendedProps: {
        slotId: slot.id,
        isBooked,
        isBlocked,
        slotDate: slot.date,
        slotStartTime: slot.startTime,
        slotEndTime: slot.endTime,
      },
    };
  });

  // ────────────────────────────────────────────────────────
  // Stats
  // ────────────────────────────────────────────────────────

  const totalSlots = slots.length;
  const bookedSlots = slots.filter(s => s.booked || s.isBooked).length;
  const blockedSlots = slots.filter(s => s.blocked || s.isBlocked).length;
  const availableSlots = totalSlots - bookedSlots - blockedSlots;

  const stats = [
    { label: 'Total', value: totalSlots, color: 'bg-blue-50 text-blue-700 border-blue-200' },
    { label: 'Available', value: availableSlots, color: 'bg-emerald-50 text-emerald-700 border-emerald-200' },
    { label: 'Booked', value: bookedSlots, color: 'bg-red-50 text-red-700 border-red-200' },
    { label: 'Blocked', value: blockedSlots, color: 'bg-gray-50 text-gray-600 border-gray-200' },
  ];

  // ────────────────────────────────────────────────────────
  // Input Style Helper
  // ────────────────────────────────────────────────────────

  const inputClass = 'w-full px-3.5 py-2.5 text-sm border border-gray-300 rounded-lg bg-white text-gray-900 outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/10 transition-all placeholder:text-gray-400';
  const labelClass = 'block text-sm font-medium text-gray-700 mb-1.5';
  const btnPrimary = 'inline-flex items-center justify-center gap-2 px-5 py-2.5 text-sm font-medium text-white bg-indigo-600 rounded-lg hover:bg-indigo-700 active:bg-indigo-800 transition-all disabled:opacity-50 shadow-sm shadow-indigo-200 cursor-pointer';
  const btnSecondary = 'inline-flex items-center justify-center gap-2 px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-all cursor-pointer';

  if (loading) {
    return (
      <div className="flex items-center justify-center py-24">
        <div className="flex flex-col items-center gap-3">
          <div className="w-8 h-8 border-3 border-indigo-200 border-t-indigo-600 rounded-full animate-spin" />
          <span className="text-sm text-gray-500">Loading slots…</span>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* ── Header ── */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 mb-1 flex items-center gap-2">
            <Calendar className="w-6 h-6 text-indigo-600" />
            Slot Management
          </h1>
          <p className="text-sm text-gray-500">Create, manage, and control your availability slots</p>
        </div>
        <div className="flex items-center gap-3">
          <button onClick={loadSlots} className={btnSecondary} title="Refresh">
            <RefreshCw className="w-4 h-4" /> Refresh
          </button>
          <button onClick={() => setShowModal(true)} className={btnPrimary}>
            <Plus className="w-4 h-4" /> Create Slots
          </button>
        </div>
      </div>

      {/* ── Stats Cards ── */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        {stats.map((s) => (
          <div key={s.label} className={`border rounded-xl px-4 py-3.5 ${s.color}`}>
            <div className="text-2xl font-bold">{s.value}</div>
            <div className="text-xs font-medium mt-0.5 opacity-80">{s.label} Slots</div>
          </div>
        ))}
      </div>

      {/* ── Calendar ── */}
      <div className="bg-white border border-gray-200 rounded-xl shadow-sm overflow-hidden">
        <div className="p-4 border-b border-gray-100 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <div className="flex items-center gap-1.5 text-xs">
              <span className="w-3 h-3 rounded-sm bg-emerald-500" /> Available
            </div>
            <div className="flex items-center gap-1.5 text-xs">
              <span className="w-3 h-3 rounded-sm bg-red-500" /> Booked
            </div>
            <div className="flex items-center gap-1.5 text-xs">
              <span className="w-3 h-3 rounded-sm bg-gray-500" /> Blocked
            </div>
          </div>
          <span className="text-xs text-gray-400">Click a slot to manage • Click a time to create</span>
        </div>
        <div className="p-4 calendar-container">
          <FullCalendar
            ref={calendarRef}
            plugins={[dayGridPlugin, timeGridPlugin, interactionPlugin]}
            initialView="timeGridWeek"
            headerToolbar={{
              left: 'prev,next today',
              center: 'title',
              right: 'dayGridMonth,timeGridWeek,timeGridDay',
            }}
            slotMinTime="06:00:00"
            slotMaxTime="22:00:00"
            allDaySlot={false}
            events={events}
            eventClick={handleEventClick}
            dateClick={handleDateClick}
            height="640px"
            nowIndicator={true}
            slotDuration="00:15:00"
            eventDisplay="block"
            dayMaxEvents={3}
          />
        </div>
      </div>

      {/* ── Slot Action Popup ── */}
      {selectedSlot && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm"
          onClick={() => setSelectedSlot(null)}>
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-sm p-6 animate-in"
            onClick={(e) => e.stopPropagation()}>
            <div className="flex items-center justify-between mb-5">
              <h3 className="text-lg font-semibold text-gray-900">Slot Actions</h3>
              <button onClick={() => setSelectedSlot(null)}
                className="p-1 hover:bg-gray-100 rounded-lg transition-colors cursor-pointer">
                <X className="w-5 h-5 text-gray-400" />
              </button>
            </div>

            <div className="bg-gray-50 rounded-xl p-4 mb-5 space-y-1.5">
              <div className="flex items-center gap-2 text-sm">
                <Calendar className="w-4 h-4 text-gray-400" />
                <span className="text-gray-900 font-medium">{selectedSlot.date}</span>
              </div>
              <div className="flex items-center gap-2 text-sm">
                <Clock className="w-4 h-4 text-gray-400" />
                <span className="text-gray-600">{selectedSlot.startTime} – {selectedSlot.endTime}</span>
              </div>
              <div className="flex items-center gap-2 text-sm">
                <span className={`w-2 h-2 rounded-full ${
                  selectedSlot.booked ? 'bg-red-500' : selectedSlot.blocked ? 'bg-gray-500' : 'bg-emerald-500'
                }`} />
                <span className="text-gray-600">
                  {selectedSlot.booked ? 'Booked' : selectedSlot.blocked ? 'Blocked' : 'Available'}
                </span>
              </div>
            </div>

            <div className="space-y-2.5">
              {!selectedSlot.booked && (
                <button onClick={() => handleBlockToggle(selectedSlot)}
                  className="w-full flex items-center gap-3 px-4 py-3 text-sm font-medium rounded-xl border border-gray-200 hover:bg-gray-50 transition-all cursor-pointer">
                  {selectedSlot.blocked ? (
                    <><Unlock className="w-4 h-4 text-emerald-600" /> <span>Unblock Slot</span></>
                  ) : (
                    <><Lock className="w-4 h-4 text-amber-600" /> <span>Block Slot</span></>
                  )}
                </button>
              )}
              <button onClick={() => handleDelete(selectedSlot)}
                className="w-full flex items-center gap-3 px-4 py-3 text-sm font-medium rounded-xl border border-red-200 text-red-600 hover:bg-red-50 transition-all cursor-pointer">
                <Trash2 className="w-4 h-4" /> Delete Slot
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── Create Slot Modal ── */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm"
          onClick={() => setShowModal(false)}>
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-lg max-h-[90vh] overflow-y-auto p-6 animate-in"
            onClick={(e) => e.stopPropagation()}>
            {/* Modal Header */}
            <div className="flex items-center justify-between mb-5">
              <div>
                <h3 className="text-lg font-semibold text-gray-900">Create Slots</h3>
                <p className="text-xs text-gray-500 mt-0.5">Add availability for your patients</p>
              </div>
              <button onClick={() => setShowModal(false)}
                className="p-1 hover:bg-gray-100 rounded-lg transition-colors cursor-pointer">
                <X className="w-5 h-5 text-gray-400" />
              </button>
            </div>

            {/* Tabs */}
            <div className="flex border border-gray-200 rounded-xl p-1 mb-6 bg-gray-50">
              {TABS.map((tab) => {
                const Icon = tab.icon;
                return (
                  <button key={tab.id}
                    onClick={() => setActiveTab(tab.id)}
                    className={`flex-1 flex items-center justify-center gap-1.5 py-2 text-xs font-medium rounded-lg transition-all cursor-pointer ${
                      activeTab === tab.id
                        ? 'bg-white text-indigo-700 shadow-sm border border-gray-200'
                        : 'text-gray-500 hover:text-gray-700'
                    }`}>
                    <Icon className="w-3.5 h-3.5" /> {tab.label}
                  </button>
                );
              })}
            </div>

            {/* ── Single Slot Form ── */}
            {activeTab === 'single' && (
              <form onSubmit={handleSingleSubmit} className="space-y-4">
                <div>
                  <label className={labelClass}>Date</label>
                  <input type="date" className={inputClass} value={singleForm.date}
                    min={new Date().toISOString().split('T')[0]}
                    onChange={(e) => setSingleForm({ ...singleForm, date: e.target.value })}
                    required />
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className={labelClass}>Start Time</label>
                    <input type="time" className={inputClass} value={singleForm.startTime}
                      onChange={(e) => setSingleForm({ ...singleForm, startTime: e.target.value })}
                      required />
                  </div>
                  <div>
                    <label className={labelClass}>End Time</label>
                    <input type="time" className={inputClass} value={singleForm.endTime}
                      onChange={(e) => setSingleForm({ ...singleForm, endTime: e.target.value })}
                      required />
                  </div>
                </div>
                <button type="submit" className={`${btnPrimary} w-full mt-2`} disabled={submitting}>
                  <Plus className="w-4 h-4" /> {submitting ? 'Creating…' : 'Create Slot'}
                </button>
              </form>
            )}

            {/* ── Bulk Form ── */}
            {activeTab === 'bulk' && (
              <form onSubmit={handleBulkSubmit} className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className={labelClass}>Start Date</label>
                    <input type="date" className={inputClass} value={bulkForm.startDate}
                      min={new Date().toISOString().split('T')[0]}
                      onChange={(e) => setBulkForm({ ...bulkForm, startDate: e.target.value })}
                      required />
                  </div>
                  <div>
                    <label className={labelClass}>End Date</label>
                    <input type="date" className={inputClass} value={bulkForm.endDate}
                      onChange={(e) => setBulkForm({ ...bulkForm, endDate: e.target.value })}
                      required />
                  </div>
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className={labelClass}>Window Start</label>
                    <input type="time" className={inputClass} value={bulkForm.windowStart}
                      onChange={(e) => setBulkForm({ ...bulkForm, windowStart: e.target.value })}
                      required />
                  </div>
                  <div>
                    <label className={labelClass}>Window End</label>
                    <input type="time" className={inputClass} value={bulkForm.windowEnd}
                      onChange={(e) => setBulkForm({ ...bulkForm, windowEnd: e.target.value })}
                      required />
                  </div>
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className={labelClass}>Slot Duration</label>
                    <select className={inputClass} value={bulkForm.durationMinutes}
                      onChange={(e) => setBulkForm({ ...bulkForm, durationMinutes: parseInt(e.target.value) })}>
                      {DURATIONS.map((d) => (
                        <option key={d} value={d}>{d} minutes</option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className={labelClass}>Buffer Between</label>
                    <select className={inputClass} value={bulkForm.bufferMinutes}
                      onChange={(e) => setBulkForm({ ...bulkForm, bufferMinutes: parseInt(e.target.value) })}>
                      <option value={0}>No buffer</option>
                      <option value={5}>5 min</option>
                      <option value={10}>10 min</option>
                      <option value={15}>15 min</option>
                    </select>
                  </div>
                </div>

                {/* Preview */}
                {bulkPreview && (
                  <div className="bg-indigo-50 border border-indigo-200 rounded-xl p-4">
                    <h4 className="text-xs font-semibold text-indigo-700 uppercase tracking-wider mb-2">Preview</h4>
                    <div className="grid grid-cols-3 gap-3 text-center">
                      <div>
                        <div className="text-xl font-bold text-indigo-900">{bulkPreview.days}</div>
                        <div className="text-xs text-indigo-600">Days</div>
                      </div>
                      <div>
                        <div className="text-xl font-bold text-indigo-900">{bulkPreview.slotsPerDay}</div>
                        <div className="text-xs text-indigo-600">Slots/Day</div>
                      </div>
                      <div>
                        <div className="text-xl font-bold text-indigo-900">{bulkPreview.total}</div>
                        <div className="text-xs text-indigo-600">Total</div>
                      </div>
                    </div>
                  </div>
                )}

                <button type="submit" className={`${btnPrimary} w-full`} disabled={submitting}>
                  <Layers className="w-4 h-4" /> {submitting ? 'Generating…' : 'Generate Slots'}
                </button>
              </form>
            )}

            {/* ── Recurring Form ── */}
            {activeTab === 'recurring' && (
              <form onSubmit={handleRecurringSubmit} className="space-y-4">
                <div>
                  <label className={labelClass}>Recurrence Pattern</label>
                  <div className="grid grid-cols-2 gap-3">
                    {['DAILY', 'WEEKLY'].map((type) => (
                      <button key={type} type="button"
                        onClick={() => setRecurringForm({ ...recurringForm, recurrenceType: type })}
                        className={`py-2.5 px-4 rounded-xl text-sm font-medium border transition-all cursor-pointer ${
                          recurringForm.recurrenceType === type
                            ? 'border-indigo-500 bg-indigo-50 text-indigo-700'
                            : 'border-gray-200 text-gray-600 hover:border-gray-300'
                        }`}>
                        {type === 'DAILY' ? '📅 Daily' : '🗓️ Weekly'}
                      </button>
                    ))}
                  </div>
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className={labelClass}>From Date</label>
                    <input type="date" className={inputClass} value={recurringForm.startDate}
                      min={new Date().toISOString().split('T')[0]}
                      onChange={(e) => setRecurringForm({ ...recurringForm, startDate: e.target.value })}
                      required />
                  </div>
                  <div>
                    <label className={labelClass}>Until Date</label>
                    <input type="date" className={inputClass} value={recurringForm.endDate}
                      onChange={(e) => setRecurringForm({ ...recurringForm, endDate: e.target.value })}
                      required />
                  </div>
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className={labelClass}>Start Time</label>
                    <input type="time" className={inputClass} value={recurringForm.startTime}
                      onChange={(e) => setRecurringForm({ ...recurringForm, startTime: e.target.value })}
                      required />
                  </div>
                  <div>
                    <label className={labelClass}>End Time</label>
                    <input type="time" className={inputClass} value={recurringForm.endTime}
                      onChange={(e) => setRecurringForm({ ...recurringForm, endTime: e.target.value })}
                      required />
                  </div>
                </div>
                {recurringForm.recurrenceType === 'WEEKLY' && recurringForm.startDate && (
                  <div className="bg-amber-50 border border-amber-200 rounded-xl px-4 py-3 text-xs text-amber-800">
                    ℹ️ Slots will be created every <strong>
                      {new Date(recurringForm.startDate + 'T00:00:00').toLocaleDateString('en-US', { weekday: 'long' })}
                    </strong> from {recurringForm.startDate} to {recurringForm.endDate || '…'}
                  </div>
                )}
                <button type="submit" className={`${btnPrimary} w-full`} disabled={submitting}>
                  <Repeat className="w-4 h-4" /> {submitting ? 'Creating…' : 'Create Recurring Slots'}
                </button>
              </form>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
