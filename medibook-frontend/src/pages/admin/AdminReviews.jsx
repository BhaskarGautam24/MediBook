import { useState, useEffect } from 'react';
import api from '../../services/api';
import toast from 'react-hot-toast';
import { Star, Flag, Trash2, CheckCircle, MessageSquare } from 'lucide-react';

/**
 * AdminReviews — Admin moderation page for flagged reviews.
 * Admins can view all flagged reviews, approve (unflag) or delete them.
 */
export default function AdminReviews() {
  const [flaggedReviews, setFlaggedReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    loadFlagged();
  }, [page]);

  const loadFlagged = async () => {
    try {
      const data = await api.get(`/reviews/admin/flagged?page=${page}&size=10`);
      setFlaggedReviews(data.content || []);
      setTotalPages(data.totalPages || 0);
    } catch (err) {
      console.error('Failed to load flagged reviews:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleUnflag = async (reviewId) => {
    try {
      await api.put(`/reviews/admin/${reviewId}/unflag`);
      toast.success('Review approved');
      loadFlagged();
    } catch (err) {
      toast.error(err.message || 'Failed to unflag');
    }
  };

  const handleDelete = async (reviewId) => {
    if (!window.confirm('Permanently delete this review?')) return;
    try {
      await api.delete(`/reviews/admin/${reviewId}`);
      toast.success('Review deleted');
      loadFlagged();
    } catch (err) {
      toast.error(err.message || 'Failed to delete');
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
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900 mb-1">Review Moderation</h1>
        <p className="text-sm text-gray-500">Manage flagged reviews reported by providers</p>
      </div>

      {flaggedReviews.length === 0 ? (
        <div className="text-center py-16 bg-white border border-gray-200 rounded-xl">
          <MessageSquare className="w-12 h-12 mx-auto text-gray-300 mb-4" />
          <h3 className="text-base font-semibold text-gray-600 mb-1">No flagged reviews</h3>
          <p className="text-sm text-gray-400">All reviews are clean — nothing to moderate</p>
        </div>
      ) : (
        <div className="space-y-4">
          {flaggedReviews.map((review) => (
            <div key={review.id} className="bg-white border border-red-200 rounded-xl p-5 shadow-sm">
              <div className="flex justify-between items-start">
                <div className="flex-1">
                  <div className="flex items-center gap-3 mb-2">
                    <div className="flex gap-0.5">
                      {[1, 2, 3, 4, 5].map((s) => (
                        <Star key={s} className={`w-4 h-4 ${s <= review.rating ? 'fill-yellow-400 text-yellow-400' : 'text-gray-200'}`} />
                      ))}
                    </div>
                    <span className="text-sm font-semibold text-gray-700">{review.rating}/5</span>
                    <span className="inline-flex px-2 py-0.5 text-[10px] font-semibold rounded-full bg-red-50 text-red-600 border border-red-200">
                      <Flag className="w-3 h-3 mr-1" /> Flagged
                    </span>
                  </div>
                  <p className="text-sm text-gray-700 mb-2">{review.comment || <span className="italic text-gray-400">No comment</span>}</p>
                  <div className="flex flex-wrap items-center gap-3 text-xs text-gray-400">
                    <span>Patient: {review.patientName}</span>
                    <span>•</span>
                    <span>Provider: {review.providerName}</span>
                    <span>•</span>
                    <span>{new Date(review.createdAt).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' })}</span>
                  </div>
                </div>
                <div className="flex gap-2 ml-4">
                  <button
                    onClick={() => handleUnflag(review.id)}
                    className="inline-flex items-center gap-1 px-3 py-1.5 text-xs font-medium text-green-700 bg-green-50 rounded-lg hover:bg-green-100 border border-green-200 transition-colors cursor-pointer"
                    title="Approve — remove flag"
                  >
                    <CheckCircle className="w-3.5 h-3.5" /> Approve
                  </button>
                  <button
                    onClick={() => handleDelete(review.id)}
                    className="inline-flex items-center gap-1 px-3 py-1.5 text-xs font-medium text-red-700 bg-red-50 rounded-lg hover:bg-red-100 border border-red-200 transition-colors cursor-pointer"
                    title="Delete review permanently"
                  >
                    <Trash2 className="w-3.5 h-3.5" /> Delete
                  </button>
                </div>
              </div>
            </div>
          ))}

          {totalPages > 1 && (
            <div className="flex justify-center gap-2 pt-4">
              <button onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0}
                className="px-3 py-1.5 text-sm rounded-lg border border-gray-300 hover:bg-gray-50 disabled:opacity-40 cursor-pointer disabled:cursor-not-allowed">
                Previous
              </button>
              <span className="px-3 py-1.5 text-sm text-gray-500">Page {page + 1} of {totalPages}</span>
              <button onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))} disabled={page >= totalPages - 1}
                className="px-3 py-1.5 text-sm rounded-lg border border-gray-300 hover:bg-gray-50 disabled:opacity-40 cursor-pointer disabled:cursor-not-allowed">
                Next
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
