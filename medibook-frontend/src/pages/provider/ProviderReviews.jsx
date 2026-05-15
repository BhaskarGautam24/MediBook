import { useState, useEffect } from 'react';
import api from '../../services/api';
import toast from 'react-hot-toast';
import { Star, Flag, MessageSquare, TrendingUp } from 'lucide-react';

/**
 * ProviderReviews — Provider dashboard page showing all reviews.
 * Providers can view reviews and flag inappropriate ones for admin moderation.
 */
export default function ProviderReviews() {
  const [reviews, setReviews] = useState([]);
  const [summary, setSummary] = useState({ averageRating: 0, totalReviews: 0 });
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  // Get provider ID from user context
  const user = JSON.parse(localStorage.getItem('medibook_user') || '{}');

  useEffect(() => {
    loadData();
  }, [page]);

  const loadData = async () => {
    try {
      // Get provider stats to find provider ID
      const stats = await api.get('/providers/me/stats');
      const providerList = await api.get('/providers');
      const myProvider = providerList.find(p => p.userId === user.id);
      if (!myProvider) return;

      // Load reviews and summary
      const [reviewPage, ratingSummary] = await Promise.all([
        api.get(`/reviews/provider/${myProvider.id}?page=${page}&size=10`),
        api.get(`/reviews/provider/${myProvider.id}/summary`),
      ]);

      setReviews(reviewPage.content || []);
      setTotalPages(reviewPage.totalPages || 0);
      setSummary(ratingSummary);
    } catch (err) {
      console.error('Failed to load reviews:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleFlag = async (reviewId) => {
    if (!window.confirm('Flag this review for admin moderation?')) return;
    try {
      await api.put(`/reviews/${reviewId}/flag`);
      toast.success('Review flagged for moderation');
      loadData();
    } catch (err) {
      toast.error(err.message || 'Failed to flag review');
    }
  };

  // Render star icons
  const renderStars = (rating) => (
    <div className="flex gap-0.5">
      {[1, 2, 3, 4, 5].map((s) => (
        <Star key={s} className={`w-4 h-4 ${s <= rating ? 'fill-yellow-400 text-yellow-400' : 'text-gray-200'}`} />
      ))}
    </div>
  );

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
        <h1 className="text-2xl font-bold text-gray-900 mb-1">Reviews & Ratings</h1>
        <p className="text-sm text-gray-500">Patient feedback on your consultations</p>
      </div>

      {/* Rating Summary Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-8">
        <div className="bg-white border border-gray-200 rounded-xl p-5 shadow-sm">
          <div className="flex items-center gap-3 mb-2">
            <div className="w-10 h-10 rounded-lg bg-yellow-50 flex items-center justify-center">
              <Star className="w-5 h-5 text-yellow-500" />
            </div>
            <span className="text-xs font-semibold uppercase tracking-wider text-gray-500">Average Rating</span>
          </div>
          <div className="flex items-end gap-2">
            <span className="text-3xl font-bold text-gray-900">{summary.averageRating || '0.0'}</span>
            <span className="text-sm text-gray-400 mb-1">/ 5</span>
          </div>
          <div className="mt-1">{renderStars(Math.round(summary.averageRating || 0))}</div>
        </div>
        <div className="bg-white border border-gray-200 rounded-xl p-5 shadow-sm">
          <div className="flex items-center gap-3 mb-2">
            <div className="w-10 h-10 rounded-lg bg-blue-50 flex items-center justify-center">
              <MessageSquare className="w-5 h-5 text-blue-500" />
            </div>
            <span className="text-xs font-semibold uppercase tracking-wider text-gray-500">Total Reviews</span>
          </div>
          <span className="text-3xl font-bold text-gray-900">{summary.totalReviews || 0}</span>
        </div>
        <div className="bg-white border border-gray-200 rounded-xl p-5 shadow-sm">
          <div className="flex items-center gap-3 mb-2">
            <div className="w-10 h-10 rounded-lg bg-green-50 flex items-center justify-center">
              <TrendingUp className="w-5 h-5 text-green-500" />
            </div>
            <span className="text-xs font-semibold uppercase tracking-wider text-gray-500">Rating Status</span>
          </div>
          <span className="text-lg font-bold text-gray-900">
            {summary.averageRating >= 4.5 ? '⭐ Excellent' :
             summary.averageRating >= 3.5 ? '👍 Good' :
             summary.averageRating >= 2.5 ? '📊 Average' :
             summary.totalReviews > 0 ? '📈 Needs Improvement' : 'No reviews yet'}
          </span>
        </div>
      </div>

      {/* Reviews List */}
      {reviews.length === 0 ? (
        <div className="text-center py-16 bg-white border border-gray-200 rounded-xl">
          <MessageSquare className="w-12 h-12 mx-auto text-gray-300 mb-4" />
          <h3 className="text-base font-semibold text-gray-600 mb-1">No reviews yet</h3>
          <p className="text-sm text-gray-400">Reviews will appear here after patients rate your consultations</p>
        </div>
      ) : (
        <div className="space-y-4">
          {reviews.map((review) => (
            <div key={review.id} className={`bg-white border rounded-xl p-5 shadow-sm ${review.isFlagged ? 'border-red-200 bg-red-50/30' : 'border-gray-200'}`}>
              <div className="flex justify-between items-start">
                <div className="flex-1">
                  <div className="flex items-center gap-3 mb-2">
                    {renderStars(review.rating)}
                    <span className="text-sm font-semibold text-gray-700">{review.rating}/5</span>
                    {review.isVerified && (
                      <span className="inline-flex px-2 py-0.5 text-[10px] font-semibold rounded-full bg-green-50 text-green-600 border border-green-200">Verified</span>
                    )}
                    {review.isFlagged && (
                      <span className="inline-flex px-2 py-0.5 text-[10px] font-semibold rounded-full bg-red-50 text-red-600 border border-red-200">Flagged</span>
                    )}
                  </div>
                  <p className="text-sm text-gray-700 mb-2">{review.comment || <span className="italic text-gray-400">No comment</span>}</p>
                  <div className="flex items-center gap-3 text-xs text-gray-400">
                    <span>By: {review.patientName}</span>
                    <span>•</span>
                    <span>{new Date(review.createdAt).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' })}</span>
                  </div>
                </div>
                {!review.isFlagged && (
                  <button
                    onClick={() => handleFlag(review.id)}
                    className="text-gray-400 hover:text-red-500 transition-colors cursor-pointer p-1"
                    title="Flag for moderation"
                  >
                    <Flag className="w-4 h-4" />
                  </button>
                )}
              </div>
            </div>
          ))}

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex justify-center gap-2 pt-4">
              <button
                onClick={() => setPage(p => Math.max(0, p - 1))}
                disabled={page === 0}
                className="px-3 py-1.5 text-sm rounded-lg border border-gray-300 hover:bg-gray-50 disabled:opacity-40 cursor-pointer disabled:cursor-not-allowed"
              >
                Previous
              </button>
              <span className="px-3 py-1.5 text-sm text-gray-500">
                Page {page + 1} of {totalPages}
              </span>
              <button
                onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                disabled={page >= totalPages - 1}
                className="px-3 py-1.5 text-sm rounded-lg border border-gray-300 hover:bg-gray-50 disabled:opacity-40 cursor-pointer disabled:cursor-not-allowed"
              >
                Next
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
