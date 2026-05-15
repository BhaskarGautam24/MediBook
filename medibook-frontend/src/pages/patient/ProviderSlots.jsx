import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../../services/api';
import toast from 'react-hot-toast';
import { useAuth } from '../../context/AuthContext';
import { Calendar, Clock, MapPin, User as UserIcon, Star, ArrowLeft, CheckCircle, CreditCard, Wallet, Building, Smartphone, Banknote, Loader2, ShieldCheck } from 'lucide-react';
import FullCalendar from '@fullcalendar/react';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';

// Payment mode options for the patient
const PAYMENT_MODES = [
  { id: 'UPI', label: 'UPI', icon: Smartphone, desc: 'Google Pay, PhonePe, etc.' },
  { id: 'CARD', label: 'Card', icon: CreditCard, desc: 'Debit / Credit Card' },
  { id: 'WALLET', label: 'Wallet', icon: Wallet, desc: 'Paytm, Amazon Pay' },
  { id: 'NETBANKING', label: 'Net Banking', icon: Building, desc: 'All major banks' },
];

const loadScript = (src) => {
  return new Promise((resolve) => {
    const script = document.createElement('script');
    script.src = src;
    script.onload = () => resolve(true);
    script.onerror = () => resolve(false);
    document.body.appendChild(script);
  });
};


export default function ProviderSlots() {
  const { isAuthenticated, user } = useAuth();
  const { providerId } = useParams();
  const navigate = useNavigate();
  
  const [provider, setProvider] = useState(null);
  const [slots, setSlots] = useState([]);
  const [loading, setLoading] = useState(true);
  const [bookingSlot, setBookingSlot] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Reviews state
  const [reviews, setReviews] = useState([]);
  const [ratingSummary, setRatingSummary] = useState({ averageRating: 0, totalReviews: 0 });

  // Payment flow state
  const [paymentStep, setPaymentStep] = useState('choose');
  const [paymentType, setPaymentType] = useState(null);
  const [selectedMode, setSelectedMode] = useState('UPI');

  useEffect(() => {
    loadProvider();
    loadSlots();
    loadReviews();
  }, [providerId]);

  const loadProvider = async () => {
    try {
      const data = await api.get(`/providers/${providerId}`);
      setProvider(data);
    } catch (err) {
      toast.error('Provider not found');
      navigate('/providers');
    }
  };

  const loadReviews = async () => {
    try {
      const [reviewPage, summary] = await Promise.all([
        api.get(`/reviews/provider/${providerId}?page=0&size=5`),
        api.get(`/reviews/provider/${providerId}/summary`),
      ]);
      setReviews(reviewPage.content || []);
      setRatingSummary(summary);
    } catch { /* reviews not critical */ }
  };

  const loadSlots = async () => {
    setLoading(true);
    try {
      const data = await api.get(`/providers/${providerId}/slots/all`);
      setSlots(data);
    } catch (err) {
      console.error('Failed to load slots:', err);
      toast.error('Failed to load available slots');
    } finally {
      setLoading(false);
    }
  };

  // Reset modal state when opening
  const openBookingModal = (slot) => {
    setBookingSlot(slot);
    setPaymentStep('choose');
    setPaymentType(null);
    setSelectedMode('UPI');
  };

  const closeModal = () => {
    setBookingSlot(null);
    setPaymentStep('choose');
    setPaymentType(null);
  };

  // ─── BOOK WITH ONLINE PAYMENT ───
  const handleOnlinePayment = async () => {
    if (!bookingSlot) return;
    setIsSubmitting(true);
    setPaymentStep('processing');

    try {
      // Step 1: Create appointment with online payment mode
      const bookResult = await api.post('/appointments', {
        slotId: bookingSlot.id,
        providerId: parseInt(providerId),
        paymentMode: selectedMode,
      });

      const paymentId = bookResult.paymentId;
      if (!paymentId) throw new Error('Payment creation failed');

      // Step 2 & 3: Gateway specific handling
      const gateway = bookResult.gateway;

      if (gateway === 'razorpay') {
        let isLoaded = !!window.Razorpay;
        if (!isLoaded) {
          isLoaded = await loadScript('https://checkout.razorpay.com/v1/checkout.js');
        }
        
        if (!isLoaded || !window.Razorpay) {
          toast.error('Razorpay SDK failed to load. Are you using an adblocker?');
          setPaymentStep('failed');
          setIsSubmitting(false);
          return;
        }

        let rzpSuccess = false;
        const options = {
          key: import.meta.env.VITE_RAZORPAY_KEY, // Set this in .env
          amount: consultationFee * 100, // in paise
          currency: 'INR',
          name: 'MediBook',
          description: `Consultation with Dr. ${provider.userName}`,
          order_id: bookResult.gatewayOrderId,
          handler: async function (response) {
            rzpSuccess = true;
            try {
              const confirmResult = await api.post(`/payments/${paymentId}/confirm`, {
                gatewayOrderId: response.razorpay_order_id,
                gatewayPaymentId: response.razorpay_payment_id,
                gatewaySignature: response.razorpay_signature,
              });
              if (confirmResult.success) {
                setPaymentStep('success');
                loadSlots();
              } else {
                setPaymentStep('failed');
              }
            } catch (err) {
              setPaymentStep('failed');
            }
          },
          prefill: {
            name: user?.name,
            email: user?.email,
          },
          theme: {
            color: '#2563eb',
          },
          modal: {
            ondismiss: async function() {
              if (rzpSuccess) return; // Don't cancel if already successful
              // User closed the modal without paying, cancel the payment
              setPaymentStep('failed');
              try {
                await api.post(`/payments/${paymentId}/confirm`, {});
              } catch(e) {}
            }
          }
        };

        const rzp = new window.Razorpay(options);
        rzp.on('payment.failed', function (response) {
          setPaymentStep('failed');
          api.post(`/payments/${paymentId}/confirm`, {}).catch(console.error);
        });
        rzp.open();
        // Keep processing state while modal is open
      } 
      else if (gateway === 'stripe') {
        // For Stripe, we would mount the Stripe Elements UI here.
        // Since it requires additional components, we'll simulate the successful confirmation.
        // In a full implementation, you would use stripe.confirmCardPayment(bookResult.clientSecret, { payment_method: ... })
        setPaymentStep('processing');
        await new Promise(resolve => setTimeout(resolve, 2000));
        
        const confirmResult = await api.post(`/payments/${paymentId}/confirm`, {
          gatewayOrderId: bookResult.gatewayOrderId,
          gatewayPaymentId: bookResult.gatewayOrderId, // using intent id
        });
        
        if (confirmResult.success) {
          setPaymentStep('success');
          loadSlots();
        } else {
          setPaymentStep('failed');
        }
      }
      else {
        // Mock Gateway Flow
        setPaymentStep('processing');
        await new Promise(resolve => setTimeout(resolve, 2000));
        
        const confirmResult = await api.post(`/payments/${paymentId}/confirm`, {
          gatewayOrderId: bookResult.gatewayOrderId,
          gatewayPaymentId: 'mock_pay_' + Date.now(),
          gatewaySignature: 'mock_sig_' + Date.now(),
        });
        
        if (confirmResult.success) {
          setPaymentStep('success');
          loadSlots();
        } else {
          setPaymentStep('failed');
        }
      }
    } catch (err) {
      toast.error(err.message || 'Payment failed');
      setPaymentStep('failed');
    } finally {
      setIsSubmitting(false);
    }
  };

  // ─── BOOK WITH PAY AT CLINIC ───
  const handlePayAtClinic = async () => {
    if (!bookingSlot) return;
    setIsSubmitting(true);

    try {
      await api.post('/appointments', {
        slotId: bookingSlot.id,
        providerId: parseInt(providerId),
        paymentMode: 'CASH',
      });
      setPaymentStep('success');
      loadSlots();
    } catch (err) {
      toast.error(err.message || 'Booking failed');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleEventClick = (info) => {
    const { isBooked, isBlocked, slotData } = info.event.extendedProps;
    if (isBooked || isBlocked) {
      toast.error('This slot is not available.');
      return;
    }
    if (!isAuthenticated) {
      toast.error('Please login to book an appointment');
      navigate('/login');
      return;
    }
    openBookingModal(slotData);
  };

  if (loading && !provider) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="flex flex-col items-center gap-3">
          <div className="w-8 h-8 border-3 border-blue-200 border-t-blue-600 rounded-full animate-spin" />
          <span className="text-sm text-gray-500">Loading doctor details...</span>
        </div>
      </div>
    );
  }

  const consultationFee = provider?.consultationFee || 500;

  const events = slots.map((slot) => {
    const isBooked = slot.booked || slot.isBooked;
    const isBlocked = slot.blocked || slot.isBlocked;
    let color = '#22c55e';
    let title = 'Available';
    let className = 'cursor-pointer hover:opacity-80 transition-opacity';
    if (isBooked) {
      color = '#ef4444';
      title = 'Booked';
      className = 'cursor-not-allowed opacity-50';
    } else if (isBlocked) {
      color = '#9ca3af';
      title = 'Blocked';
      className = 'cursor-not-allowed opacity-50';
    }
    return {
      id: slot.id,
      title,
      start: `${slot.date}T${slot.startTime}`,
      end: `${slot.date}T${slot.endTime}`,
      backgroundColor: color,
      borderColor: color,
      textColor: '#ffffff',
      className,
      extendedProps: { slotId: slot.id, isBooked, isBlocked, slotData: slot },
    };
  });

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Back Button */}
      <button 
        onClick={() => navigate('/providers')}
        className="flex items-center gap-2 text-gray-500 hover:text-gray-900 transition-colors mb-6 text-sm font-medium"
      >
        <ArrowLeft className="w-4 h-4" /> Back to Doctors
      </button>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        
        {/* Left Column: Provider Info */}
        <div className="space-y-6">
          {provider && (
            <div className="bg-white rounded-2xl shadow-sm border border-gray-200 overflow-hidden">
              <div className="bg-gradient-to-r from-blue-600 to-indigo-600 h-24"></div>
              <div className="px-6 pb-6 pt-0 relative">
                <div className="w-20 h-20 bg-white rounded-full flex items-center justify-center border-4 border-white shadow-md -mt-10 mb-4 mx-auto lg:mx-0 text-blue-600">
                  <UserIcon className="w-10 h-10" />
                </div>
                <div className="text-center lg:text-left">
                  <h1 className="text-2xl font-bold text-gray-900 mb-1">Dr. {provider.userName}</h1>
                  <p className="text-blue-600 font-medium text-sm mb-4">{provider.specialization}</p>
                  <div className="space-y-3">
                    <div className="flex items-center gap-3 text-sm text-gray-600 justify-center lg:justify-start">
                      <Star className="w-4 h-4 text-amber-500 fill-amber-500" />
                      <span>{provider.experienceYears} Years Experience</span>
                    </div>
                    {/* Rating display */}
                    <div className="flex items-center gap-2 text-sm justify-center lg:justify-start">
                      <div className="flex">
                        {[1,2,3,4,5].map(s => (
                          <Star key={s} className={`w-4 h-4 ${s <= Math.round(ratingSummary.averageRating || 0) ? 'fill-yellow-400 text-yellow-400' : 'text-gray-200'}`} />
                        ))}
                      </div>
                      <span className="font-medium text-gray-700">{ratingSummary.averageRating || '0.0'}</span>
                      <span className="text-gray-400">({ratingSummary.totalReviews || 0} reviews)</span>
                    </div>
                    <div className="flex items-center gap-3 text-sm text-gray-600 justify-center lg:justify-start">
                      <MapPin className="w-4 h-4 text-gray-400 shrink-0" />
                      <span className="line-clamp-2">{provider.clinicName}, {provider.clinicAddress}</span>
                    </div>
                    <div className="flex items-center gap-3 text-sm justify-center lg:justify-start">
                      <Banknote className="w-4 h-4 text-green-500" />
                      <span className="font-semibold text-green-700">₹{consultationFee} Consultation Fee</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          )}

          <div className="bg-blue-50 rounded-2xl p-6 border border-blue-100">
            <h3 className="font-semibold text-blue-900 mb-2">How to Book</h3>
            <ul className="space-y-2 text-sm text-blue-800">
              <li className="flex items-start gap-2">
                <span className="bg-blue-200 text-blue-700 rounded-full w-5 h-5 flex items-center justify-center shrink-0 mt-0.5 text-xs font-bold">1</span>
                Browse the calendar for available slots (green).
              </li>
              <li className="flex items-start gap-2">
                <span className="bg-blue-200 text-blue-700 rounded-full w-5 h-5 flex items-center justify-center shrink-0 mt-0.5 text-xs font-bold">2</span>
                Click on a time that works for you.
              </li>
              <li className="flex items-start gap-2">
                <span className="bg-blue-200 text-blue-700 rounded-full w-5 h-5 flex items-center justify-center shrink-0 mt-0.5 text-xs font-bold">3</span>
                Choose "Pay Online" or "Pay at Clinic".
              </li>
            </ul>
          </div>

          {/* Patient Reviews Section */}
          {reviews.length > 0 && (
            <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-6">
              <h3 className="font-semibold text-gray-900 mb-4 flex items-center gap-2">
                <Star className="w-4 h-4 text-yellow-500 fill-yellow-500" />
                Patient Reviews ({ratingSummary.totalReviews})
              </h3>
              <div className="space-y-4">
                {reviews.map((r) => (
                  <div key={r.id} className="border-b border-gray-100 pb-3 last:border-0 last:pb-0">
                    <div className="flex items-center gap-2 mb-1">
                      <div className="flex">
                        {[1,2,3,4,5].map(s => (
                          <Star key={s} className={`w-3 h-3 ${s <= r.rating ? 'fill-yellow-400 text-yellow-400' : 'text-gray-200'}`} />
                        ))}
                      </div>
                      {r.isVerified && <ShieldCheck className="w-3.5 h-3.5 text-green-500" />}
                    </div>
                    {r.comment && <p className="text-sm text-gray-600 mb-1">{r.comment}</p>}
                    <p className="text-xs text-gray-400">
                      {r.patientName} • {new Date(r.createdAt).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' })}
                    </p>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Right Column: Calendar */}
        <div className="lg:col-span-2">
          <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-5">
            <div className="flex items-center justify-between mb-4 pb-4 border-b border-gray-100">
              <h2 className="text-lg font-bold text-gray-900">Available Slots</h2>
              <div className="flex items-center gap-4 text-xs font-medium">
                <div className="flex items-center gap-1.5 text-green-700 bg-green-50 px-2 py-1 rounded-md">
                  <span className="w-2 h-2 rounded-full bg-green-500" /> Available
                </div>
              </div>
            </div>
            <div className="patient-calendar">
              {loading ? (
                <div className="flex justify-center py-20"><div className="spinner" /></div>
              ) : (
                <FullCalendar
                  plugins={[dayGridPlugin, timeGridPlugin, interactionPlugin]}
                  initialView="timeGridWeek"
                  headerToolbar={{ left: 'prev,next today', center: 'title', right: 'dayGridMonth,timeGridWeek' }}
                  slotMinTime="08:00:00"
                  slotMaxTime="20:00:00"
                  allDaySlot={false}
                  events={events}
                  eventClick={handleEventClick}
                  height="600px"
                  nowIndicator={true}
                  slotDuration="00:15:00"
                  expandRows={true}
                  stickyHeaderDates={true}
                  eventMinHeight={30}
                />
              )}
            </div>
          </div>
        </div>
      </div>

      {/* ═══════════════════════════════════════════════════════════ */}
      {/* BOOKING + PAYMENT MODAL                                    */}
      {/* ═══════════════════════════════════════════════════════════ */}
      {bookingSlot && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm px-4"
             onClick={paymentStep === 'processing' ? undefined : closeModal}>
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-md overflow-hidden animate-in"
               onClick={e => e.stopPropagation()}>

            {/* ─── STEP: Choose Payment ─── */}
            {paymentStep === 'choose' && (
              <>
                <div className="bg-blue-600 p-6 text-center">
                  <div className="w-16 h-16 bg-white/20 rounded-full flex items-center justify-center mx-auto mb-3 text-white">
                    <Calendar className="w-8 h-8" />
                  </div>
                  <h3 className="text-xl font-bold text-white">Book Appointment</h3>
                  <p className="text-blue-100 mt-1 text-sm">Review & choose payment method</p>
                </div>
                <div className="p-6">
                  {/* Appointment Details */}
                  <div className="bg-gray-50 rounded-xl p-4 space-y-3 mb-5">
                    <div className="flex items-center justify-between pb-3 border-b border-gray-200">
                      <span className="text-gray-500 text-sm">Doctor</span>
                      <span className="font-semibold text-gray-900">Dr. {provider?.userName}</span>
                    </div>
                    <div className="flex items-center justify-between pb-3 border-b border-gray-200">
                      <span className="text-gray-500 text-sm">Date</span>
                      <span className="font-semibold text-gray-900">{bookingSlot.date}</span>
                    </div>
                    <div className="flex items-center justify-between pb-3 border-b border-gray-200">
                      <span className="text-gray-500 text-sm">Time</span>
                      <span className="font-semibold text-gray-900">
                        {bookingSlot.startTime.substring(0,5)} - {bookingSlot.endTime.substring(0,5)}
                      </span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-gray-500 text-sm">Consultation Fee</span>
                      <span className="font-bold text-green-700 text-lg">₹{consultationFee}</span>
                    </div>
                  </div>

                  {/* Payment Type Selection */}
                  {!paymentType && (
                    <div className="space-y-3">
                      <p className="text-sm font-semibold text-gray-700 mb-2">How would you like to pay?</p>
                      <button
                        onClick={() => setPaymentType('online')}
                        className="w-full flex items-center gap-4 p-4 border-2 border-gray-200 rounded-xl hover:border-blue-400 hover:bg-blue-50/50 transition-all cursor-pointer group"
                      >
                        <div className="p-2 bg-blue-100 rounded-lg group-hover:bg-blue-200 transition-colors">
                          <CreditCard className="w-5 h-5 text-blue-600" />
                        </div>
                        <div className="text-left">
                          <div className="font-semibold text-gray-900">Pay Online</div>
                          <div className="text-xs text-gray-500">UPI, Card, Wallet, Net Banking</div>
                        </div>
                      </button>
                      <button
                        onClick={handlePayAtClinic}
                        disabled={isSubmitting}
                        className="w-full flex items-center gap-4 p-4 border-2 border-gray-200 rounded-xl hover:border-green-400 hover:bg-green-50/50 transition-all cursor-pointer group"
                      >
                        <div className="p-2 bg-green-100 rounded-lg group-hover:bg-green-200 transition-colors">
                          <Banknote className="w-5 h-5 text-green-600" />
                        </div>
                        <div className="text-left">
                          <div className="font-semibold text-gray-900">Pay at Clinic</div>
                          <div className="text-xs text-gray-500">Cash payment after consultation</div>
                        </div>
                      </button>
                      <button onClick={closeModal}
                        className="w-full mt-2 px-4 py-2 text-sm text-gray-500 hover:text-gray-700 transition-colors">
                        Cancel
                      </button>
                    </div>
                  )}

                  {/* Online Payment Mode Selection */}
                  {paymentType === 'online' && (
                    <div className="space-y-3">
                      <p className="text-sm font-semibold text-gray-700 mb-2">Select Payment Mode</p>
                      <div className="grid grid-cols-2 gap-2">
                        {PAYMENT_MODES.map((mode) => {
                          const Icon = mode.icon;
                          return (
                            <button
                              key={mode.id}
                              onClick={() => setSelectedMode(mode.id)}
                              className={`flex flex-col items-center gap-1.5 p-3 rounded-xl border-2 transition-all cursor-pointer ${
                                selectedMode === mode.id
                                  ? 'border-blue-500 bg-blue-50 shadow-sm'
                                  : 'border-gray-200 hover:border-gray-300'
                              }`}
                            >
                              <Icon className={`w-5 h-5 ${selectedMode === mode.id ? 'text-blue-600' : 'text-gray-400'}`} />
                              <span className={`text-xs font-semibold ${selectedMode === mode.id ? 'text-blue-700' : 'text-gray-600'}`}>
                                {mode.label}
                              </span>
                              <span className="text-[10px] text-gray-400">{mode.desc}</span>
                            </button>
                          );
                        })}
                      </div>
                      <div className="flex gap-3 mt-4">
                        <button onClick={() => setPaymentType(null)}
                          className="flex-1 px-4 py-2.5 border border-gray-300 text-gray-700 font-medium rounded-xl hover:bg-gray-50 transition-colors">
                          Back
                        </button>
                        <button
                          onClick={handleOnlinePayment}
                          disabled={isSubmitting}
                          className="flex-1 px-4 py-2.5 bg-blue-600 text-white font-medium rounded-xl hover:bg-blue-700 transition-colors flex items-center justify-center gap-2"
                        >
                          <ShieldCheck className="w-4 h-4" /> Pay ₹{consultationFee}
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              </>
            )}

            {/* ─── STEP: Processing Payment ─── */}
            {paymentStep === 'processing' && (
              <div className="p-10 text-center">
                <div className="w-20 h-20 mx-auto mb-6 bg-blue-50 rounded-full flex items-center justify-center">
                  <Loader2 className="w-10 h-10 text-blue-600 animate-spin" />
                </div>
                <h3 className="text-lg font-bold text-gray-900 mb-2">Processing Payment</h3>
                <p className="text-sm text-gray-500 mb-4">
                  Connecting to payment gateway...
                </p>
                <div className="bg-gray-100 rounded-lg p-3 text-sm text-gray-600">
                  <div className="flex justify-between mb-1">
                    <span>Amount</span>
                    <span className="font-semibold">₹{consultationFee}</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Mode</span>
                    <span className="font-semibold">{selectedMode}</span>
                  </div>
                </div>
                <div className="mt-4 flex items-center justify-center gap-2 text-xs text-gray-400">
                  <ShieldCheck className="w-3.5 h-3.5" />
                  Secured by Mock Payment Gateway
                </div>
              </div>
            )}

            {/* ─── STEP: Payment Success ─── */}
            {paymentStep === 'success' && (
              <div className="p-10 text-center">
                <div className="w-20 h-20 mx-auto mb-6 bg-green-50 rounded-full flex items-center justify-center">
                  <CheckCircle className="w-12 h-12 text-green-500" />
                </div>
                <h3 className="text-lg font-bold text-gray-900 mb-2">
                  {paymentType === 'online' ? 'Payment Successful!' : 'Appointment Booked!'}
                </h3>
                <p className="text-sm text-gray-500 mb-6">
                  {paymentType === 'online'
                    ? `₹${consultationFee} paid via ${selectedMode}. Your appointment is pending doctor approval.`
                    : 'Your appointment is pending doctor approval. Pay at the clinic after consultation.'}
                </p>
                <div className="flex gap-3">
                  <button onClick={closeModal}
                    className="flex-1 px-4 py-2.5 border border-gray-300 text-gray-700 font-medium rounded-xl hover:bg-gray-50 transition-colors">
                    Close
                  </button>
                  <button onClick={() => navigate('/patient/appointments')}
                    className="flex-1 px-4 py-2.5 bg-blue-600 text-white font-medium rounded-xl hover:bg-blue-700 transition-colors">
                    View Appointments
                  </button>
                </div>
              </div>
            )}

            {/* ─── STEP: Payment Failed ─── */}
            {paymentStep === 'failed' && (
              <div className="p-10 text-center">
                <div className="w-20 h-20 mx-auto mb-6 bg-red-50 rounded-full flex items-center justify-center">
                  <span className="text-4xl">✕</span>
                </div>
                <h3 className="text-lg font-bold text-gray-900 mb-2">Payment Failed</h3>
                <p className="text-sm text-gray-500 mb-6">
                  Something went wrong. Your slot has been released. Please try again.
                </p>
                <div className="flex gap-3">
                  <button onClick={closeModal}
                    className="flex-1 px-4 py-2.5 border border-gray-300 text-gray-700 font-medium rounded-xl hover:bg-gray-50 transition-colors">
                    Close
                  </button>
                  <button onClick={() => { setPaymentStep('choose'); setPaymentType(null); }}
                    className="flex-1 px-4 py-2.5 bg-blue-600 text-white font-medium rounded-xl hover:bg-blue-700 transition-colors">
                    Try Again
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
